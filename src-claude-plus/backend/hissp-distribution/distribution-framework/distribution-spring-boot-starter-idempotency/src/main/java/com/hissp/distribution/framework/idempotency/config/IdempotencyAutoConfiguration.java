package com.hissp.distribution.framework.idempotency.config;

import com.hissp.distribution.framework.idempotency.core.IdempotencyService;
import com.hissp.distribution.framework.idempotency.core.IdempotencyServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.annotation.Resource;

/**
 * 幂等性自动配置类
 *
 * @author system
 */
@AutoConfiguration
@EnableConfigurationProperties(IdempotencyProperties.class)
@ConditionalOnProperty(prefix = "distribution.idempotency", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableScheduling
@Slf4j
public class IdempotencyAutoConfiguration {

    @Resource
    private IdempotencyProperties idempotencyProperties;

    /**
     * 幂等性服务Bean
     */
    @Bean
    @ConditionalOnMissingBean
    public IdempotencyService idempotencyService() {
        log.info("[IdempotencyAutoConfiguration][初始化幂等性服务]");
        return new IdempotencyServiceImpl();
    }

    /**
     * 幂等性数据清理任务
     * 定期清理过期的幂等性记录
     */
    @Bean
    @ConditionalOnProperty(prefix = "distribution.idempotency.cleanup", name = "enabled", havingValue = "true")
    public IdempotencyCleanupTask idempotencyCleanupTask() {
        log.info("[IdempotencyAutoConfiguration][初始化幂等性清理任务]");
        return new IdempotencyCleanupTask();
    }

    /**
     * 幂等性数据清理任务实现
     */
    public class IdempotencyCleanupTask {

        @Resource
        private IdempotencyService idempotencyService;

        /**
         * 定期清理过期数据
         * 使用配置的cron表达式执行
         */
        @Scheduled(cron = "#{@idempotencyProperties.cleanup.cron}")
        public void cleanupExpiredRecords() {
            try {
                int retentionDays = idempotencyProperties.getCleanup().getRetentionDays();
                int deletedCount = idempotencyService.cleanExpiredRecords(retentionDays);
                
                if (deletedCount > 0) {
                    log.info("[cleanupExpiredRecords][清理过期幂等性记录完成: retentionDays={}, deletedCount={}]", 
                        retentionDays, deletedCount);
                } else {
                    log.debug("[cleanupExpiredRecords][无过期记录需要清理: retentionDays={}]", retentionDays);
                }
            } catch (Exception e) {
                log.error("[cleanupExpiredRecords][清理过期幂等性记录失败]", e);
            }
        }
    }
}
