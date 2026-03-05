package com.hissp.distribution.module.trade.constants;

/**
 * Trade模块幂等性业务键常量
 * 纯常量定义，不包含任何方法调用，实现真正的解耦
 *
 * @author system
 */
public class TradeOrderIdempotencyConstants {

    // ==================== 业务键前缀常量 ====================

    /**
     * 订单支付成功业务键前缀
     */
    public static final String ORDER_PAYMENT_SUCCESS_PREFIX = "order_payment_success";

    /**
     * 订单退款成功业务键前缀
     */
    public static final String ORDER_REFUND_SUCCESS_PREFIX = "trade_order_refund_success";

    /**
     * 虚拟发货业务键前缀
     */
    public static final String ORDER_VIRTUAL_DELIVERY_PREFIX = "trade_order_virtual_delivery";

    /**
     * 渠道发货业务键前缀
     */
    public static final String ORDER_CHANNEL_SHIPPING_PREFIX = "trade_order_channel_shipping";

    // 私有构造函数，防止实例化
    private TradeOrderIdempotencyConstants() {
    }
}
