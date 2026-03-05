package com.hissp.distribution.module.trade.api.order.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 交易订单发货 Request DTO
 * 用于API层的订单发货操作
 * 
 * @author system
 */
@Data
public class TradeOrderDeliveryReqDTO {

    /**
     * 订单编号
     */
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    /**
     * 发货物流公司编号
     * 如果为空字符串或特殊值，表示无需物流（如虚拟商品）
     */
    private Long logisticsId;

    /**
     * 发货物流单号
     * 如果为空字符串，表示无需物流单号（如虚拟商品）
     */
    private String logisticsNo;

    /**
     * 操作人ID（可选）
     * 用于记录是谁执行的发货操作
     */
    private Long operatorId;

    /**
     * 发货备注（可选）
     */
    private String remark;
}
