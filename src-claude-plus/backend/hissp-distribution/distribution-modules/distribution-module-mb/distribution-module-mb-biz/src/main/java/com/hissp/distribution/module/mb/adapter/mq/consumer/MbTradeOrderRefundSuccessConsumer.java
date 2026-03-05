package com.hissp.distribution.module.mb.adapter.mq.consumer;

import com.hissp.distribution.framework.idempotency.core.IdempotencyService;
import com.hissp.distribution.framework.idempotency.utils.IdempotencyUtils;
import com.hissp.distribution.module.trade.constants.TradeOrderIdempotencyConstants;
import com.hissp.distribution.module.mb.domain.service.trade.refund.MbOrderRefundReverseOperationService;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import com.hissp.distribution.module.trade.message.TradeOrderRefundSuccessMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * Trade订单退款成功消息消费者
 * 处理Trade系统中MB相关商品的退款分佣回退逻辑
 *
 * @author system
 */

@Slf4j
@RocketMQMessageListener(
    topic = "trade-order-events",
    selectorExpression = "refund-success",
    consumerGroup = "mb_trade_order_refund_success_consumer_group"
)
public class MbTradeOrderRefundSuccessConsumer implements RocketMQListener<TradeOrderRefundSuccessMessage> {

    public static final String CONSUMER_GROUP = "mb_trade_order_refund_success_consumer_group";
    public static final String TOPIC = "trade-order-events";

    @Resource
    private IdempotencyService idempotencyService;

    @Resource
    private MbOrderRefundReverseOperationService mbOrderRefundReverseOperationService;

    @Override
    public void onMessage(TradeOrderRefundSuccessMessage message) {
        // 使用优化后的幂等性服务，基于业务键进行检查
        String businessKey = generateBusinessKey(message.getOrderId(), message.getPayRefundId());

        log.info("[onMessage][收到Trade订单退款成功消息: orderId={}, payRefundId={}, businessKey={}]",
            message.getOrderId(), message.getPayRefundId(), businessKey);

        // 幂等性检查
        if (!idempotencyService.checkAndRecordWithTopic(businessKey, CONSUMER_GROUP, TOPIC)) {
            log.info("[onMessage][消息已处理过，跳过处理: orderId={}, payRefundId={}]", 
                message.getOrderId(), message.getPayRefundId());
            return;
        }

        try {
            // 处理Trade订单退款成功业务逻辑
            processTradeOrderRefundSuccess(message);

            // 标记处理成功
            idempotencyService.markSuccess(businessKey, CONSUMER_GROUP);
            log.info("[onMessage][处理Trade订单退款成功消息完成: orderId={}, payRefundId={}]", 
                message.getOrderId(), message.getPayRefundId());
        } catch (Exception e) {
            // 标记处理失败
            String errorMsg = IdempotencyUtils.truncateErrorMsg(e.getMessage(), 500);
            idempotencyService.markFailed(businessKey, CONSUMER_GROUP, errorMsg);
            log.error("[onMessage][处理Trade订单退款成功消息失败: orderId={}, payRefundId={}]", 
                message.getOrderId(), message.getPayRefundId(), e);
            throw e; // 重新抛出异常，让RocketMQ进行重试
        }
    }

    /**
     * 处理Trade订单退款成功业务逻辑
     *
     * @param message 退款成功消息
     */
    private void processTradeOrderRefundSuccess(TradeOrderRefundSuccessMessage message) {
        try {
            // 构建退款通知DTO（兼容现有的处理逻辑）
            PayRefundNotifyReqDTO notifyReqDTO = PayRefundNotifyReqDTO.builder()
                .merchantOrderId(message.getMerchantOrderId())
                .payRefundId(message.getPayRefundId())
                .build();

            log.info("[processTradeOrderRefundSuccess][开始处理Trade订单退款: orderId={}, orderNo={}]",
                message.getOrderId(), message.getOrderNo());

            // 调用现有的退款逆操作服务处理
            boolean result = mbOrderRefundReverseOperationService.processRefundNotification(notifyReqDTO);

            if (result) {
                log.info("[processTradeOrderRefundSuccess][Trade订单退款处理成功: orderId={}]", 
                    message.getOrderId());
            } else {
                log.warn("[processTradeOrderRefundSuccess][Trade订单退款处理失败或跳过: orderId={}]", 
                    message.getOrderId());
            }

        } catch (Exception e) {
            log.error("[processTradeOrderRefundSuccess][处理Trade订单退款异常: orderId={}]", 
                message.getOrderId(), e);
            throw e;
        }
    }

    /**
     * 生成业务键
     *
     * @param orderId 订单ID
     * @param payRefundId 支付退款ID
     * @return 业务键
     */
    private String generateBusinessKey(Long orderId, Long payRefundId) {
        return IdempotencyUtils.generateCompositeBusinessKey(
            TradeOrderIdempotencyConstants.ORDER_REFUND_SUCCESS_PREFIX, orderId, payRefundId);
    }
}
