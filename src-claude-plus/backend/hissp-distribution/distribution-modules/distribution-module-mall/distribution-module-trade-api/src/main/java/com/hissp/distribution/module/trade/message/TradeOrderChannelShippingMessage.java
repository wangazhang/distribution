package com.hissp.distribution.module.trade.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 渠道发货消息
 *
 * <p>用于延迟触发实物订单的渠道发货，满足微信支付发货时间限制。</p>
 */
@Data
@Accessors(chain = true)
public class TradeOrderChannelShippingMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易订单编号
     */
    private Long orderId;

    /**
     * 支付订单编号
     */
    private Long payOrderId;
}
