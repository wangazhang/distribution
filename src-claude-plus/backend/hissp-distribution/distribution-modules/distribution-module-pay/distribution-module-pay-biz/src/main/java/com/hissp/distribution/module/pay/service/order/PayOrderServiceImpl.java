package com.hissp.distribution.module.pay.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.date.LocalDateTimeUtils;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.framework.common.util.id.IdGeneratorUtil;
import com.hissp.distribution.framework.common.util.number.MoneyUtils;
import com.hissp.distribution.framework.pay.core.client.PayClient;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderDeliveryStatusRespEnum;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import com.hissp.distribution.framework.tenant.core.util.TenantUtils;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderExportReqVO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderPageReqVO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderSubmitReqVO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.PayOrderSubmitRespVO;
import com.hissp.distribution.module.pay.convert.order.PayOrderConvert;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.channel.PayChannelDO;
import com.hissp.distribution.module.pay.dal.dataobject.order.PayOrderDO;
import com.hissp.distribution.module.pay.dal.mysql.order.PayOrderMapper;
import com.hissp.distribution.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hissp.distribution.module.pay.enums.notify.PayNotifyTypeEnum;
import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import com.hissp.distribution.module.pay.framework.pay.config.PayProperties;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.channel.PayChannelService;
import com.hissp.distribution.module.pay.service.notify.PayNotifyService;
import com.google.common.annotations.VisibleForTesting;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.util.json.JsonUtils.toJsonString;
import static com.hissp.distribution.module.pay.enums.ErrorCodeConstants.*;

