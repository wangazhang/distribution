package com.hissp.distribution.module.trade.mq.consumer.order;

import com.alibaba.fastjson.JSON;
import com.hissp.distribution.module.trade.message.TradeOrderVirtualDeliveryMessage;
import com.hissp.distribution.module.trade.service.order.handler.ext.VirtualGoodsDeliveryExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 虚拟发货消息消费者
 *
 * <p>延迟拉起虚拟商品的渠道发货请求，规避微信支付后立即发货的限制。</p>
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "trade-order-events",
        selectorExpression = "virtual-delivery",
        consumerGroup = "trade-virtual-delivery-dispatcher"
)
public class VirtualDeliveryDispatcherConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private VirtualGoodsDeliveryExecutor virtualGoodsDeliveryExecutor;

    @Override
    public void onMessage(MessageExt messageExt) {
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        TradeOrderVirtualDeliveryMessage message = JSON.parseObject(body, TradeOrderVirtualDeliveryMessage.class);
        try {
            virtualGoodsDeliveryExecutor.execute(message.getOrderId());
        } catch (Exception ex) {
            log.error("[onMessage][虚拟发货处理失败 msgId={}, message={}]", messageExt.getMsgId(), message, ex);
            throw ex;
        }
    }
}
