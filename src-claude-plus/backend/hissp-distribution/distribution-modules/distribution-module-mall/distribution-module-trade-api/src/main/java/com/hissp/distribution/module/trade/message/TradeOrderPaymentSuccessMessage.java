package com.hissp.distribution.module.trade.message;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 交易订单支付成功消息
 * 精简版消息，只包含关键标识信息，业务数据在消费时重新查询
 *
 * 设计原则：
 * 1. 消息只包含最关键的标识信息
 * 2. 业务数据在消费时通过API重新查询获取
 * 3. 确保数据的实时性和一致性
 * 4. 减少消息体积，提高传输效率
 *
 * @author system
 */
@Data
public class TradeOrderPaymentSuccessMessage {

    /**
     * 消息版本，用于向后兼容
     */
    @NotNull(message = "消息版本不能为空")
    private String version = "2.0";

    /**
     * 订单ID - 核心标识
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 用户ID - 核心标识
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 支付时间 - 事件时间戳
     */
    private LocalDateTime payTime;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime = LocalDateTime.now();
}
