package com.hissp.distribution.module.trade.convert;

import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.message.TradeOrderPaymentSuccessMessage;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单事件消息转换器
 * 精简版：只转换关键标识信息，业务数据在消费时重新查询
 *
 * @author system
 */
@Component
public class TradeOrderEventConvert {


    /**
     * 转换为订单支付成功消息（精简版本）
     * 只包含核心标识信息，业务数据在消费时重新查询
     *
     * @param order 订单信息
     * @param payTime 支付时间
     * @return 支付成功消息
     */
    public TradeOrderPaymentSuccessMessage convertToPaymentSuccessMessage(TradeOrderDO order, LocalDateTime payTime) {
        TradeOrderPaymentSuccessMessage message = new TradeOrderPaymentSuccessMessage();
        message.setOrderId(order.getId());
        message.setUserId(order.getUserId());
        message.setPayTime(payTime);
        message.setEventTime(LocalDateTime.now());
        return message;
    }
}
