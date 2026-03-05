package com.hissp.distribution.framework.idempotency.core;

import com.hissp.distribution.framework.idempotency.dal.dataobject.MqMessageIdempotencyDO;
import com.hissp.distribution.framework.idempotency.dal.mysql.MqMessageIdempotencyMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MQ消息幂等性服务实现
 *
 * 优化版本：
 * 1. 简化接口实现，减少冗余逻辑
 * 2. 优化数据库查询性能
 * 3. 支持基于业务键的幂等性检查
 *
 * @author system
 */
@Service
@Slf4j
public class IdempotencyServiceImpl implements IdempotencyService {

    @Resource
    private MqMessageIdempotencyMapper idempotencyMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean checkAndRecord(String messageId, String consumerGroup, String businessKey) {
        log.debug("[checkAndRecord][检查消息幂等性: messageId={}, consumerGroup={}, businessKey={}]", 
            messageId, consumerGroup, businessKey);

        // 1. 先检查是否已存在记录
        MqMessageIdempotencyDO existing = idempotencyMapper.selectByMessageIdAndConsumerGroup(messageId, consumerGroup);
        if (existing != null) {
            log.info("[checkAndRecord][消息已处理过: messageId={}, status={}]", messageId, existing.getStatus());
            return false;
        }

        // 2. 检查业务键是否已处理
        if (StringUtils.hasText(businessKey)) {
            MqMessageIdempotencyDO businessExisting = idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
            if (businessExisting != null) {
                // 根据状态判断是否需要跳过处理
                if (businessExisting.getStatus().equals(MqMessageIdempotencyDO.Status.SUCCESS)) {
                    log.info("[checkAndRecord][业务已成功处理，跳过: businessKey={}, status={}]", businessKey, businessExisting.getStatus());
                    return false;
                } else if (businessExisting.getStatus().equals(MqMessageIdempotencyDO.Status.PROCESSING)) {
                    log.info("[checkAndRecord][业务正在处理中，跳过: businessKey={}, status={}]", businessKey, businessExisting.getStatus());
                    return false;
                } else if (businessExisting.getStatus().equals(MqMessageIdempotencyDO.Status.FAILED)) {
                    // 失败状态允许重新处理，直接更新状态为处理中，避免删除重插入的并发问题
                    log.info("[checkAndRecord][业务处理失败，重置为处理中状态: businessKey={}, status={}, retryCount={}]",
                        businessKey, businessExisting.getStatus(), businessExisting.getRetryCount());

                    // 更新现有记录为处理中状态，重置错误信息
                    int updated = idempotencyMapper.updateStatus(businessExisting.getMessageId(), consumerGroup,
                        MqMessageIdempotencyDO.Status.PROCESSING, null);

                    if (updated > 0) {
                        log.debug("[checkAndRecord][重置失败记录为处理中状态成功: businessKey={}]", businessKey);
                        return true; // 允许处理
                    } else {
                        log.warn("[checkAndRecord][重置失败记录状态失败，可能已被其他线程处理: businessKey={}]", businessKey);
                        return false; // 可能已被其他线程处理，跳过
                    }
                }
            }
        }

        // 3. 插入处理中记录
        try {
            MqMessageIdempotencyDO record = new MqMessageIdempotencyDO();
            record.setMessageId(messageId);
            record.setConsumerGroup(consumerGroup);
            record.setTopic(null); // 原有方法设置为null，保持向后兼容
            record.setBusinessKey(businessKey);
            record.setStatus(MqMessageIdempotencyDO.Status.PROCESSING);
            record.setRetryCount(0);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            idempotencyMapper.insert(record);
            log.debug("[checkAndRecord][记录消息处理状态: messageId={}]", messageId);
            return true;
        } catch (DuplicateKeyException e) {
            // 并发情况下可能出现重复插入，此时认为已处理
            log.warn("[checkAndRecord][并发插入冲突，消息可能已被处理: messageId={}]", messageId);
            return false;
        }
    }

