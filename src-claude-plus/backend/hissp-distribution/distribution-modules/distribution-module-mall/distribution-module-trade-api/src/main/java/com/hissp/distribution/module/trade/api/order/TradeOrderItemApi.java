package com.hissp.distribution.module.trade.api.order;

import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 订单项 API 接口
 *
 * @author system
 */
public interface TradeOrderItemApi {

    /**
     * 获得指定订单的订单项列表
     *
     * @param orderId 订单编号
     * @return 订单项列表
     */
    List<TradeOrderItemRespDTO> getOrderItemListByOrderId(Long orderId);

    /**
     * 获得订单项列表
     *
     * @param ids 订单项编号数组
     * @return 订单项列表
     */
    List<TradeOrderItemRespDTO> getOrderItemList(Collection<Long> ids);

} 