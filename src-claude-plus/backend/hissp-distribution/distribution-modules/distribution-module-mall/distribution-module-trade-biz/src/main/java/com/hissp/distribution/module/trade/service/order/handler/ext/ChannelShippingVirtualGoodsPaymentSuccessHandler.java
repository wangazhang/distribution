package com.hissp.distribution.module.trade.service.order.handler.ext;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.module.pay.api.order.PayOrderApi;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import com.hissp.distribution.module.product.enums.spu.ProductSpuTypeEnum;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.message.TradeOrderVirtualDeliveryMessage;
import com.hissp.distribution.module.trade.mq.producer.TradeOrderEventProducer;
import com.hissp.distribution.module.trade.service.order.handler.event.PaymentSuccessHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 渠道虚拟商品发货处理器
 *
 * <p>订单支付成功后，针对虚拟商品自动推送渠道发货，目前仅支持微信。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelShippingVirtualGoodsPaymentSuccessHandler implements PaymentSuccessHandler {

    private static final int ROCKETMQ_DELAY_LEVEL_1_MINUTE = 5;

    private final PayOrderApi payOrderApi;
    private final VirtualGoodsDeliveryExecutor virtualGoodsDeliveryExecutor;
    private final TradeOrderEventProducer tradeOrderEventProducer;

    @Override
    public boolean supports(TradeOrderItemDO item) {
        return ProductSpuTypeEnum.isVirtual(item.getProductType());
    }

    @Override
    public void onPayment(TradeOrderDO order, TradeOrderItemDO item) {
        if (order.getPayOrderId() == null) {
            log.warn("[onPayment][orderId={}] 缺少支付订单编号，无法推送渠道虚拟发货", order.getId());
            return;
        }
        if (StrUtil.isBlank(order.getPayChannelCode())
                || !StrUtil.containsIgnoreCase(order.getPayChannelCode(), "wx")) {
            return;
        }

        PayOrderRespDTO payOrder = payOrderApi.getOrder(order.getPayOrderId());
        if (payOrder == null) {
            log.error("[onPayment][orderId={}] 未查询到支付单({})，停止虚拟发货", order.getId(), order.getPayOrderId());
            return;
        }

        if (needsDelay(payOrder)) {
            TradeOrderVirtualDeliveryMessage message = new TradeOrderVirtualDeliveryMessage()
                    .setOrderId(order.getId())
                    .setPayOrderId(order.getPayOrderId());
            tradeOrderEventProducer.sendVirtualDeliveryMessage(message, ROCKETMQ_DELAY_LEVEL_1_MINUTE);
            log.info("[onPayment][orderId={}] 支付完成未满一分钟，提交延迟虚拟发货消息", order.getId());
            return;
        }

        try {
            virtualGoodsDeliveryExecutor.execute(order.getId(), payOrder);
        } catch (Exception ex) {
            log.error("[onPayment][orderId={}] 推送渠道虚拟发货失败", order.getId(), ex);
        }
    }

    private boolean needsDelay(PayOrderRespDTO payOrder) {
        LocalDateTime successTime = payOrder.getSuccessTime();
        if (successTime == null) {
            return true;
        }
        Duration duration = Duration.between(successTime, LocalDateTime.now());
        return duration.toSeconds() < 60;
    }
}
