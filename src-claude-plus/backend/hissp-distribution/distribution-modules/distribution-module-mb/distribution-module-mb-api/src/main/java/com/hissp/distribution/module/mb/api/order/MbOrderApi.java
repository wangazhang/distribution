package com.hissp.distribution.module.mb.api.order;

import com.hissp.distribution.module.mb.api.order.dto.MbOrderSimpleRespDTO;

/**
 * MB 订单对外查询 API
 *
 * @author codex
 */
public interface MbOrderApi {

    /**
     * 根据订单编号获取简要信息
     *
     * @param orderId 订单编号
     * @return 订单摘要，查无数据时返回 {@code null}
     */
    MbOrderSimpleRespDTO getOrderSummary(Long orderId);

}
