package com.hissp.distribution.module.mb.enums.mq;

/**
 * MB业务事件Topic枚举
 * 基于DDD领域边界设计，按照业务领域划分Topic
 * 
 * @author system
 */
public enum MbBizEventTopicEnum {

    /**
     * MB业务事件主题
     * 包含MB模块相关的所有业务事件
     */
    MB_BUSINESS_EVENTS("mb-business-events", "MB业务事件主题");

    /**
     * Topic名称
     */
    private final String topic;

    /**
     * Topic描述
     */
    private final String description;

    MbBizEventTopicEnum(String topic, String description) {
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
