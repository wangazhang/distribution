package com.hissp.distribution.module.trade.mq.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 消息发送状态监控器
 * 用于监控消息发送的成功率、失败率等指标
 * 
 * @author system
 */
@Slf4j
@Component
public class MessageSendMonitor {

    /**
     * 发送成功计数器
     */
    private final AtomicLong successCount = new AtomicLong(0);

    /**
     * 发送失败计数器
     */
    private final AtomicLong failureCount = new AtomicLong(0);

    /**
     * 按Topic统计的发送成功计数
     */
    private final ConcurrentHashMap<String, AtomicLong> topicSuccessCount = new ConcurrentHashMap<>();

    /**
     * 按Topic统计的发送失败计数
     */
    private final ConcurrentHashMap<String, AtomicLong> topicFailureCount = new ConcurrentHashMap<>();

    /**
     * 记录消息发送成功
     *
     * @param topic Topic名称
     * @param orderId 订单ID
     * @param msgId 消息ID
     */
    public void recordSuccess(String topic, Long orderId, String msgId) {
        successCount.incrementAndGet();
        topicSuccessCount.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
        
        log.info("[MessageSendMonitor][消息发送成功: topic={}, orderId={}, msgId={}]", topic, orderId, msgId);
    }

    /**
     * 记录消息发送失败
     *
     * @param topic Topic名称
     * @param orderId 订单ID
     * @param error 错误信息
     */
    public void recordFailure(String topic, Long orderId, String error) {
        failureCount.incrementAndGet();
        topicFailureCount.computeIfAbsent(topic, k -> new AtomicLong(0)).incrementAndGet();
        
        log.error("[MessageSendMonitor][消息发送失败: topic={}, orderId={}, error={}]", topic, orderId, error);
    }

    /**
     * 获取总体发送成功率
     *
     * @return 成功率（0-100）
     */
    public double getOverallSuccessRate() {
        long total = successCount.get() + failureCount.get();
        if (total == 0) {
            return 100.0;
        }
        return (double) successCount.get() / total * 100;
    }

    /**
     * 获取指定Topic的发送成功率
     *
     * @param topic Topic名称
     * @return 成功率（0-100）
     */
    public double getTopicSuccessRate(String topic) {
        long success = topicSuccessCount.getOrDefault(topic, new AtomicLong(0)).get();
        long failure = topicFailureCount.getOrDefault(topic, new AtomicLong(0)).get();
        long total = success + failure;
        
        if (total == 0) {
            return 100.0;
        }
        return (double) success / total * 100;
    }

    /**
     * 获取监控统计信息
     *
     * @return 统计信息
     */
    public String getStatistics() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 消息发送监控统计 ===\n");
        sb.append(String.format("总发送成功: %d\n", successCount.get()));
        sb.append(String.format("总发送失败: %d\n", failureCount.get()));
        sb.append(String.format("总体成功率: %.2f%%\n", getOverallSuccessRate()));
        
        sb.append("\n=== 按Topic统计 ===\n");
        for (String topic : topicSuccessCount.keySet()) {
            long success = topicSuccessCount.get(topic).get();
            long failure = topicFailureCount.getOrDefault(topic, new AtomicLong(0)).get();
            double rate = getTopicSuccessRate(topic);
            sb.append(String.format("%s: 成功=%d, 失败=%d, 成功率=%.2f%%\n", topic, success, failure, rate));
        }
        
        return sb.toString();
    }

    /**
     * 重置统计数据
     */
    public void reset() {
        successCount.set(0);
        failureCount.set(0);
        topicSuccessCount.clear();
        topicFailureCount.clear();
        
        log.info("[MessageSendMonitor][统计数据已重置]");
    }
}
