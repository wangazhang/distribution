package com.hissp.distribution.module.trade.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 交易订单退款成功消息
 * 当订单退款成功时发布此消息，供各业务模块订阅处理
 *
 * @author system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeOrderRefundSuccessMessage {

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 支付退款ID
     */
    private Long payRefundId;

    /**
     * 商户订单号
     */
    private String merchantOrderId;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;
}
