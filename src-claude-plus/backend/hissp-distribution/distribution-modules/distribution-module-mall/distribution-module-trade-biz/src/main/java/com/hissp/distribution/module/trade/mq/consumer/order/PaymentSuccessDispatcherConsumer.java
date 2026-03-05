package com.hissp.distribution.module.trade.mq.consumer.order;

import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderItemMapper;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderMapper;
import com.alibaba.fastjson.JSON;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderItemMapper;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderMapper;
import com.hissp.distribution.module.trade.message.TradeOrderPaymentSuccessMessage;
import com.hissp.distribution.module.trade.service.order.handler.event.TradeOrderEventProcessService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 支付成功统一分发器：按订单项分发到领域处理器
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "trade-order-events",
        selectorExpression = "payment-success",
        consumerGroup = "trade-payment-success-dispatcher"
)
public class PaymentSuccessDispatcherConsumer implements RocketMQListener<MessageExt> {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeOrderEventProcessService processService;

    @Override
    public void onMessage(MessageExt messageExt) {
        String msgId = messageExt.getMsgId();
        String body = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        TradeOrderPaymentSuccessMessage message = JSON.parseObject(body, TradeOrderPaymentSuccessMessage.class);

        try {
            TradeOrderDO order = tradeOrderMapper.selectById(message.getOrderId());
            if (order == null) {
                log.warn("[onMessage][order({}) not found]", message.getOrderId());
                return;
            }
            List<TradeOrderItemDO> items = tradeOrderItemMapper.selectListByOrderId(order.getId());
            for (TradeOrderItemDO item : items) {
                processService.processPaymentItem(order, item, msgId);
            }
        } catch (Exception e) {
            log.error("[onMessage][payment-success 分发异常 msgId={}, message={}]", msgId, message, e);
            throw e; // 抛出异常，触发 RocketMQ 重试
        }
    }
}

