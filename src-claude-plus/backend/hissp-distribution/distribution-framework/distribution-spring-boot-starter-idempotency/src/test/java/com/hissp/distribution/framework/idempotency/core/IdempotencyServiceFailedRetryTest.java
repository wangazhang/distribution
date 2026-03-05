package com.hissp.distribution.framework.idempotency.core;

import com.hissp.distribution.framework.idempotency.dal.dataobject.MqMessageIdempotencyDO;
import com.hissp.distribution.framework.idempotency.dal.mysql.MqMessageIdempotencyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 幂等性服务失败重试测试
 * 验证失败状态的记录可以重新处理
 *
 * @author system
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceFailedRetryTest {

    private static final String TEST_BUSINESS_KEY = "test-business-key";
    private static final String TEST_CONSUMER_GROUP = "test-consumer-group";
    private static final String TEST_TOPIC = "test-topic";
    private static final String TEST_MESSAGE_ID = "test-message-id";

    @Mock
    private MqMessageIdempotencyMapper idempotencyMapper;

    @InjectMocks
    private IdempotencyServiceImpl idempotencyService;

    private MqMessageIdempotencyDO failedRecord;

    @BeforeEach
    void setUp() {
        // 创建一个失败状态的记录
        failedRecord = new MqMessageIdempotencyDO();
        failedRecord.setId(1L);
        failedRecord.setMessageId(TEST_MESSAGE_ID);
        failedRecord.setConsumerGroup(TEST_CONSUMER_GROUP);
        failedRecord.setTopic(TEST_TOPIC);
        failedRecord.setBusinessKey(TEST_BUSINESS_KEY);
        failedRecord.setStatus(MqMessageIdempotencyDO.Status.FAILED);
        failedRecord.setRetryCount(1);
        failedRecord.setErrorMsg("Previous processing failed");
        failedRecord.setCreateTime(LocalDateTime.now().minusMinutes(5));
        failedRecord.setUpdateTime(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void testCheckAndRecordWithTopic_FailedRecord_ShouldAllowRetry() {
        // Given: 存在一个失败状态的记录
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(anyString(), eq(TEST_CONSUMER_GROUP)))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(failedRecord);
        when(idempotencyMapper.updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP,
            MqMessageIdempotencyDO.Status.PROCESSING, null)).thenReturn(1);

        // When: 调用检查方法
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP, TEST_TOPIC);

        // Then: 应该允许处理（返回true）
        assertTrue(result, "失败状态的记录应该允许重新处理");

        // 验证更新了失败记录的状态为处理中
        verify(idempotencyMapper, times(1)).updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP,
            MqMessageIdempotencyDO.Status.PROCESSING, null);

        // 验证没有删除记录
        verify(idempotencyMapper, never()).deleteById(any());

        // 验证没有插入新记录
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecord_FailedRecord_ShouldAllowRetry() {
        // Given: 存在一个失败状态的记录
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(anyString(), eq(TEST_CONSUMER_GROUP)))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(failedRecord);
        when(idempotencyMapper.updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP,
            MqMessageIdempotencyDO.Status.PROCESSING, null)).thenReturn(1);

        // When: 调用检查方法
        boolean result = idempotencyService.checkAndRecord(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP);

        // Then: 应该允许处理（返回true）
        assertTrue(result, "失败状态的记录应该允许重新处理");

        // 验证更新了失败记录的状态为处理中
        verify(idempotencyMapper, times(1)).updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP,
            MqMessageIdempotencyDO.Status.PROCESSING, null);

        // 验证没有删除记录
        verify(idempotencyMapper, never()).deleteById(any());

        // 验证没有插入新记录
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecordWithTopic_SuccessRecord_ShouldSkip() {
        // Given: 存在一个成功状态的记录
        MqMessageIdempotencyDO successRecord = new MqMessageIdempotencyDO();
        successRecord.setId(1L);
        successRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);
        successRecord.setBusinessKey(TEST_BUSINESS_KEY);

        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(anyString(), eq(TEST_CONSUMER_GROUP)))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(successRecord);

        // When: 调用检查方法
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP, TEST_TOPIC);

        // Then: 应该跳过处理（返回false）
        assertFalse(result, "成功状态的记录应该跳过处理");

        // 验证没有删除记录
        verify(idempotencyMapper, never()).deleteById(any());
        // 验证没有插入新记录
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecordWithTopic_ProcessingRecord_ShouldSkip() {
        // Given: 存在一个处理中状态的记录
        MqMessageIdempotencyDO processingRecord = new MqMessageIdempotencyDO();
        processingRecord.setId(1L);
        processingRecord.setStatus(MqMessageIdempotencyDO.Status.PROCESSING);
        processingRecord.setBusinessKey(TEST_BUSINESS_KEY);

        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(anyString(), eq(TEST_CONSUMER_GROUP)))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(processingRecord);

        // When: 调用检查方法
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP, TEST_TOPIC);

        // Then: 应该跳过处理（返回false）
        assertFalse(result, "处理中状态的记录应该跳过处理");

        // 验证没有删除记录
        verify(idempotencyMapper, never()).deleteById(any());
        // 验证没有插入新记录
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }
}