    /**
     * 标记消息处理成功（原有方法，基于消息ID）
     *
     * @param messageId 消息唯一标识
     * @param consumerGroup 消费者组名称
     */
    private void markSuccessInternal(String messageId, String consumerGroup) {
        log.debug("[markSuccessInternal][标记消息处理成功: messageId={}, consumerGroup={}]", messageId, consumerGroup);

        int updated = idempotencyMapper.updateStatus(messageId, consumerGroup,
            MqMessageIdempotencyDO.Status.SUCCESS, null);

        if (updated > 0) {
            log.info("[markSuccessInternal][消息处理成功: messageId={}]", messageId);
        } else {
            log.warn("[markSuccessInternal][未找到消息记录: messageId={}]", messageId);
        }
    }

    /**
     * 标记消息处理失败（原有方法，基于消息ID）
     *
     * @param messageId 消息唯一标识
     * @param consumerGroup 消费者组名称
     * @param errorMsg 错误信息
     */
    private void markFailedInternal(String messageId, String consumerGroup, String errorMsg) {
        log.debug("[markFailedInternal][标记消息处理失败: messageId={}, consumerGroup={}, error={}]",
            messageId, consumerGroup, errorMsg);

        // 增加重试次数
        idempotencyMapper.incrementRetryCount(messageId, consumerGroup);

        // 更新状态为失败
        int updated = idempotencyMapper.updateStatus(messageId, consumerGroup,
            MqMessageIdempotencyDO.Status.FAILED, errorMsg);

        if (updated > 0) {
            log.warn("[markFailedInternal][消息处理失败: messageId={}, error={}]", messageId, errorMsg);
        } else {
            log.error("[markFailedInternal][未找到消息记录: messageId={}]", messageId);
        }
    }

    @Override
    public boolean isBusinessProcessed(String businessKey, String consumerGroup) {
        if (!StringUtils.hasText(businessKey)) {
            return false;
        }
        
        MqMessageIdempotencyDO record = idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
        boolean processed = record != null && record.getStatus() == MqMessageIdempotencyDO.Status.SUCCESS;
        
        log.debug("[isBusinessProcessed][业务处理检查: businessKey={}, processed={}]", businessKey, processed);
        return processed;
    }

