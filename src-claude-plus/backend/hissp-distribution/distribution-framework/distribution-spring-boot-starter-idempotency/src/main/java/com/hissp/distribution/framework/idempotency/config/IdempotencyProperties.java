package com.hissp.distribution.framework.idempotency.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 幂等性配置属性
 *
 * @author system
 */
@ConfigurationProperties(prefix = "distribution.idempotency")
@Data
public class IdempotencyProperties {

    /**
     * 是否启用幂等性功能
     */
    private boolean enabled = true;

    /**
     * 数据库表名
     */
    private String tableName = "mq_message_idempotency";

    /**
     * 过期数据清理配置
     */
    private CleanupConfig cleanup = new CleanupConfig();

    /**
     * 过期数据清理配置
     */
    @Data
    public static class CleanupConfig {

        /**
         * 是否启用自动清理
         */
        private boolean enabled = false;

        /**
         * 数据保留天数
         */
        private int retentionDays = 30;

        /**
         * 清理任务执行间隔（小时）
         */
        private int intervalHours = 24;

        /**
         * 清理任务的cron表达式
         * 默认每天凌晨2点执行
         */
        private String cron = "0 0 2 * * ?";
    }
}
