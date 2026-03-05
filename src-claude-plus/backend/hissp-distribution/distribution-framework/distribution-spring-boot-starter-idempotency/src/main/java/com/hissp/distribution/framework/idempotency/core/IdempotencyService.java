package com.hissp.distribution.framework.idempotency.core;

/**
 * MQ消息幂等性服务接口
 * 用于确保消息重复消费时不会产生副作用
 *
 * 优化版本：
 * 1. 简化接口，减少冗余参数
 * 2. 基于业务键的幂等性检查
 * 3. 支持批量操作，提升性能
 *
 * @author system
 */
public interface IdempotencyService {

    /**
     * 检查并记录消息处理（简化版本）
     * 基于业务键进行幂等性检查，自动生成消息ID
     *
     * @param businessKey 业务唯一键
     * @param consumerGroup 消费者组名称
     * @return true-可以处理, false-已处理过
     */
    boolean checkAndRecord(String businessKey, String consumerGroup);

    /**
     * 检查并记录消息处理（包含topic的简化版本）
     * 基于业务键进行幂等性检查，自动生成消息ID，并记录topic信息
     *
     * @param businessKey 业务唯一键
     * @param consumerGroup 消费者组名称
     * @param topic 主题名称
     * @return true-可以处理, false-已处理过
     */
    boolean checkAndRecordWithTopic(String businessKey, String consumerGroup, String topic);

    /**
     * 检查并记录消息处理（完整版本）
     * 如果消息已经处理过，返回false；否则记录处理状态并返回true
     *
     * @param messageId 消息唯一标识
     * @param consumerGroup 消费者组名称
     * @param businessKey 业务唯一键
     * @return true-可以处理, false-已处理过
     */
    boolean checkAndRecord(String messageId, String consumerGroup, String businessKey);

    /**
     * 检查并记录消息处理（完整版本，包含topic）
     * 如果消息已经处理过，返回false；否则记录处理状态并返回true
     *
     * @param messageId 消息唯一标识
     * @param consumerGroup 消费者组名称
     * @param businessKey 业务唯一键
     * @param topic 主题名称
     * @return true-可以处理, false-已处理过
     */
    boolean checkAndRecordWithTopic(String messageId, String consumerGroup, String businessKey, String topic);

    /**
     * 标记消息处理成功（基于业务键）
     *
     * @param businessKey 业务唯一键
     * @param consumerGroup 消费者组名称
     */
    void markSuccess(String businessKey, String consumerGroup);

    /**
     * 标记消息处理成功（基于消息ID）
     *
     * @param messageId 消息唯一标识
     * @param consumerGroup 消费者组名称
     */
    void markSuccessById(String messageId, String consumerGroup);

    /**
     * 标记消息处理失败（基于业务键）
     *
     * @param businessKey 业务唯一键
     * @param consumerGroup 消费者组名称
     * @param errorMsg 错误信息
     */
    void markFailed(String businessKey, String consumerGroup, String errorMsg);

    /**
     * 标记消息处理失败（基于消息ID）
     *
     * @param messageId 消息唯一标识
     * @param consumerGroup 消费者组名称
     * @param errorMsg 错误信息
     */
    void markFailedById(String messageId, String consumerGroup, String errorMsg);

    /**
     * 检查业务是否已处理
     * 基于业务唯一键检查，用于业务级别的幂等性判断
     *
     * @param businessKey 业务唯一键
     * @param consumerGroup 消费者组名称
     * @return true-已处理, false-未处理
     */
    boolean isBusinessProcessed(String businessKey, String consumerGroup);

    /**
     * 清理过期的幂等性记录
     * 定期清理超过指定时间的记录，避免数据过多
     *
     * @param expireDays 过期天数
     * @return 清理的记录数
     */
    int cleanExpiredRecords(int expireDays);
}
