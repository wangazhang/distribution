package com.hissp.distribution.module.trade.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MQ迁移配置类
 * 支持灰度发布和向后兼容性
 * 
 * @author system
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "trade.mq.migration")
public class MqMigrationConfig {

    /**
     * 是否启用MQ模式
     * true: 使用新的MQ异步模式
     * false: 使用原有的同步Handler模式
     */
    private boolean enabled = true;

    /**
     * 是否启用降级模式
     * true: MQ发送失败时降级到同步Handler模式
     * false: MQ发送失败时直接抛异常
     */
    private boolean fallbackEnabled = false;

    /**
     * 是否启用双写模式（用于灰度验证）
     * true: 同时执行MQ模式和Handler模式，但以MQ结果为准
     * false: 只执行一种模式
     */
    private boolean dualWriteEnabled = false;

    /**
     * MQ发送超时时间（毫秒）
     */
    private long sendTimeoutMs = 3000;

    /**
     * MQ发送重试次数
     */
    private int retryTimes = 3;

    /**
     * 是否启用性能监控
     */
    private boolean monitorEnabled = true;

    /**
     * 监控数据保留天数
     */
    private int monitorRetentionDays = 7;

    /**
     * 是否启用详细日志
     */
    private boolean verboseLogging = false;

    /**
     * 灰度发布配置
     */
    private GrayReleaseConfig grayRelease = new GrayReleaseConfig();

    @Data
    public static class GrayReleaseConfig {
        /**
         * 是否启用灰度发布
         */
        private boolean enabled = false;

        /**
         * 灰度比例（0-100）
         * 表示使用新MQ模式的流量比例
         */
        private int percentage = 0;

        /**
         * 灰度用户ID列表
         * 指定的用户ID强制使用新模式
         */
        private String userIds = "";

        /**
         * 灰度订单类型列表
         * 指定的订单类型使用新模式
         */
        private String orderTypes = "";
    }

    /**
     * 检查是否应该使用MQ模式
     *
     * @param userId 用户ID
     * @param orderType 订单类型
     * @return true-使用MQ模式, false-使用Handler模式
     */
    public boolean shouldUseMqMode(Long userId, Integer orderType) {
        if (!enabled) {
            return false;
        }

        if (!grayRelease.enabled) {
            return true;
        }

        // 检查指定用户ID
        if (userId != null && !grayRelease.userIds.isEmpty()) {
            String[] userIdArray = grayRelease.userIds.split(",");
            for (String id : userIdArray) {
                if (userId.toString().equals(id.trim())) {
                    return true;
                }
            }
        }

        // 检查指定订单类型
        if (orderType != null && !grayRelease.orderTypes.isEmpty()) {
            String[] orderTypeArray = grayRelease.orderTypes.split(",");
            for (String type : orderTypeArray) {
                if (orderType.toString().equals(type.trim())) {
                    return true;
                }
            }
        }

        // 按比例灰度
        if (userId != null) {
            int hash = Math.abs(userId.hashCode() % 100);
            return hash < grayRelease.percentage;
        }

        return false;
    }

    /**
     * 是否启用降级模式
     */
    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    /**
     * 是否启用双写模式
     */
    public boolean isDualWriteEnabled() {
        return dualWriteEnabled;
    }

    /**
     * 获取发送超时时间
     */
    public long getSendTimeoutMs() {
        return sendTimeoutMs;
    }

    /**
     * 获取重试次数
     */
    public int getRetryTimes() {
        return retryTimes;
    }

    /**
     * 是否启用监控
     */
    public boolean isMonitorEnabled() {
        return monitorEnabled;
    }

    /**
     * 是否启用详细日志
     */
    public boolean isVerboseLogging() {
        return verboseLogging;
    }
}
