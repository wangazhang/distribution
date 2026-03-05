package com.hissp.distribution.framework.idempotency.core;

import com.hissp.distribution.framework.idempotency.dal.dataobject.MqMessageIdempotencyDO;
import com.hissp.distribution.framework.idempotency.dal.mysql.MqMessageIdempotencyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * IdempotencyServiceImpl 单元测试
 * 
 * @author system
 */
@ExtendWith(MockitoExtension.class)
class IdempotencyServiceImplTest {

    @Mock
    private MqMessageIdempotencyMapper idempotencyMapper;

    @InjectMocks
    private IdempotencyServiceImpl idempotencyService;

    private static final String TEST_MESSAGE_ID = "test-message-id-123";
    private static final String TEST_CONSUMER_GROUP = "test-consumer-group";
    private static final String TEST_BUSINESS_KEY = "test-business-key-456";

    @BeforeEach
    void setUp() {
        // 重置所有mock
        reset(idempotencyMapper);
    }

    @Test
    void testCheckAndRecord_NewMessage_Success() {
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
        verify(idempotencyMapper, times(1)).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecord_ExistingMessage_ReturnFalse() {
        // Given
        MqMessageIdempotencyDO existingRecord = new MqMessageIdempotencyDO();
        existingRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);
        
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(existingRecord);

        // When
        boolean result = idempotencyService.checkAndRecord(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY);

        // Then
        assertFalse(result);
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecord_ExistingBusinessKey_ReturnFalse() {
        // Given
        MqMessageIdempotencyDO businessRecord = new MqMessageIdempotencyDO();
        businessRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);
        
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(businessRecord);

        // When
        boolean result = idempotencyService.checkAndRecord(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY);

        // Then
        assertFalse(result);
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testCheckAndRecord_DuplicateKeyException_ReturnFalse() {
        // Given
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class)))
            .thenThrow(new DuplicateKeyException("Duplicate key"));

        // When
        boolean result = idempotencyService.checkAndRecord(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, TEST_BUSINESS_KEY);

        // Then
        assertFalse(result);
    }

    @Test
    void testCheckAndRecord_SimplifiedVersion_Success() {
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
        verify(idempotencyMapper, times(1)).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    void testMarkSuccess_BusinessKey_Success() {
        // Given
        MqMessageIdempotencyDO record = new MqMessageIdempotencyDO();
        record.setMessageId(TEST_MESSAGE_ID);
        
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(record);
        when(idempotencyMapper.updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, 
            MqMessageIdempotencyDO.Status.SUCCESS, null)).thenReturn(1);

        // When
        idempotencyService.markSuccess(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP);

        // Then
        verify(idempotencyMapper, times(1)).updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, 
            MqMessageIdempotencyDO.Status.SUCCESS, null);
    }

    @Test
    void testMarkFailed_BusinessKey_Success() {
        // Given
        String errorMsg = "Test error message";
        MqMessageIdempotencyDO record = new MqMessageIdempotencyDO();
        record.setMessageId(TEST_MESSAGE_ID);
        
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(record);
        when(idempotencyMapper.incrementRetryCount(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP)).thenReturn(1);
        when(idempotencyMapper.updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, 
            MqMessageIdempotencyDO.Status.FAILED, errorMsg)).thenReturn(1);

        // When
        idempotencyService.markFailed(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP, errorMsg);

        // Then
        verify(idempotencyMapper, times(1)).incrementRetryCount(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP);
        verify(idempotencyMapper, times(1)).updateStatus(TEST_MESSAGE_ID, TEST_CONSUMER_GROUP, 
            MqMessageIdempotencyDO.Status.FAILED, errorMsg);
    }

    @Test
    void testIsBusinessProcessed_Success_ReturnTrue() {
        // Given
        MqMessageIdempotencyDO record = new MqMessageIdempotencyDO();
        record.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);
        
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(record);

        // When
        boolean result = idempotencyService.isBusinessProcessed(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsBusinessProcessed_NotProcessed_ReturnFalse() {
        // Given
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP))
            .thenReturn(null);

        // When
        boolean result = idempotencyService.isBusinessProcessed(TEST_BUSINESS_KEY, TEST_CONSUMER_GROUP);

        // Then
        assertFalse(result);
    }
}
