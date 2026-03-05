package com.hissp.distribution.module.trade.api.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 渠道回传的订单收货/结算信息
 */
@Data
public class TradeOrderChannelReceiveReqDTO implements Serializable {

    /** 渠道标识 */
    private String channelCode;

    /** 渠道事件或类型 */
    private String event;

    /** 交易订单编号，可选 */
    private Long orderId;

    /** 支付订单编号，可选 */
    private Long payOrderId;

    /** 商户侧订单号 */
    private String merchantTradeNo;

    /** 渠道妥投方式：1-手动，2-自动，可为空 */
    private Integer confirmReceiveMethod;

    /** 渠道妥投时间 */
    private LocalDateTime confirmReceiveTime;

    /** 渠道结算时间（部分渠道可能回传） */
    private LocalDateTime settlementTime;

    /** 渠道原始载荷 */
    private String rawPayload;
}
