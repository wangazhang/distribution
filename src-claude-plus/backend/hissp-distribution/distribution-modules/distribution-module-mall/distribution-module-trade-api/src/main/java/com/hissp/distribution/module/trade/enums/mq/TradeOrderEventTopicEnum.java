package com.hissp.distribution.module.trade.enums.mq;

/**
 * 交易订单事件Topic枚举
 * 基于DDD领域边界设计，按照业务领域划分Topic
 * 
 * @author system
 */
public enum TradeOrderEventTopicEnum {

    /**
     * 交易订单事件主题
     * 包含订单生命周期中的所有事件
     */
    TRADE_ORDER_EVENTS("trade-order-events", "交易订单事件主题");

    /**
     * Topic名称
     */
    private final String topic;

    /**
     * Topic描述
     */
    private final String description;

    TradeOrderEventTopicEnum(String topic, String description) {
        this.topic = topic;
        this.description = description;
    }

    public String getTopic() {
        return topic;
    }

    public String getDescription() {
        return description;
    }
}
