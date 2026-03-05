package com.hissp.distribution.module.trade.enums.mq;

/**
 * 交易订单事件Tag枚举
 * 按照具体业务事件类型设计Tag，用于消息过滤和路由
 * 
 * @author system
 */
public enum TradeOrderEventTagEnum {

    /**
     * 订单支付成功事件
     */
    PAYMENT_SUCCESS("payment-success", "订单支付成功"),

    /**
     * 订单退款成功事件
     */
    REFUND_SUCCESS("refund-success", "订单退款成功"),

    /**
     * 生涯产品购买事件
     */
    CAREER_PRODUCT_PURCHASE("career-product-purchase", "生涯产品购买"),

    /**
     * 普通产品购买事件
     */
    NORMAL_PRODUCT_PURCHASE("normal-product-purchase", "普通产品购买"),

    /**
     * 订单取消事件
     */
    ORDER_CANCEL("order-cancel", "订单取消"),

    /**
     * 订单发货事件
     */
    ORDER_DELIVERY("order-delivery", "订单发货"),

    /**
     * 渠道发货事件
     */
    CHANNEL_SHIPPING("channel-shipping", "渠道发货"),

    /**
     * 订单完成事件
     */
    ORDER_COMPLETE("order-complete", "订单完成"),

    /**
     * 虚拟发货事件
     */
    VIRTUAL_DELIVERY("virtual-delivery", "虚拟发货");

    /**
     * Tag名称
     */
    private final String tag;

    /**
     * Tag描述
     */
    private final String description;

    TradeOrderEventTagEnum(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }
}
