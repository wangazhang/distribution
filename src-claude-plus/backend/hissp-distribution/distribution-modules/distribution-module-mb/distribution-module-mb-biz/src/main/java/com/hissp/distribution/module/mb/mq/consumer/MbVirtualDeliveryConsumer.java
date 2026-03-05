package com.hissp.distribution.module.mb.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.hissp.distribution.module.mb.domain.service.trade.MbVirtualDeliveryService;
import com.hissp.distribution.module.mb.message.MbOrderVirtualDeliveryMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * MB 订单虚拟发货消息消费者
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "mb-order-events",
        selectorExpression = "virtual-delivery",
        consumerGroup = "mb-virtual-delivery-dispatcher"
)
public class MbVirtualDeliveryConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private MbVirtualDeliveryService mbVirtualDeliveryService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        MbOrderVirtualDeliveryMessage message = JSON.parseObject(body, MbOrderVirtualDeliveryMessage.class);
        try {
            mbVirtualDeliveryService.executeVirtualDelivery(message.getOrderId());
        } catch (Exception ex) {
            log.error("[MbVirtualDeliveryConsumer][虚拟发货处理失败 msgId={}, message={}]", messageExt.getMsgId(), message, ex);
            throw ex;
        }
    }
}