    @Override
    public int cleanExpiredRecords(int expireDays) {
        LocalDateTime expireTime = LocalDateTime.now().minusDays(expireDays);
        int deleted = idempotencyMapper.deleteExpiredRecords(expireTime);

        log.info("[cleanExpiredRecords][清理过期记录: expireDays={}, deleted={}]", expireDays, deleted);
        return deleted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndRecord(String businessKey, String consumerGroup) {
        // 生成唯一消息ID
        String messageId = generateMessageId();
        return checkAndRecord(messageId, consumerGroup, businessKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndRecordWithTopic(String businessKey, String consumerGroup, String topic) {
        // 生成唯一消息ID
        String messageId = generateMessageId();
        return checkAndRecordWithTopic(messageId, consumerGroup, businessKey, topic);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndRecordWithTopic(String messageId, String consumerGroup, String businessKey, String topic) {
        log.debug("[checkAndRecordWithTopic][检查消息幂等性: messageId={}, consumerGroup={}, businessKey={}, topic={}]",
            messageId, consumerGroup, businessKey, topic);

        // 1. 先检查是否已存在记录
        MqMessageIdempotencyDO existing = idempotencyMapper.selectByMessageIdAndConsumerGroup(messageId, consumerGroup);
        if (existing != null) {
            log.info("[checkAndRecordWithTopic][消息已处理过: messageId={}, status={}]", messageId, existing.getStatus());
            return false;
        }

        // 2. 检查业务键是否已存在
        if (StringUtils.hasText(businessKey)) {
            MqMessageIdempotencyDO businessRecord = idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
            if (businessRecord != null) {
                // 根据状态判断是否需要跳过处理
                if (businessRecord.getStatus().equals(MqMessageIdempotencyDO.Status.SUCCESS)) {
                    log.info("[checkAndRecordWithTopic][业务已成功处理，跳过: businessKey={}, status={}]", businessKey, businessRecord.getStatus());
                    return false;
                } else if (businessRecord.getStatus().equals(MqMessageIdempotencyDO.Status.PROCESSING)) {
                    log.info("[checkAndRecordWithTopic][业务正在处理中，跳过: businessKey={}, status={}]", businessKey, businessRecord.getStatus());
                    return false;
                } else if (businessRecord.getStatus().equals(MqMessageIdempotencyDO.Status.FAILED)) {
                    // 失败状态允许重新处理，直接更新状态为处理中，避免删除重插入的并发问题
                    log.info("[checkAndRecordWithTopic][业务处理失败，重置为处理中状态: businessKey={}, status={}, retryCount={}]",
                        businessKey, businessRecord.getStatus(), businessRecord.getRetryCount());

                    // 更新现有记录为处理中状态，重置错误信息
                    int updated = idempotencyMapper.updateStatus(businessRecord.getMessageId(), consumerGroup,
                        MqMessageIdempotencyDO.Status.PROCESSING, null);

                    if (updated > 0) {
                        log.debug("[checkAndRecordWithTopic][重置失败记录为处理中状态成功: businessKey={}]", businessKey);
                        return true; // 允许处理
                    } else {
                        log.warn("[checkAndRecordWithTopic][重置失败记录状态失败，可能已被其他线程处理: businessKey={}]", businessKey);
                        return false; // 可能已被其他线程处理，跳过
                    }
                }
            }
        }

        // 3. 插入处理中记录
        try {
            MqMessageIdempotencyDO record = new MqMessageIdempotencyDO();
            record.setMessageId(messageId);
            record.setConsumerGroup(consumerGroup);
            record.setTopic(topic); // 设置topic字段
            record.setBusinessKey(businessKey);
            record.setStatus(MqMessageIdempotencyDO.Status.PROCESSING);
            record.setRetryCount(0);
            record.setCreateTime(LocalDateTime.now());
            record.setUpdateTime(LocalDateTime.now());

            idempotencyMapper.insert(record);
            log.debug("[checkAndRecordWithTopic][记录消息处理状态: messageId={}, topic={}]", messageId, topic);
            return true;
        } catch (DuplicateKeyException e) {
            // 并发情况下可能出现重复插入，此时认为已处理
            log.warn("[checkAndRecordWithTopic][并发插入冲突，消息可能已被处理: messageId={}]", messageId);
            return false;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markSuccess(String businessKey, String consumerGroup) {
        log.debug("[markSuccess][标记业务处理成功: businessKey={}, consumerGroup={}]", businessKey, consumerGroup);

        // 通过业务键查找记录，然后更新状态
        MqMessageIdempotencyDO record = idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
        if (record != null) {
            int updated = idempotencyMapper.updateStatus(record.getMessageId(), consumerGroup,
                MqMessageIdempotencyDO.Status.SUCCESS, null);

            if (updated > 0) {
                log.info("[markSuccess][业务处理成功: businessKey={}]", businessKey);
            } else {
                log.warn("[markSuccess][更新业务记录失败: businessKey={}]", businessKey);
            }
        } else {
            log.warn("[markSuccess][未找到业务记录: businessKey={}]", businessKey);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markSuccessById(String messageId, String consumerGroup) {
        markSuccessInternal(messageId, consumerGroup);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markFailed(String businessKey, String consumerGroup, String errorMsg) {
        log.debug("[markFailed][标记业务处理失败: businessKey={}, consumerGroup={}, error={}]",
            businessKey, consumerGroup, errorMsg);

        // 通过业务键查找记录，然后更新状态
        MqMessageIdempotencyDO record = idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
        if (record != null) {
            // 增加重试次数
            idempotencyMapper.incrementRetryCount(record.getMessageId(), consumerGroup);

            // 更新状态为失败
            int updated = idempotencyMapper.updateStatus(record.getMessageId(), consumerGroup,
                MqMessageIdempotencyDO.Status.FAILED, errorMsg);

            if (updated > 0) {
                log.warn("[markFailed][业务处理失败: businessKey={}, error={}]", businessKey, errorMsg);
            } else {
                log.error("[markFailed][更新业务记录失败: businessKey={}]", businessKey);
            }
        } else {
            log.error("[markFailed][未找到业务记录: businessKey={}]", businessKey);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markFailedById(String messageId, String consumerGroup, String errorMsg) {
        markFailedInternal(messageId, consumerGroup, errorMsg);
    }

    /**
     * 生成消息ID
     * 使用简单的UUID生成唯一标识符，与业务键解耦
     *
     * @return 消息ID
     */
    private String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
