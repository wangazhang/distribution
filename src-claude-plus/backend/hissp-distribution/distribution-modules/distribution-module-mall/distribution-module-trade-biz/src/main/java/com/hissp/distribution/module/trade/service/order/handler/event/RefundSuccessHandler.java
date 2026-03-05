package com.hissp.distribution.module.trade.service.order.handler.event;

import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;

/**
 * 退款成功后对单个订单项的领域处理器
 */
public interface RefundSuccessHandler {

    /** 是否支持处理该订单项 */
    boolean supports(TradeOrderItemDO item);

    /** 退款成功处理（需自行保证业务幂等） */
    void onRefund(TradeOrderDO order, TradeOrderItemDO item);
}

