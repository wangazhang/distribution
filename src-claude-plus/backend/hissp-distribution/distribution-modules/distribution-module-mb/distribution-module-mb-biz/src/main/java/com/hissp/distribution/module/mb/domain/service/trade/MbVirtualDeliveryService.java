package com.hissp.distribution.module.mb.domain.service.trade;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.module.fulfillmentchannel.api.ChannelShippingApi;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingLogisticsTypeEnum;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.mb.constants.MbOrderBizType;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbOrderMapper;
import com.hissp.distribution.module.mb.enums.MbOrderStatusEnum;
import com.hissp.distribution.module.mb.message.MbOrderVirtualDeliveryMessage;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import com.hissp.distribution.module.product.enums.spu.ProductSpuTypeEnum;
import com.hissp.distribution.module.pay.api.order.PayOrderApi;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Locale;

/**
 * MB物料订单虚拟发货服务
 *
 * <p>负责发布延迟发货消息以及真正执行渠道发货。</p>
 */
@Service
@Slf4j
public class MbVirtualDeliveryService {

    private static final int ROCKETMQ_DELAY_LEVEL_1_MINUTE = 5;

    @Resource
    private MbOrderMapper mbOrderMapper;
    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private ChannelShippingApi channelShippingApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private MbOrderEventProducer mbOrderEventProducer;

    /**
     * 发送延迟虚拟发货消息
     *
     * @param orderId MB订单ID
     */
    public void scheduleVirtualDelivery(Long orderId) {
        if (orderId == null) {
            return;
        }
        MbOrderVirtualDeliveryMessage message = new MbOrderVirtualDeliveryMessage().setOrderId(orderId);
        mbOrderEventProducer.sendVirtualDeliveryMessage(message, ROCKETMQ_DELAY_LEVEL_1_MINUTE);
    }

    /**
     * 执行渠道虚拟发货
     *
     * @param orderId MB订单ID
     */
    public void executeVirtualDelivery(Long orderId) {
        try {
            MbOrderDO order = mbOrderMapper.selectById(orderId);
            if (order == null) {
                log.warn("[executeVirtualDelivery][orderId={}] 订单不存在，忽略发货", orderId);
                return;
            }
            if (StrUtil.isBlank(order.getPaymentId())) {
                log.warn("[executeVirtualDelivery][orderId={}] 支付单编号为空，无法推送渠道发货", orderId);
                return;
            }

            Long payOrderId;
            try {
                payOrderId = Long.valueOf(order.getPaymentId());
            } catch (NumberFormatException ex) {
                log.error("[executeVirtualDelivery][orderId={}] 支付单编号非法: {}", orderId, order.getPaymentId(), ex);
                return;
            }

            PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
            if (payOrder == null) {
                log.warn("[executeVirtualDelivery][orderId={}] 未查询到支付单({})，跳过发货", orderId, payOrderId);
                return;
            }
            if (StrUtil.isBlank(payOrder.getChannelCode())
                    || !StrUtil.containsIgnoreCase(payOrder.getChannelCode(), "wx")) {
                log.info("[executeVirtualDelivery][orderId={}] 支付渠道非微信，无需虚拟发货", orderId);
                return;
            }

            ChannelShippingRequestDTO request = buildShippingRequest(order, payOrderId, payOrder);
            if (request == null) {
                log.warn("[executeVirtualDelivery][orderId={}] 构建发货请求失败", orderId);
                return;
            }
            channelShippingApi.ship(request);
            log.info("[executeVirtualDelivery][orderId={}] 已推送渠道虚拟发货", orderId);

            // 渠道发货指令推送成功，补齐订单发货状态
            markOrderDelivered(order);
        } catch (Exception ex) {
            log.error("[executeVirtualDelivery][orderId={}] 渠道虚拟发货执行失败", orderId, ex);
            throw ex;
        }
    }

    private ChannelShippingRequestDTO buildShippingRequest(MbOrderDO order, Long payOrderId, PayOrderRespDTO payOrder) {
        ChannelShippingRequestDTO request = new ChannelShippingRequestDTO();
        request.setOrderId(order.getId());
        request.setOrderNo(StrUtil.blankToDefault(payOrder.getMerchantOrderId(), "MB-" + order.getId()));
        request.setPayOrderId(payOrderId);
        request.setUserId(order.getAgentUserId());
        request.setLogisticsType(ChannelShippingLogisticsTypeEnum.VIRTUAL_GOODS.getType());
        request.setAllDelivered(Boolean.TRUE);
        request.setRemark("MB物料订单自动虚拟发货");

        Long productId = order.getProductId();
        if (StrUtil.equalsIgnoreCase(order.getBizType(), MbOrderBizType.MATERIAL_CONVERT)
                || StrUtil.equalsIgnoreCase(order.getBizType(), MbOrderBizType.COLLAGEN_CONVERT)) {
            MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
            if (definition != null && definition.getConvertedSpuId() != null) {
                productId = definition.getConvertedSpuId();
            }
        }

        ProductSpuRespDTO product = productSpuApi.getSpu(productId);
        ChannelShippingRequestDTO.Item item = new ChannelShippingRequestDTO.Item();
        item.setOrderItemId(order.getId());
        item.setSpuId(productId);
        item.setSpuName(product != null ? product.getName() : "物料订单");
        item.setProductType(product != null && product.getType() != null
                ? product.getType() : ProductSpuTypeEnum.VIRTUAL.getType());
        item.setCount(order.getQuantity() != null ? order.getQuantity() : 1);
        request.setItems(Collections.singletonList(item));
        request.setItemDesc(item.getSpuName());
        return request;
    }

    /**
     * 虚拟发货成功后尝试将订单从 PROCESSING 切换到 DELIVERED
     *
     * @param order 当前订单
     */
    private void markOrderDelivered(MbOrderDO order) {
        if (order == null || StrUtil.isBlank(order.getStatus())) {
            return;
        }
        MbOrderStatusEnum statusEnum;
        try {
            statusEnum = MbOrderStatusEnum.fromCode(order.getStatus().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            log.warn("[markOrderDelivered][orderId={}] 解析订单状态失败: {}", order != null ? order.getId() : null,
                    order != null ? order.getStatus() : null, ex);
            return;
        }
        if (!MbOrderStatusEnum.PROCESSING.equals(statusEnum)) {
            return;
        }

        LocalDateTime deliveryTime = order.getDeliveryTime() != null ? order.getDeliveryTime() : LocalDateTime.now();
        MbOrderDO updateObj = new MbOrderDO()
                .setStatus(MbOrderStatusEnum.DELIVERED.getCode())
                .setDeliveryTime(deliveryTime);
        int updated = mbOrderMapper.updateByIdAndStatus(order.getId(), statusEnum.getCode(), updateObj);
        if (updated == 0) {
            log.debug("[markOrderDelivered][orderId={}] 状态已变化，跳过虚拟发货状态同步", order.getId());
            return;
        }

        order.setStatus(updateObj.getStatus());
        order.setDeliveryTime(deliveryTime);
        log.info("[markOrderDelivered][orderId={}] 虚拟发货完成，状态切换为 DELIVERED", order.getId());
    }
}
