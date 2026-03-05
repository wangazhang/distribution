package com.hissp.distribution.module.trade.mq.producer;

import com.hissp.distribution.module.trade.enums.mq.TradeOrderEventTagEnum;
import com.hissp.distribution.module.trade.enums.mq.TradeOrderEventTopicEnum;
import com.hissp.distribution.module.trade.message.TradeOrderChannelShippingMessage;
import com.hissp.distribution.module.trade.message.TradeOrderPaymentSuccessMessage;
import com.hissp.distribution.module.trade.message.TradeOrderRefundSuccessMessage;
import com.hissp.distribution.module.trade.message.TradeOrderVirtualDeliveryMessage;
import com.hissp.distribution.module.trade.constants.TradeOrderIdempotencyConstants;
import com.hissp.distribution.framework.idempotency.utils.IdempotencyUtils;
import com.hissp.distribution.module.trade.mq.monitor.MessageSendMonitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import jakarta.annotation.Resource;

/**
 * 交易订单事件消息生产者
 * 负责发送交易订单相关的业务事件消息
 * 支持同步/异步发送，确保事务一致性
 * 
 * @author system
 */
@Slf4j
@Component
public class TradeOrderEventProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Resource
    private MessageSendMonitor messageSendMonitor;

    /**
     * 发送订单支付成功消息（同步发送，确保事务一致性）
     *
     * @param message 消息内容
     * @param async 是否异步发送，默认false（同步发送）
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendOrderPaymentSuccessMessage(TradeOrderPaymentSuccessMessage message, boolean async) {
        String topic = TradeOrderEventTopicEnum.TRADE_ORDER_EVENTS.getTopic();
        String tag = TradeOrderEventTagEnum.PAYMENT_SUCCESS.getTag();
        String key = IdempotencyUtils.generateSimpleBusinessKey(
            TradeOrderIdempotencyConstants.ORDER_PAYMENT_SUCCESS_PREFIX, message.getOrderId());

        log.info("[sendOrderPaymentSuccessMessage][发送订单支付成功消息: orderId={}, async={}]",
            message.getOrderId(), async);

        return sendMessage(message, topic, tag, key, async, "支付成功");
    }

    /**
     * 统一的消息发送方法（支持泛型）
     *
     * @param message 消息内容
     * @param topic Topic名称
     * @param tag Tag名称
     * @param key 消息Key
     * @param async 是否异步发送
     * @param messageType 消息类型描述（用于日志）
     * @return 发送结果
     */
    private <T> boolean sendMessage(T message, String topic, String tag, String key, boolean async,
                                    String messageType) {
        return sendMessage(message, topic, tag, key, async, messageType, null);
    }

    private <T> boolean sendMessage(T message, String topic, String tag, String key, boolean async,
                                    String messageType, Integer delayLevel) {
        if (async) {
            return sendAsyncWithCallback(message, topic, tag, key, messageType);
        } else {
            return sendSyncWithRetry(message, topic, tag, key, messageType, delayLevel);
        }
    }

    /**
     * 同步发送消息，带重试机制（泛型版本）
     *
     * @param message 消息内容
     * @param topic Topic名称
     * @param tag Tag名称
     * @param key 消息Key
     * @param messageType 消息类型描述
     * @return 发送结果
     */
    private <T> boolean sendSyncWithRetry(T message, String topic, String tag, String key, String messageType,
                                          Integer delayLevel) {
        int maxRetries = 3;
        int retryDelay = 1000; // 1秒

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                Message<T> mqMessage = MessageBuilder
                    .withPayload(message)
                    .setHeader("KEYS", key)
                    .build();

                String destination = topic + ":" + tag;
                SendResult sendResult;
                if (delayLevel == null) {
                    sendResult = rocketMQTemplate.syncSend(destination, mqMessage);
                } else {
                    sendResult = rocketMQTemplate.syncSend(destination, mqMessage, 3000, delayLevel);
                }

                if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                    Long orderId = extractOrderId(message);
                    log.info("[sendSyncWithRetry][{}消息发送成功: orderId={}, msgId={}, attempt={}]",
                        messageType, orderId, sendResult.getMsgId(), attempt);
                    messageSendMonitor.recordSuccess(topic, orderId, sendResult.getMsgId());
                    return true;
                } else {
                    Long orderId = extractOrderId(message);
                    log.warn("[sendSyncWithRetry][{}消息发送失败: orderId={}, sendStatus={}, attempt={}]",
                        messageType, orderId, sendResult.getSendStatus(), attempt);

                    if (attempt < maxRetries) {
                        try {
                            Thread.sleep(retryDelay * attempt); // 递增延迟
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                Long orderId = extractOrderId(message);
                log.error("[sendSyncWithRetry][{}消息发送异常: orderId={}, attempt={}]",
                    messageType, orderId, attempt, e);

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // 最后一次重试失败，执行降级策略
                    return handleSendFailure(message, e, messageType);
                }
            }
        }

        return false;
    }

    /**
     * 异步发送消息，带回调处理（泛型版本）
     *
     * @param message 消息内容
     * @param topic Topic名称
     * @param tag Tag名称
     * @param key 消息Key
     * @param messageType 消息类型描述
     * @return 发送结果
     */
    private <T> boolean sendAsyncWithCallback(T message, String topic, String tag, String key, String messageType) {
        try {
            Message<T> mqMessage = MessageBuilder
                .withPayload(message)
                .setHeader("KEYS", key)
                .build();

            Long orderId = extractOrderId(message);
            String destination = topic + ":" + tag;
            rocketMQTemplate.asyncSend(destination, mqMessage, new SendCallbackImpl(orderId, topic, messageType));

            log.info("[sendAsyncWithCallback][{}消息异步发送提交: orderId={}]", messageType, orderId);
            return true;
        } catch (Exception e) {
            Long orderId = extractOrderId(message);
            log.error("[sendAsyncWithCallback][{}消息异步发送异常: orderId={}]", messageType, orderId, e);
            return handleSendFailure(message, e, messageType);
        }
    }

    /**
     * 统一的发送失败处理策略（泛型版本）
     *
     * @param message 消息内容
     * @param exception 异常信息
     * @param messageType 消息类型描述
     * @return 是否继续执行业务逻辑
     */
    private <T> boolean handleSendFailure(T message, Exception exception, String messageType) {
        String topic = TradeOrderEventTopicEnum.TRADE_ORDER_EVENTS.getTopic();
        String errorMsg = exception.getMessage();
        Long orderId = extractOrderId(message);

        log.error("[handleSendFailure][{}消息发送失败，执行降级策略: orderId={}]",
            messageType, orderId, exception);

        // 记录失败监控数据
        messageSendMonitor.recordFailure(topic, orderId, errorMsg);

        // 降级策略：
        // 1. 记录失败日志，用于后续补偿
        // 2. 可以选择抛出异常回滚事务，或者继续执行（根据业务需求）
        // 3. 可以将消息存储到数据库，后续通过定时任务重试

        // 这里选择抛出异常，确保事务回滚，保证数据一致性
        throw new RuntimeException(messageType + "消息发送失败，事务回滚", exception);
    }

    /**
     * 从消息对象中提取订单ID（支持多种消息类型）
     *
     * @param message 消息对象
     * @return 订单ID
     */
    private <T> Long extractOrderId(T message) {
        if (message instanceof TradeOrderPaymentSuccessMessage) {
            return ((TradeOrderPaymentSuccessMessage) message).getOrderId();
        } else if (message instanceof TradeOrderRefundSuccessMessage) {
            return ((TradeOrderRefundSuccessMessage) message).getOrderId();
        } else if (message instanceof TradeOrderVirtualDeliveryMessage) {
            return ((TradeOrderVirtualDeliveryMessage) message).getOrderId();
        }
        // 可以根据需要扩展其他消息类型
        throw new IllegalArgumentException("不支持的消息类型: " + message.getClass().getSimpleName());
    }

    /**
     * 发送订单支付成功消息（默认同步发送）
     *
     * @param message 消息内容
     * @return 发送结果
     */
    public boolean sendOrderPaymentSuccessMessage(TradeOrderPaymentSuccessMessage message) {
        return sendOrderPaymentSuccessMessage(message, false);
    }

    /**
     * 发送订单退款成功消息（同步发送）
     *
     * @param message 退款成功消息
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendRefundSuccessMessage(TradeOrderRefundSuccessMessage message) {
        String topic = TradeOrderEventTopicEnum.TRADE_ORDER_EVENTS.getTopic();
        String tag = TradeOrderEventTagEnum.REFUND_SUCCESS.getTag();
        String key = IdempotencyUtils.generateCompositeBusinessKey(
            TradeOrderIdempotencyConstants.ORDER_REFUND_SUCCESS_PREFIX, message.getOrderId(), message.getPayRefundId());

        log.info("[sendRefundSuccessMessage][发送订单退款成功消息: orderId={}, payRefundId={}]",
            message.getOrderId(), message.getPayRefundId());

        return sendMessage(message, topic, tag, key, false, "退款成功");
    }

    /**
     * 发送虚拟发货消息（延迟）
     *
     * @param message 虚拟发货消息
     * @param delayLevel RocketMQ 延迟级别
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendVirtualDeliveryMessage(TradeOrderVirtualDeliveryMessage message, int delayLevel) {
        String topic = TradeOrderEventTopicEnum.TRADE_ORDER_EVENTS.getTopic();
        String tag = TradeOrderEventTagEnum.VIRTUAL_DELIVERY.getTag();
        String key = IdempotencyUtils.generateSimpleBusinessKey(
                TradeOrderIdempotencyConstants.ORDER_VIRTUAL_DELIVERY_PREFIX, message.getOrderId());

        log.info("[sendVirtualDeliveryMessage][发送虚拟发货消息: orderId={}, delayLevel={}]", message.getOrderId(), delayLevel);

        return sendMessage(message, topic, tag, key, false, "虚拟发货", delayLevel);
    }

    /**
     * 发送渠道发货消息（延迟）
     *
     * @param message 渠道发货消息
     * @param delayLevel RocketMQ 延迟级别
     * @return 发送结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean sendChannelShippingMessage(TradeOrderChannelShippingMessage message, int delayLevel) {
        String topic = TradeOrderEventTopicEnum.TRADE_ORDER_EVENTS.getTopic();
        String tag = TradeOrderEventTagEnum.CHANNEL_SHIPPING.getTag();
        String key = IdempotencyUtils.generateSimpleBusinessKey(
                TradeOrderIdempotencyConstants.ORDER_CHANNEL_SHIPPING_PREFIX, message.getOrderId());

        log.info("[sendChannelShippingMessage][发送渠道发货消息: orderId={}, delayLevel={}]", message.getOrderId(), delayLevel);

        return sendMessage(message, topic, tag, key, false, "渠道发货", delayLevel);
    }



    /**
     * 异步发送回调实现（支持多种消息类型）
     */
    private class SendCallbackImpl implements org.apache.rocketmq.client.producer.SendCallback {
        private final Long orderId;
        private final String topic;
        private final String messageType;

        public SendCallbackImpl(Long orderId, String topic, String messageType) {
            this.orderId = orderId;
            this.topic = topic;
            this.messageType = messageType;
        }

        @Override
        public void onSuccess(SendResult sendResult) {
            log.info("[SendCallback][{}消息异步发送成功: orderId={}, msgId={}]",
                messageType, orderId, sendResult.getMsgId());
            messageSendMonitor.recordSuccess(topic, orderId, sendResult.getMsgId());
        }

        @Override
        public void onException(Throwable e) {
            log.error("[SendCallback][{}消息异步发送失败: orderId={}]", messageType, orderId, e);
            messageSendMonitor.recordFailure(topic, orderId, e.getMessage());
        }
    }
}
