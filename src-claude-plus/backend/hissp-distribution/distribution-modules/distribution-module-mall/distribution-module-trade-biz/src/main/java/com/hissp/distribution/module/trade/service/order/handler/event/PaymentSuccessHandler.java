package com.hissp.distribution.module.trade.service.order.handler.event;

import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;

/**
 * 支付成功后对单个订单项的领域处理器
 */
public interface PaymentSuccessHandler {

    /** 是否支持处理该订单项 */
    boolean supports(TradeOrderItemDO item);

    /** 支付成功处理（需自行保证业务幂等） */
    void onPayment(TradeOrderDO order, TradeOrderItemDO item);
}

