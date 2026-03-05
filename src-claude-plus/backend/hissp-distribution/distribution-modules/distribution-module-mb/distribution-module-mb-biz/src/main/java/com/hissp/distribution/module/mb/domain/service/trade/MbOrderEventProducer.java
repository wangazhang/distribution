package com.hissp.distribution.module.mb.domain.service.trade;

import com.hissp.distribution.module.mb.message.MbOrderVirtualDeliveryMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * MB 物料订单事件生产者
 */
@Component
@Slf4j
public class MbOrderEventProducer {

    private static final String TOPIC = "mb-order-events";
    private static final String TAG_VIRTUAL_DELIVERY = "virtual-delivery";
    private static final long SEND_TIMEOUT = 3000L;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    public void sendVirtualDeliveryMessage(MbOrderVirtualDeliveryMessage message, int delayLevel) {
        String destination = TOPIC + ":" + TAG_VIRTUAL_DELIVERY;
        String key = "MB_ORDER_VIRTUAL_DELIVERY_" + message.getOrderId();
        Message<MbOrderVirtualDeliveryMessage> mqMessage = MessageBuilder
                .withPayload(message)
                .setHeader(RocketMQHeaders.KEYS, key)
                .build();
        try {
            rocketMQTemplate.syncSend(destination, mqMessage, SEND_TIMEOUT, delayLevel);
            log.info("[MbOrderEventProducer][虚拟发货消息发送成功 orderId={} delayLevel={}]", message.getOrderId(), delayLevel);
        } catch (Exception ex) {
            log.error("[MbOrderEventProducer][虚拟发货消息发送失败 orderId={}]", message.getOrderId(), ex);
            throw new RuntimeException("发送MB虚拟发货消息失败", ex);
        }
    }
}
