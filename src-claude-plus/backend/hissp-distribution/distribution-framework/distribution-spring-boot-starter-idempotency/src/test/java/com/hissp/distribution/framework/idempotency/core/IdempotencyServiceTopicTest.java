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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * IdempotencyService Topic 字段测试
 * 验证 topic 字段是否被正确写入数据库
 * 
 * @author system
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceTopicTest {

    @Mock
    private MqMessageIdempotencyMapper idempotencyMapper;

    @InjectMocks
    private IdempotencyServiceImpl idempotencyService;

    private static final String TEST_MESSAGE_ID = "test-message-id-123";
    private static final String TEST_CONSUMER_GROUP = "test-consumer-group";
    private static final String TEST_BUSINESS_KEY = "test-business-key-456";
    private static final String TEST_TOPIC = "trade-order-events";

    @BeforeEach
    void setUp() {
        // 重置所有mock
        reset(idempotencyMapper);
    }

    @Test
    void testCheckAndRecordWithTopic_ShouldSetTopicField() {
        // Given
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class))).thenReturn(1);

        // When
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY, TEST_TOPIC);

        // Then
        assertTrue(result);
        
        // 验证插入的记录包含正确的topic字段
        ArgumentCaptor<MqMessageIdempotencyDO> captor = ArgumentCaptor.forClass(MqMessageIdempotencyDO.class);
        verify(idempotencyMapper, times(1)).insert(captor.capture());
        
        MqMessageIdempotencyDO insertedRecord = captor.getValue();
        assertEquals(TEST_MESSAGE_ID, insertedRecord.getMessageId());
        assertEquals(TEST_CONSUMER_GROUP, insertedRecord.getConsumerGroup());
        assertEquals(TEST_BUSINESS_KEY, insertedRecord.getBusinessKey());
        assertEquals(TEST_TOPIC, insertedRecord.getTopic()); // 验证topic字段
        assertEquals(MqMessageIdempotencyDO.Status.PROCESSING, insertedRecord.getStatus());
        assertEquals(0, insertedRecord.getRetryCount());
        assertNotNull(insertedRecord.getCreateTime());
        assertNotNull(insertedRecord.getUpdateTime());
    }

    @Test
    void testCheckAndRecordWithTopic_SimplifiedVersion_ShouldSetTopicField() {
        // Given
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(anyString(), eq(TEST_CONSUMER_GROUP)))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class))).thenReturn(1);

        // When
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP, TEST_TOPIC);

        // Then
        assertTrue(result);

        // 验证插入的记录包含正确的topic字段
        ArgumentCaptor<MqMessageIdempotencyDO> captor = ArgumentCaptor.forClass(MqMessageIdempotencyDO.class);
        verify(idempotencyMapper, times(1)).insert(captor.capture());

        MqMessageIdempotencyDO insertedRecord = captor.getValue();
        assertNotNull(insertedRecord.getMessageId()); // UUID生成的messageId
        assertTrue(insertedRecord.getMessageId().length() == 32); // UUID去掉横线后的长度
        assertEquals(TEST_CONSUMER_GROUP, insertedRecord.getConsumerGroup());
        assertEquals(TEST_BUSINESS_KEY, insertedRecord.getBusinessKey());
        assertEquals(TEST_TOPIC, insertedRecord.getTopic()); // 验证topic字段
        assertEquals(MqMessageIdempotencyDO.Status.PROCESSING, insertedRecord.getStatus());
    }

    @Test
    void testCheckAndRecord_OriginalMethod_ShouldSetTopicToNull() {
        // Given
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class))).thenReturn(1);

        // When
        boolean result = idempotencyService.checkAndRecord(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY);

        // Then
        assertTrue(result);
        
        // 验证插入的记录topic字段为null（保持向后兼容）
        ArgumentCaptor<MqMessageIdempotencyDO> captor = ArgumentCaptor.forClass(MqMessageIdempotencyDO.class);
        verify(idempotencyMapper, times(1)).insert(captor.capture());
        
        MqMessageIdempotencyDO insertedRecord = captor.getValue();
        assertEquals(TEST_MESSAGE_ID, insertedRecord.getMessageId());
        assertEquals(TEST_CONSUMER_GROUP, insertedRecord.getConsumerGroup());
        assertEquals(TEST_BUSINESS_KEY, insertedRecord.getBusinessKey());
        assertNull(insertedRecord.getTopic()); // 验证topic字段为null
        assertEquals(MqMessageIdempotencyDO.Status.PROCESSING, insertedRecord.getStatus());
    }

    @Test
    void testCheckAndRecord_SimplifiedOriginalMethod_ShouldSetTopicToNull() {
        // Given
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(anyString(), eq(TEST_CONSUMER_GROUP)))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class))).thenReturn(1);

        // When
        boolean result = idempotencyService.checkAndRecord(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP);

        // Then
        assertTrue(result);

        // 验证插入的记录topic字段为null（保持向后兼容）
        ArgumentCaptor<MqMessageIdempotencyDO> captor = ArgumentCaptor.forClass(MqMessageIdempotencyDO.class);
        verify(idempotencyMapper, times(1)).insert(captor.capture());

        MqMessageIdempotencyDO insertedRecord = captor.getValue();
        assertNotNull(insertedRecord.getMessageId()); // UUID生成的messageId
        assertTrue(insertedRecord.getMessageId().length() == 32); // UUID去掉横线后的长度
        assertEquals(TEST_CONSUMER_GROUP, insertedRecord.getConsumerGroup());
        assertEquals(TEST_BUSINESS_KEY, insertedRecord.getBusinessKey());
        assertNull(insertedRecord.getTopic()); // 验证topic字段为null
        assertEquals(MqMessageIdempotencyDO.Status.PROCESSING, insertedRecord.getStatus());
    }

    @Test
    void testCheckAndRecordWithTopic_ExistingMessage_ShouldReturnFalse() {
        // Given
        MqMessageIdempotencyDO existingRecord = new MqMessageIdempotencyDO();
        existingRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);
        
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(existingRecord);

        // When
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY, TEST_TOPIC);

        // Then
        assertFalse(result);
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecordWithTopic_ExistingBusinessKey_ShouldReturnFalse() {
        // Given
        MqMessageIdempotencyDO businessRecord = new MqMessageIdempotencyDO();
        businessRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);
        
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(businessRecord);

        // When
        boolean result = idempotencyService.checkAndRecordWithTopic(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY, TEST_TOPIC);

        // Then
        assertFalse(result);
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }
}