/**
 * 支付订单 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
@Slf4j
public class PayOrderServiceImpl implements PayOrderService {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayOrderMapper orderMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public PayOrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public PayOrderDO getOrder(Long appId, String merchantOrderId) {
        return orderMapper.selectByAppIdAndMerchantOrderId(appId, merchantOrderId);
    }

    @Override
    public List<PayOrderDO> getOrderList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return orderMapper.selectBatchIds(ids);
    }

    @Override
    public PayOrderDO getOrderByNo(String no) {
        return orderMapper.selectByNo(no);
    }

    @Override
    public Long getOrderCountByAppId(Long appId) {
        return orderMapper.selectCountByAppId(appId);
    }

    @Override
    public PageResult<PayOrderDO> getOrderPage(PayOrderPageReqVO pageReqVO) {
        return orderMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayOrderDO> getOrderList(PayOrderExportReqVO exportReqVO) {
        return orderMapper.selectList(exportReqVO);
    }

    @Override
    public Long createOrder(PayOrderCreateReqDTO reqDTO) {
        PayAppDO app = appService.validPayApp(reqDTO.getAppKey());
        PayOrderDO order = orderMapper.selectByAppIdAndMerchantOrderId(app.getId(), reqDTO.getMerchantOrderId());
        if (order != null) {
            log.warn("[createOrder][appId({}) merchantOrderId({}) 已经存在对应的支付单({})]", order.getAppId(),
                    order.getMerchantOrderId(), toJsonString(order));
            return order.getId();
        }
        if ("test".equals(activeProfile) || "local".equals(activeProfile)) {
            reqDTO.setPrice(1);
        }
        order = PayOrderConvert.INSTANCE.convert(reqDTO).setAppId(app.getId()).setId(IdGeneratorUtil.nextId())
                .setNotifyUrl(app.getOrderNotifyUrl())
                .setStatus(PayOrderStatusEnum.WAITING.getStatus())
                .setRefundPrice(0);
        orderMapper.insert(order);
        return order.getId();
    }

    @Override
    public PayOrderSubmitRespVO submitOrder(PayOrderSubmitReqVO reqVO, String userIp) {
        PayOrderDO order = validateOrderCanSubmit(reqVO.getId());
        PayChannelDO channel = validateChannelCanSubmit(order.getAppId(), reqVO.getChannelCode());
        PayClient client = channelService.getPayClient(channel.getId());

        String no = noRedisDAO.generate(payProperties.getOrderNoPrefix());
        String channelUserId = resolveChannelUserId(reqVO.getChannelExtras());
        order.setChannelId(channel.getId());
        order.setChannelCode(channel.getCode());
        order.setNo(no);
        if (StrUtil.isNotBlank(channelUserId)) {
            order.setChannelUserId(channelUserId);
        }
        orderMapper.updateById(order);

        PayOrderUnifiedInnerReqDTO unifiedOrderReqDTO = PayOrderConvert.INSTANCE.convert2(reqVO, userIp)
                .setOutTradeNo(order.getNo())
                .setSubject(order.getSubject()).setBody(order.getBody())
                .setNotifyUrl(genChannelOrderNotifyUrl(channel))
                .setReturnUrl(reqVO.getReturnUrl())
                .setPrice(order.getPrice()).setExpireTime(order.getExpireTime());
        PayOrderInnerRespDTO unifiedOrderResp = client.unifiedOrder(unifiedOrderReqDTO);

        fillChannelUserId(unifiedOrderResp, channelUserId);
        if (unifiedOrderResp != null) {
            try {
                getSelf().notifyOrder(channel, unifiedOrderResp);
            } catch (Exception e) {
                log.warn("[submitOrder][order({}) channel({}) 支付结果({}) 通知时发生异常，可能是并发问题]",
                        order, channel, unifiedOrderResp, e);
            }
            if (StrUtil.isNotEmpty(unifiedOrderResp.getChannelErrorCode())) {
                throw exception(PAY_ORDER_SUBMIT_CHANNEL_ERROR,
                        unifiedOrderResp.getChannelErrorMsg(), unifiedOrderResp.getChannelErrorCode());
            }
            order = orderMapper.selectById(order.getId());
        }
        return PayOrderConvert.INSTANCE.convert(order, unifiedOrderResp);
    }

    private PayOrderDO validateOrderCanSubmit(Long id) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_SUCCESS);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        if (LocalDateTimeUtils.beforeNow(order.getExpireTime())) {
            throw exception(PAY_ORDER_IS_EXPIRED);
        }
        validateOrderActuallyPaid(id);
        return order;
    }

    @VisibleForTesting
    void validateOrderActuallyPaid(Long id) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order.getChannelId() == null || order.getNo() == null) {
            return;
        }
        PayClient payClient = channelService.getPayClient(order.getChannelId());
        if (payClient == null) {
            log.error("[validateOrderCanSubmit][渠道编号({}) 找不到对应的支付客户端]", order.getChannelId());
            return;
        }
        PayOrderInnerRespDTO respDTO = payClient.getOrder(order.getNo());
        if (respDTO != null && PayOrderStatusRespEnum.isSuccess(respDTO.getStatus())) {
            log.warn("[validateOrderCanSubmit][order({}) 的 PayOrderRespDTO({}) 已支付，可能是回调延迟]",
                    id, toJsonString(respDTO));
            throw exception(PAY_ORDER_EXTENSION_IS_PAID);
        }
    }

    private PayChannelDO validateChannelCanSubmit(Long appId, String channelCode) {
        appService.validPayApp(appId);
        PayChannelDO channel = channelService.validPayChannel(appId, channelCode);
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[validatePayChannelCanSubmit][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        return channel;
    }

    private String genChannelOrderNotifyUrl(PayChannelDO channel) {
        return payProperties.getOrderNotifyUrl() + "/" + channel.getId();
    }

    private String resolveChannelUserId(Map<String, String> extras) {
        if (extras == null || extras.isEmpty()) {
            return null;
        }
        String channelUserId = extras.get("openId");
        if (StrUtil.isBlank(channelUserId)) {
            channelUserId = extras.get("openid");
        }
        return StrUtil.blankToDefault(channelUserId, null);
    }

    private void fillChannelUserId(PayOrderInnerRespDTO unifiedOrderResp, String channelUserId) {
        if (unifiedOrderResp == null) {
            return;
        }
        if (StrUtil.isBlank(channelUserId)) {
            return;
        }
        unifiedOrderResp.setChannelUserId(channelUserId);
    }

    @Override
    public void notifyOrder(Long channelId, PayOrderInnerRespDTO notify) {
        PayChannelDO channel = channelService.validPayChannel(channelId);
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyOrder(channel, notify));
    }

    @Transactional(rollbackFor = Exception.class)
    public void notifyOrder(PayChannelDO channel, PayOrderInnerRespDTO notify) {
        if (PayOrderStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyOrderSuccess(channel, notify);
        } else if (PayOrderStatusRespEnum.isClosed(notify.getStatus())) {
            notifyOrderClosed(channel, notify);
        } else {
            log.debug("[notifyOrder][订单({}) 状态({})无需处理]", notify.getOutTradeNo(), notify.getStatus());
        }
    }

    private void notifyOrderSuccess(PayChannelDO channel, PayOrderInnerRespDTO notify) {
        // 先消费渠道通知，输出统一的决策结果，后续才能据此决定是否再次回调业务系统
        PayOrderNotifyResult result = updateOrderSuccess(channel, notify);
        if (!result.shouldNotifyBusiness()) {
            log.info("[notifyOrderSuccess][order({}) 忽略重复通知 deliveryChanged={} statusChanged=false]",
                    result.getOrder().getId(), result.isDeliveryStatusChanged());
            return;
        }
        PayOrderDO order = result.getOrder();
        String rawPayload = toRawPayload(notify.getRawData());
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.ORDER.getType(), order.getId(),
                buildNotifyExtParams(result.getDeliveryStatus(), rawPayload));
    }

    private String toRawPayload(Object rawData) {
        if (rawData == null) {
            return null;
        }
        if (rawData instanceof String) {
            return (String) rawData;
        }
        try {
            return JsonUtils.toJsonString(rawData);
        } catch (Exception ex) {
            log.warn("[toRawPayload][序列化渠道通知数据失败 raw={}]", rawData, ex);
            return null;
        }
    }

    private Map<String, Object> buildNotifyExtParams(PayOrderDeliveryStatusRespEnum deliveryStatus, String rawPayload) {
        Map<String, Object> extParams = new HashMap<>(2);
        if (deliveryStatus != null) {
            extParams.put("deliveryStatus", deliveryStatus.getCode());
        }
        if (StrUtil.isNotBlank(rawPayload)) {
            extParams.put("rawPayload", rawPayload);
        }
        return extParams.isEmpty() ? null : extParams;
    }

    /**
     * 计算当前应写入的手续费费率，优先使用渠道回调中的真实值。
     */
    private Double resolveChannelFeeRate(PayOrderDO order, PayChannelDO channel, Double notifiedFeeRate) {
        if (notifiedFeeRate != null) {
            return notifiedFeeRate;
        }
        if (order.getChannelFeeRate() != null) {
            return order.getChannelFeeRate();
        }
        return channel.getFeeRate();
    }

    /**
     * 计算应写入的手续费金额，优先使用渠道提供的 feeAmount。
     */
    private Integer resolveChannelFeePrice(PayOrderDO order, Integer notifiedFeePrice, Double feeRate) {
        if (notifiedFeePrice != null) {
            return notifiedFeePrice;
        }
        if (order.getChannelFeePrice() != null && order.getChannelFeePrice() > 0) {
            return order.getChannelFeePrice();
        }
        if (feeRate == null || order.getPrice() == null) {
            return order.getChannelFeePrice();
        }
        return MoneyUtils.calculateRatePrice(order.getPrice(), feeRate);
    }

    /**
     * 已支付订单在收到附加通知时，补写手续费、妥投等字段。
     */
    private boolean updateSuccessExtrasIfNeeded(PayOrderDO order, String deliveryStatusCode, boolean deliveryChanged,
                                                Double notifiedFeeRate, Integer notifiedFeePrice) {
        boolean needUpdate = false;
        PayOrderDO patch = new PayOrderDO().setId(order.getId());
        if (deliveryChanged && StrUtil.isNotBlank(deliveryStatusCode)) {
            patch.setDeliveryStatus(deliveryStatusCode);
            needUpdate = true;
        }
        if (notifiedFeeRate != null && !Objects.equals(notifiedFeeRate, order.getChannelFeeRate())) {
            patch.setChannelFeeRate(notifiedFeeRate);
            needUpdate = true;
        }
        if (notifiedFeePrice != null && !Objects.equals(notifiedFeePrice, order.getChannelFeePrice())) {
            patch.setChannelFeePrice(notifiedFeePrice);
            needUpdate = true;
        }
        if (!needUpdate) {
            return false;
        }
        orderMapper.updateById(patch);
        return true;
    }

    /**
     * 支付回调在本地的统一处理结果。
     */
    private static final class PayOrderNotifyResult {
        private final PayOrderDO order;
        private final boolean orderStatusChanged;
        private final boolean deliveryStatusChanged;
        private final PayOrderDeliveryStatusRespEnum deliveryStatus;

        private PayOrderNotifyResult(PayOrderDO order, boolean orderStatusChanged,
                                     boolean deliveryStatusChanged, PayOrderDeliveryStatusRespEnum deliveryStatus) {
            this.order = order;
            this.orderStatusChanged = orderStatusChanged;
            this.deliveryStatusChanged = deliveryStatusChanged;
            this.deliveryStatus = deliveryStatus;
        }

        static PayOrderNotifyResult of(PayOrderDO order, boolean orderStatusChanged,
                                       boolean deliveryStatusChanged, PayOrderDeliveryStatusRespEnum deliveryStatus) {
            return new PayOrderNotifyResult(order, orderStatusChanged, deliveryStatusChanged, deliveryStatus);
        }

        PayOrderDO getOrder() {
            return order;
        }

        PayOrderDeliveryStatusRespEnum getDeliveryStatus() {
            return deliveryStatus;
        }

        boolean isDeliveryStatusChanged() {
            return deliveryStatusChanged;
        }

        boolean shouldNotifyBusiness() {
            return orderStatusChanged || deliveryStatusChanged;
        }
    }

    private PayOrderNotifyResult updateOrderSuccess(PayChannelDO channel, PayOrderInnerRespDTO notify) {
        PayOrderDO order = orderMapper.selectByNo(notify.getOutTradeNo());
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        PayOrderDeliveryStatusRespEnum deliveryStatus = notify.getDeliveryStatus();
        String targetDeliveryCode = deliveryStatus != null ? deliveryStatus.getCode() : null;
        boolean deliveryChanged = targetDeliveryCode != null && !Objects.equals(targetDeliveryCode, order.getDeliveryStatus());

        if (PayOrderStatusEnum.isSuccess(order.getStatus())) {
            boolean patched = updateSuccessExtrasIfNeeded(order, targetDeliveryCode, deliveryChanged,
                    notify.getChannelFeeRate(), notify.getChannelFeePrice());
            if (patched) {
                order = orderMapper.selectById(order.getId());
            }
            if (deliveryChanged) {
                log.info("[updateOrderSuccess][order({}) 妥投状态更新为 {}]", order.getId(), targetDeliveryCode);
            }
            return PayOrderNotifyResult.of(order, false, deliveryChanged, deliveryStatus);
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }

        Double effectiveFeeRate = resolveChannelFeeRate(order, channel, notify.getChannelFeeRate());
        Integer effectiveFeePrice = resolveChannelFeePrice(order, notify.getChannelFeePrice(), effectiveFeeRate);

        int updateCounts = orderMapper.updateByIdAndStatus(order.getId(), PayOrderStatusEnum.WAITING.getStatus(),
                PayOrderDO.builder().status(PayOrderStatusEnum.SUCCESS.getStatus())
                        .channelId(channel.getId()).channelCode(channel.getCode())
                        .successTime(notify.getSuccessTime()).no(notify.getOutTradeNo())
                        .channelOrderNo(notify.getChannelOrderNo())
                        .channelTransactionNo(notify.getChannelTransactionNo())
                        .channelUserId(notify.getChannelUserId())
                        .channelFeeRate(effectiveFeeRate)
                        .channelFeePrice(effectiveFeePrice)
                        .deliveryStatus(targetDeliveryCode != null ? targetDeliveryCode : order.getDeliveryStatus())
                        .channelNotifyData(notify.getRawData() != null ? JsonUtils.toJsonString(notify.getRawData()) : null)
                        .build());
        if (updateCounts == 0) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        log.info("[updateOrderSuccess][order({}) 更新为已支付] targetFee={} targetDelivery={}",
                order.getId(), effectiveFeePrice, targetDeliveryCode);
        order = orderMapper.selectById(order.getId());
        return PayOrderNotifyResult.of(order, true, deliveryChanged, deliveryStatus);
    }

    private void notifyOrderClosed(PayChannelDO channel, PayOrderInnerRespDTO notify) {
        PayOrderDO order = orderMapper.selectByNo(notify.getOutTradeNo());
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (PayOrderStatusEnum.isClosed(order.getStatus())) {
            log.info("[notifyOrderClosed][order({}) 已经是支付关闭，无需更新]", order.getId());
            return;
        }
        if (PayOrderStatusEnum.isSuccess(order.getStatus())) {
            log.info("[notifyOrderClosed][order({}) 是已支付，无需更新为支付关闭]", order.getId());
            return;
        }
        if (!PayOrderStatusEnum.WAITING.getStatus().equals(order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }

        int updateCounts = orderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
                PayOrderDO.builder().status(PayOrderStatusEnum.CLOSED.getStatus())
                        .channelNotifyData(toJsonString(notify))
                        .channelErrorCode(notify.getChannelErrorCode()).channelErrorMsg(notify.getChannelErrorMsg()).build());
        if (updateCounts == 0) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyOrderClosed][order({}) 更新为支付关闭]", order.getId());
    }

    @Override
    public void updateOrderRefundPrice(Long id, Integer incrRefundPrice) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }
        if (order.getRefundPrice() + incrRefundPrice > order.getPrice()) {
            throw exception(REFUND_PRICE_EXCEED);
        }

        PayOrderDO updateObj = new PayOrderDO()
                .setRefundPrice(order.getRefundPrice() + incrRefundPrice)
                .setStatus(PayOrderStatusEnum.REFUND.getStatus());
        int updateCount = orderMapper.updateByIdAndStatus(id, order.getStatus(), updateObj);
        if (updateCount == 0) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }
    }

    @Override
    public void updatePayOrderPrice(Long id, Integer payPrice) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(PayOrderStatusEnum.WAITING.getStatus(), order.getStatus())) {
            throw exception(PAY_ORDER_STATUS_IS_NOT_WAITING);
        }
        if (ObjectUtil.equal(order.getPrice(), payPrice)) {
            return;
        }
        orderMapper.updateById(new PayOrderDO().setId(order.getId()).setPrice(payPrice));
    }

    @Override
    public int syncOrder(LocalDateTime minCreateTime) {
        List<PayOrderDO> orders = orderMapper.selectListByStatusAndCreateTimeGe(
                PayOrderStatusEnum.WAITING.getStatus(), minCreateTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }
        int count = 0;
        for (PayOrderDO order : orders) {
            count += syncOrder(order) ? 1 : 0;
        }
        return count;
    }

    private boolean syncOrder(PayOrderDO order) {
        try {
            if (order.getChannelId() == null) { // 未提交支付，无需同步
                return false;
            }
            PayClient payClient = channelService.getPayClient(order.getChannelId());
            if (payClient == null) {
                log.error("[syncOrder][渠道编号({}) 找不到对应的支付客户端]", order.getChannelId());
                return false;
            }
            PayOrderInnerRespDTO respDTO = payClient.getOrder(order.getNo());
            if (PayOrderStatusRespEnum.isClosed(respDTO.getStatus())) {
                return false;
            }
            notifyOrder(order.getChannelId(), respDTO);
            return PayOrderStatusRespEnum.isSuccess(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncOrder][order({}) 同步支付状态异常]", order.getId(), e);
            return false;
        }
    }

    @Override
    public int expireOrder() {
        List<PayOrderDO> orders = orderMapper.selectListByStatusAndExpireTimeLt(
                PayOrderStatusEnum.WAITING.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }
        int count = 0;
        for (PayOrderDO order : orders) {
            count += expireOrder(order) ? 1 : 0;
        }
        return count;
    }

    private boolean expireOrder(PayOrderDO order) {
        try {
            if (order.getChannelId() != null) {
                PayClient payClient = channelService.getPayClient(order.getChannelId());
                if (payClient == null) {
                    log.error("[expireOrder][渠道编号({}) 找不到对应的支付客户端]", order.getChannelId());
                    return false;
                }
                PayOrderInnerRespDTO respDTO = payClient.getOrder(order.getNo());
                if (PayOrderStatusRespEnum.isRefund(respDTO.getStatus())) {
                    log.error("[expireOrder][order({}) 的 PayOrderRespDTO({}) 已退款，可能是回调延迟]",
                            order.getId(), toJsonString(respDTO));
                    return false;
                }
                if (PayOrderStatusRespEnum.isSuccess(respDTO.getStatus())) {
                    notifyOrder(order.getChannelId(), respDTO);
                    return false;
                }
            }

            PayOrderDO updateObj = new PayOrderDO().setStatus(PayOrderStatusEnum.CLOSED.getStatus());
            if (orderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateObj) == 0) {
                log.error("[expireOrder][order({}) 更新为支付关闭失败]", order.getId());
                return false;
            }
            log.info("[expireOrder][order({}) 更新为支付关闭成功]", order.getId());
            return true;
        } catch (Throwable e) {
            log.error("[expireOrder][order({}) 过期订单异常]", order.getId(), e);
            return false;
        }
    }

    /**
     * 渠道订单查询：校验支付单、渠道信息并调用客户端获取实时状态。
     */
    @Override
    public PayOrderInnerRespDTO queryChannelOrder(Long id) {
        PayOrderDO order = validateOrderForChannelQuery(id);
        PayClient payClient = channelService.getPayClient(order.getChannelId());
        if (payClient == null) {
            throw exception(PAY_ORDER_CHANNEL_QUERY_UNSUPPORTED);
        }
        try {
            PayOrderInnerRespDTO respDTO = payClient.getOrder(order.getNo());
            if (respDTO == null) {
                throw exception(PAY_ORDER_CHANNEL_QUERY_EMPTY);
            }
            return respDTO;
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("[queryChannelOrder][orderId={}] 渠道查询失败", id, ex);
            throw exception(PAY_ORDER_CHANNEL_QUERY_UNSUPPORTED);
        }
    }

    private PayOrderDO validateOrderForChannelQuery(Long id) {
        PayOrderDO order = orderMapper.selectById(id);
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        if (order.getChannelId() == null || StrUtil.isBlank(order.getNo())) {
            throw exception(PAY_ORDER_CHANNEL_QUERY_UNSUPPORTED);
        }
        return order;
    }

    private PayOrderServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
