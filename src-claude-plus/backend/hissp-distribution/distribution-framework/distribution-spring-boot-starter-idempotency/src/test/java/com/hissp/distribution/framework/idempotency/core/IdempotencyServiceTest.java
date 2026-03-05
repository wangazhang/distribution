package com.hissp.distribution.framework.idempotency.core;

import com.hissp.distribution.framework.idempotency.dal.dataobject.MqMessageIdempotencyDO;
import com.hissp.distribution.framework.idempotency.dal.mysql.MqMessageIdempotencyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 幂等性服务测试类
 *
 * @author system
 */
public class IdempotencyServiceTest {

    @Mock
    private MqMessageIdempotencyMapper idempotencyMapper;

    @InjectMocks
    private IdempotencyServiceImpl idempotencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCheckAndRecord_FirstTime_ShouldReturnTrue() {
        // 准备测试数据
        String messageId = "test_msg_001";
        String consumerGroup = "test_consumer_group";
        String businessKey = "test_business_key";

        // Mock 查询返回null（首次处理）
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(messageId, consumerGroup)).thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup)).thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class))).thenReturn(1);

        // 执行测试
        boolean result = idempotencyService.checkAndRecord(messageId, consumerGroup, businessKey);

        // 验证结果
        assertTrue(result);
        verify(idempotencyMapper).selectByMessageIdAndConsumerGroup(messageId, consumerGroup);
        verify(idempotencyMapper).selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
        verify(idempotencyMapper).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    public void testCheckAndRecord_MessageAlreadyExists_ShouldReturnFalse() {
        // 准备测试数据
        String messageId = "test_msg_001";
        String consumerGroup = "test_consumer_group";
        String businessKey = "test_business_key";

        MqMessageIdempotencyDO existingRecord = new MqMessageIdempotencyDO();
        existingRecord.setMessageId(messageId);
        existingRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);

        // Mock 查询返回已存在记录
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(messageId, consumerGroup)).thenReturn(existingRecord);

        // 执行测试
        boolean result = idempotencyService.checkAndRecord(messageId, consumerGroup, businessKey);

        // 验证结果
        assertFalse(result);
        verify(idempotencyMapper).selectByMessageIdAndConsumerGroup(messageId, consumerGroup);
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    public void testCheckAndRecord_BusinessAlreadyProcessed_ShouldReturnFalse() {
        // 准备测试数据
        String messageId = "test_msg_001";
        String consumerGroup = "test_consumer_group";
        String businessKey = "test_business_key";

        MqMessageIdempotencyDO businessRecord = new MqMessageIdempotencyDO();
        businessRecord.setBusinessKey(businessKey);
        businessRecord.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);

        // Mock 查询
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(messageId, consumerGroup)).thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup)).thenReturn(businessRecord);

        // 执行测试
        boolean result = idempotencyService.checkAndRecord(messageId, consumerGroup, businessKey);

        // 验证结果
        assertFalse(result);
        verify(idempotencyMapper).selectByMessageIdAndConsumerGroup(messageId, consumerGroup);
        verify(idempotencyMapper).selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
        verify(idempotencyMapper, never()).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    public void testCheckAndRecord_ConcurrentInsert_ShouldReturnFalse() {
        // 准备测试数据
        String messageId = "test_msg_001";
        String consumerGroup = "test_consumer_group";
        String businessKey = "test_business_key";

        // Mock 查询返回null，但插入时抛出重复键异常
        when(idempotencyMapper.selectByMessageIdAndConsumerGroup(messageId, consumerGroup)).thenReturn(null);
        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup)).thenReturn(null);
        when(idempotencyMapper.insert(any(MqMessageIdempotencyDO.class))).thenThrow(new DuplicateKeyException("Duplicate key"));

        // 执行测试
        boolean result = idempotencyService.checkAndRecord(messageId, consumerGroup, businessKey);

        // 验证结果
        assertFalse(result);
        verify(idempotencyMapper).insert(any(MqMessageIdempotencyDO.class));
    }

    @Test
    public void testMarkSuccess() {
        // 准备测试数据
        String messageId = "test_msg_001";
        String consumerGroup = "test_consumer_group";

        when(idempotencyMapper.updateStatus(messageId, consumerGroup, 
            MqMessageIdempotencyDO.Status.SUCCESS, null)).thenReturn(1);

        // 执行测试
        idempotencyService.markSuccessById(messageId, consumerGroup);

        // 验证调用
        verify(idempotencyMapper).updateStatus(messageId, consumerGroup, 
            MqMessageIdempotencyDO.Status.SUCCESS, null);
    }

    @Test
    public void testMarkFailed() {
        // 准备测试数据
        String messageId = "test_msg_001";
        String consumerGroup = "test_consumer_group";
        String errorMsg = "Test error message";

        when(idempotencyMapper.incrementRetryCount(messageId, consumerGroup)).thenReturn(1);
        when(idempotencyMapper.updateStatus(messageId, consumerGroup, 
            MqMessageIdempotencyDO.Status.FAILED, errorMsg)).thenReturn(1);

        // 执行测试
        idempotencyService.markFailedById(messageId, consumerGroup, errorMsg);

        // 验证调用
        verify(idempotencyMapper).incrementRetryCount(messageId, consumerGroup);
        verify(idempotencyMapper).updateStatus(messageId, consumerGroup, 
            MqMessageIdempotencyDO.Status.FAILED, errorMsg);
    }

    @Test
    public void testIsBusinessProcessed_Processed_ShouldReturnTrue() {
        // 准备测试数据
        String businessKey = "test_business_key";
        String consumerGroup = "test_consumer_group";

        MqMessageIdempotencyDO record = new MqMessageIdempotencyDO();
        record.setBusinessKey(businessKey);
        record.setStatus(MqMessageIdempotencyDO.Status.SUCCESS);

        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup)).thenReturn(record);

        // 执行测试
        boolean result = idempotencyService.isBusinessProcessed(businessKey, consumerGroup);

        // 验证结果
        assertTrue(result);
        verify(idempotencyMapper).selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
    }

    @Test
    public void testIsBusinessProcessed_NotProcessed_ShouldReturnFalse() {
        // 准备测试数据
        String businessKey = "test_business_key";
        String consumerGroup = "test_consumer_group";

        when(idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup)).thenReturn(null);

        // 执行测试
        boolean result = idempotencyService.isBusinessProcessed(businessKey, consumerGroup);

        // 验证结果
        assertFalse(result);
        verify(idempotencyMapper).selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
    }

    @Test
    public void testCleanExpiredRecords() {
        // 准备测试数据
        int expireDays = 30;
        int deletedCount = 100;

        when(idempotencyMapper.deleteExpiredRecords(any(LocalDateTime.class))).thenReturn(deletedCount);

        // 执行测试
        int result = idempotencyService.cleanExpiredRecords(expireDays);

        // 验证结果
        assertEquals(deletedCount, result);
        verify(idempotencyMapper).deleteExpiredRecords(any(LocalDateTime.class));
    }
}
