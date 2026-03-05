package com.hissp.distribution.module.trade.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 虚拟商品发货消息
 *
 * <p>用于延迟触发虚拟商品的渠道发货请求，避免支付完成后立即调用被微信拒绝。</p>
 */
@Data
@Accessors(chain = true)
public class TradeOrderVirtualDeliveryMessage implements Serializable {

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
