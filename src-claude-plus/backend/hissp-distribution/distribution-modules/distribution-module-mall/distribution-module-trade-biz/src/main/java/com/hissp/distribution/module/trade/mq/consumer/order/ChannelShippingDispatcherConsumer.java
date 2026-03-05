package com.hissp.distribution.module.trade.mq.consumer.order;

import com.alibaba.fastjson.JSON;
import com.hissp.distribution.module.trade.message.TradeOrderChannelShippingMessage;
import com.hissp.distribution.module.trade.service.order.TradeOrderUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * 渠道发货消息消费者
 *
 * <p>延迟拉起实物订单的渠道发货请求，确保满足微信发货时间要求。</p>
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "trade-order-events",
        selectorExpression = "channel-shipping",
        consumerGroup = "trade-channel-shipping-dispatcher"
)
public class ChannelShippingDispatcherConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private TradeOrderUpdateService tradeOrderUpdateService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        TradeOrderChannelShippingMessage message = JSON.parseObject(body, TradeOrderChannelShippingMessage.class);
        try {
            tradeOrderUpdateService.triggerChannelShipping(message.getOrderId());
        } catch (Exception ex) {
            log.error("[onMessage][渠道发货处理失败 msgId={}, message={}]", messageExt.getMsgId(), message, ex);
            throw ex;
        }
    }
}
