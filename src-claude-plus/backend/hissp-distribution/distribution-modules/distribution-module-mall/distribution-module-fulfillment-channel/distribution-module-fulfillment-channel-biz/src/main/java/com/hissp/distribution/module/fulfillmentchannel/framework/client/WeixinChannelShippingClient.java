package com.hissp.distribution.module.fulfillmentchannel.framework.client;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.shop.request.shipping.*;
import cn.binarywang.wx.miniapp.bean.shop.response.WxMaOrderShippingInfoBaseResponse;
import cn.binarywang.wx.miniapp.bean.shop.response.WxMaOrderShippingInfoGetResponse;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO.ChannelShippingConfirmStatus;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.WeixinOrderConfirmExtraRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingChannelEnum;
import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingLogisticsTypeEnum;
import com.hissp.distribution.module.fulfillmentchannel.property.ChannelShippingProperties;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.error.WxError;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

/**
 * 微信渠道发货实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeixinChannelShippingClient implements ChannelShippingClient {

    private final WxMaService wxMaService;
    private final ChannelShippingProperties properties;

    private static final int WECHAT_ERROR_ORDER_NOT_FOUND = 10060001;
    private static final int WECHAT_ERROR_ORDER_NOT_SHIPPABLE = 10060004;
    private static final int MAX_RETRY_TIMES = 3;
    private static final long RETRY_BACKOFF_MS = 200L;

    @Override
    public boolean supports(Integer channel) {
        return Objects.equals(ChannelShippingChannelEnum.WECHAT_MINIAPP.getCode(), channel);
    }

    @Override
    public void ship(ChannelShippingRequestDTO request,
                     PayOrderRespDTO payOrder) throws Exception {
        ChannelShippingProperties.Weixin weixin = properties.getWeixin();
        // 未开启配置时直接跳过，避免在单体或测试环境产生误调用
        if (weixin == null || !Boolean.TRUE.equals(weixin.getEnabled())) {
            log.warn("[ship][orderNo={}] 微信发货未启用，忽略推送", request.getOrderNo());
            return;
        }
        // 微信要求带上支付流水号和 openid，否则无法关联订单
        if (payOrder == null || payOrder.getChannelOrderNo() == null || payOrder.getChannelUserId() == null) {
            throw new IllegalStateException("缺少渠道支付信息，无法推送微信发货");
        }
        // 快递类型必须补充物流单号与快递公司，平台会进行校验
        if (Objects.equals(request.getLogisticsType(), ChannelShippingLogisticsTypeEnum.EXPRESS.getType())
                && (request.getLogisticsNo() == null || request.getLogisticsCompanyCode() == null)) {
            throw new IllegalArgumentException("快递发货缺少物流单号或快递公司编码");
        }
        log.info("[ship][request={}] 渠道开始发货", JsonUtils.toJsonString(request));
        boolean canShip = ensureDeliverable(request, payOrder, weixin);
        if (!canShip) {
            log.info("[ship][orderNo={}] 微信订单已满足发货或不需要重复推送，忽略本次发货", request.getOrderNo());
            return;
        }
        uploadWithRetry(request, payOrder, weixin);
    }

    private WxMaOrderShippingInfoUploadRequest buildRequest(ChannelShippingRequestDTO request,
                                                            PayOrderRespDTO payOrder,
                                                            ChannelShippingProperties.Weixin weixin) {
        // 微信识别订单的关键字段，包括支付交易号与商户号
        String transactionId = StrUtil.blankToDefault(payOrder.getChannelTransactionNo(), payOrder.getChannelOrderNo());
        if (StrUtil.isBlank(transactionId)) {
            log.warn("[buildRequest][orderNo={}] 渠道交易流水为空，可能导致微信发货失败", request.getOrderNo());
        }
        OrderKeyBean orderKey = OrderKeyBean.builder()
                .orderNumberType(2)
                .transactionId(transactionId)
                .mchId(weixin.getMchId())
                //.outTradeNo(request.getOrderNo())
                .build();

        List<ShippingListBean> shippingList = buildShippingList(request);

        return WxMaOrderShippingInfoUploadRequest.builder()
                .orderKey(orderKey)
                // 若未显式指定物流模式，默认按虚拟商品推送
                .logisticsType(Objects.requireNonNullElse(request.getLogisticsType(),
                        ChannelShippingLogisticsTypeEnum.VIRTUAL_GOODS.getType()))
                .deliveryMode(1)
                .isAllDelivered(request.getAllDelivered())
                .shippingList(shippingList)
                .uploadTime(DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.now()))
                .payer(PayerBean.builder().openid(payOrder.getChannelUserId()).build())
                .build();
    }

    private List<ShippingListBean> buildShippingList(ChannelShippingRequestDTO request) {
        if (Objects.equals(request.getLogisticsType(), ChannelShippingLogisticsTypeEnum.VIRTUAL_GOODS.getType())) {
            // 虚拟商品场景封装单独的虚拟物流信息
            return List.of(ShippingListBean.builder()
                    .itemDesc(buildVirtualItemDesc(request))
                    .build());
        }

        // 快递/同城/自提等模式需要拼装物流编码与运单号
        ShippingListBean.ShippingListBeanBuilder builder = ShippingListBean.builder()
                .trackingNo(request.getLogisticsNo())
                .expressCompany(request.getLogisticsCompanyCode())
                .itemDesc(request.getItemDesc());
        if (request.getReceiverContact() != null) {
            builder.contact(ContactBean.builder().receiverContact(request.getReceiverContact()).build());
        }
        return List.of(builder.build());
    }

    private String buildVirtualItemDesc(ChannelShippingRequestDTO request) {
        String desc = request.getItemDesc();
        if (StrUtil.isNotBlank(desc)) {
            return desc;
        }
        if (CollUtil.isEmpty(request.getItems())) {
            return "虚拟商品";
        }
        // 取前10个商品名称拼接，避免描述过长
        return request.getItems().stream()
                .map(ChannelShippingRequestDTO.Item::getSpuName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .limit(10)
                .collect(Collectors.joining("、"));
    }

    private boolean ensureDeliverable(ChannelShippingRequestDTO request,
                                      PayOrderRespDTO payOrder,
                                      ChannelShippingProperties.Weixin weixin) throws Exception {
        String transactionId = StrUtil.blankToDefault(payOrder.getChannelTransactionNo(), payOrder.getChannelOrderNo());
        if (StrUtil.isBlank(transactionId)) {
            log.warn("[ensureDeliverable][orderNo={}] 渠道交易流水为空，跳过状态校验直接尝试发货", request.getOrderNo());
            return true;
        }
        WxMaOrderShippingInfoGetRequest wxRequest = WxMaOrderShippingInfoGetRequest.builder()
                .transactionId(transactionId)
                .merchantId(weixin.getMchId())
                .merchantTradeNo(StrUtil.blankToDefault(request.getOrderNo(), payOrder.getMerchantOrderId()))
                .build();
        if (StrUtil.isNotBlank(weixin.getSubMchId())) {
            wxRequest.setSubMerchantId(weixin.getSubMchId());
        }
        WxMaOrderShippingInfoGetResponse response =
                wxMaService.getWxMaOrderShippingService().get(wxRequest);
        if (response == null) {
            return true;
        }
        Integer errCode = response.getErrCode();
        if (errCode != null && errCode != 0) {
            if (Objects.equals(errCode, WECHAT_ERROR_ORDER_NOT_FOUND)) {
                // 交由后续发货流程重试
                return true;
            }
            throw buildWxErrorException(errCode, response.getErrMsg());
        }
        WxMaOrderShippingInfoBaseResponse.Order order = response.getOrder();
        if (order == null) {
            return true;
        }
        Integer orderState = order.getOrderState();
        if (orderState == null) {
            return true;
        }
        if (orderState < 1) {
            log.info("[ensureDeliverable][orderNo={}] 微信订单状态({})未达到发货条件，等待后续重试", request.getOrderNo(), orderState);
            throw buildWxErrorException(WECHAT_ERROR_ORDER_NOT_SHIPPABLE, "微信支付单状态未达到发货条件");
        }
        if (orderState >= 2) {
            log.info("[ensureDeliverable][orderNo={}] 微信订单状态({})显示已发货或完结，跳过重复推送", request.getOrderNo(), orderState);
            return false;
        }
        return true;
    }

    private void uploadWithRetry(ChannelShippingRequestDTO request,
                                 PayOrderRespDTO payOrder,
                                 ChannelShippingProperties.Weixin weixin) throws Exception {
        for (int attempt = 1; attempt <= MAX_RETRY_TIMES; attempt++) {
            WxMaOrderShippingInfoUploadRequest uploadRequest = buildRequest(request, payOrder, weixin);
            try {
                WxMaOrderShippingInfoBaseResponse response =
                        wxMaService.getWxMaOrderShippingService().upload(uploadRequest);
                Integer errCode = response != null ? response.getErrCode() : null;
                if (errCode == null || errCode == 0) {
                    return;
                }
                if (shouldRetry(errCode) && attempt < MAX_RETRY_TIMES) {
                    log.info("[uploadWithRetry][orderNo={}] 微信返回支付单不存在，准备第{}次重试", request.getOrderNo(), attempt + 1);
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw buildWxErrorException(errCode, response != null ? response.getErrMsg() : null);
            } catch (WxErrorException ex) {
                int errCode = extractErrorCode(ex);
                if (shouldRetry(errCode) && attempt < MAX_RETRY_TIMES) {
                    log.info("[uploadWithRetry][orderNo={}] 微信返回支付单不存在，准备第{}次重试", request.getOrderNo(), attempt + 1, ex);
                    sleepBeforeRetry(attempt);
                    continue;
                }
                throw ex;
            }
        }
        throw buildWxErrorException(WECHAT_ERROR_ORDER_NOT_FOUND, "支付单不存在，重试仍失败");
    }

    private boolean shouldRetry(Integer errCode) {
        return Objects.equals(errCode, WECHAT_ERROR_ORDER_NOT_FOUND);
    }

    private void sleepBeforeRetry(int attempt) {
        long delay = RETRY_BACKOFF_MS * (1L << (attempt - 1));
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("微信虚拟发货重试被中断", ex);
        }
    }

    private int extractErrorCode(WxErrorException ex) {
        if (ex == null) {
            return -1;
        }
        if (ex.getError() != null) {
            return ex.getError().getErrorCode();
        }
        return -1;
    }

    private WxErrorException buildWxErrorException(Integer errCode, String errMsg) {
        WxError error = WxError.builder()
                .errorCode(errCode != null ? errCode : -1)
                .errorMsg(StrUtil.blankToDefault(errMsg, "未知错误"))
                .build();
        return new WxErrorException(error);
    }

    @Override
    public ChannelShippingQueryRespDTO query(ChannelShippingQueryRequestDTO request, PayOrderRespDTO payOrder) throws Exception {
        ChannelShippingQueryRespDTO respDTO = new ChannelShippingQueryRespDTO().setShipped(false);
        ChannelShippingProperties.Weixin weixin = properties.getWeixin();
        if (weixin == null || !Boolean.TRUE.equals(weixin.getEnabled())) {
            return respDTO;
        }
        if (payOrder == null) {
            return respDTO;
        }
        String channelCode = payOrder.getChannelCode();
        if (StrUtil.isBlank(channelCode) || !StrUtil.containsIgnoreCase(channelCode, "wx")) {
            return respDTO;
        }
        String transactionId = StrUtil.blankToDefault(payOrder.getChannelTransactionNo(), payOrder.getChannelOrderNo());
        if (StrUtil.isBlank(transactionId)) {
            return respDTO;
        }

        WxMaOrderShippingInfoGetRequest wxRequest = WxMaOrderShippingInfoGetRequest.builder()
                .transactionId(transactionId)
                .merchantId(weixin.getMchId())
                .merchantTradeNo(StrUtil.blankToDefault(request.getOrderNo(), payOrder.getMerchantOrderId()))
                .build();
        if (StrUtil.isNotBlank(weixin.getSubMchId())) {
            wxRequest.setSubMerchantId(weixin.getSubMchId());
        }

        WxMaOrderShippingInfoGetResponse response =
                wxMaService.getWxMaOrderShippingService().get(wxRequest);
        if (response == null) {
            return respDTO;
        }
        Integer errCode = response.getErrCode();
        if (errCode != null && errCode != 0) {
            log.warn("[query][orderId={}] 微信返回错误码：{} - {}", request.getOrderId(), errCode, response.getErrMsg());
            return respDTO;
        }
        WxMaOrderShippingInfoBaseResponse.Order order = response.getOrder();
        if (order == null || order.getShipping() == null) {
            return respDTO;
        }
        WxMaOrderShippingInfoBaseResponse.Shipping shipping = order.getShipping();
        boolean shipped = (shipping.getShippingList() != null && !shipping.getShippingList().isEmpty())
                || Boolean.TRUE.equals(shipping.getFinishShipping())
                || (order.getOrderState() != null && order.getOrderState() >= 2);
        respDTO.setShipped(shipped);

        ChannelShippingConfirmStatus confirmStatus = resolveConfirmStatus(order.getOrderState());
        if (confirmStatus != null) {
            respDTO.setConfirmStatus(confirmStatus);
        }
        if (shipped) {
            WeixinOrderConfirmExtraRespDTO extra = buildWeixinConfirmExtra(request, payOrder, weixin, transactionId);
            respDTO.addExtra(ChannelShippingQueryRespDTO.EXTRA_WEIXIN_CONFIRM, extra);
        }
        return respDTO;
    }

    private ChannelShippingConfirmStatus resolveConfirmStatus(Integer orderState) {
        if (orderState == null) {
            return null;
        }
        switch (orderState) {
            case 3: // 确认收货
            case 4: // 交易完成
                return ChannelShippingConfirmStatus.CONFIRMED;
            case 5: // 已退款
                return ChannelShippingConfirmStatus.CANCELED;
            case 2: // 已发货
            case 1: // 待发货
                return ChannelShippingConfirmStatus.UNCONFIRMED;
            default:
                return null;
        }
    }

    private WeixinOrderConfirmExtraRespDTO buildWeixinConfirmExtra(ChannelShippingQueryRequestDTO request,
                                                                   PayOrderRespDTO payOrder,
                                                                   ChannelShippingProperties.Weixin weixin,
                                                                   String transactionId) {
        if (!Boolean.TRUE.equals(request.getDelivered())) {
            return null;
        }
        if (StrUtil.isBlank(weixin.getMchId())) {
            return null;
        }
        WeixinOrderConfirmExtraRespDTO respDTO = new WeixinOrderConfirmExtraRespDTO();
        respDTO.setMerchantId(weixin.getMchId());
        if (StrUtil.isNotBlank(weixin.getSubMchId())) {
            respDTO.setSubMerchantId(weixin.getSubMchId());
        }
        respDTO.setTransactionId(transactionId);
        respDTO.setMerchantTradeNo(StrUtil.blankToDefault(request.getOrderNo(), payOrder.getMerchantOrderId()));
        return respDTO;
    }

}
