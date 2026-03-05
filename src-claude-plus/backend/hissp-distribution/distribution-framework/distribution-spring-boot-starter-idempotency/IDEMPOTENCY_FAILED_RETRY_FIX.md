# 幂等性服务失败重试修复

## 问题描述

在 `MbTradeOrderRefundSuccessConsumer` 中发现退款幂等性问题：
- 第一次处理失败后，消息状态被标记为 `FAILED`
- 第二次重试时，幂等性检查直接跳过处理，导致失败的消息无法重新处理

## 问题根因

在 `IdempotencyServiceImpl.checkAndRecordWithTopic` 方法中，业务键检查逻辑存在问题：

```java
// 原有问题代码
MqMessageIdempotencyDO businessRecord = idempotencyMapper.selectByBusinessKeyAndConsumerGroup(businessKey, consumerGroup);
if (businessRecord != null) {
    log.info("[checkAndRecordWithTopic][业务已处理过: businessKey={}, status={}]", businessKey, businessRecord.getStatus());
    return false; // 不管什么状态都返回false，这是问题所在
}
```

这导致：
- `SUCCESS` 状态：跳过处理（正确）
- `PROCESSING` 状态：跳过处理（正确，避免并发）
- `FAILED` 状态：跳过处理（**错误**，应该允许重试）

## 修复方案

修改 `checkAndRecordWithTopic` 和 `checkAndRecord` 方法，根据记录状态进行不同处理：

```java
// 修复后的代码
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
```

## 修复逻辑

1. **SUCCESS 状态**：跳过处理（已成功完成）
2. **PROCESSING 状态**：跳过处理（正在处理中，避免并发问题）
3. **FAILED 状态**：更新状态为 PROCESSING，重置错误信息，允许重新处理

## 并发安全性

考虑到数据库表的唯一约束：
- `uk_message_consumer` (`message_id`,`consumer_group`)
- `uk_business_consumer` (`business_key`,`consumer_group`)

最初的删除重插入方案在高并发下可能触发 `DuplicateKeyException`。

**优化方案**：直接更新失败记录的状态为 `PROCESSING`，避免删除重插入的并发问题：
- 原子性操作，避免并发冲突
- 保持记录的连续性和可追溯性
- 更高效的数据库操作

## 修改的文件

1. `IdempotencyServiceImpl.java`
   - 修改 `checkAndRecordWithTopic` 方法
   - 修改 `checkAndRecord` 方法

2. `IdempotencyServiceTest.java`
   - 修复测试方法调用（使用正确的方法签名）

3. 新增测试文件：`IdempotencyServiceFailedRetryTest.java`
   - 验证失败状态记录可以重新处理
   - 验证成功和处理中状态仍然跳过处理

## 测试验证

所有测试通过，包括：
- 新增的失败重试测试（4个测试用例）
- 现有的幂等性测试（24个测试用例）
- 总计：28个测试用例，全部通过

## 影响范围

此修复只影响失败状态的记录处理，不会影响：
- 成功状态的幂等性保护
- 处理中状态的并发保护
- 现有的业务逻辑

## 部署建议

1. 在测试环境验证修复效果
2. 监控幂等性相关日志，确认失败重试正常工作
3. 关注数据库中 `mq_message_idempotency` 表的记录变化

## 相关日志

修复后，失败重试时会看到如下日志：
```
[checkAndRecordWithTopic][业务处理失败，重置为处理中状态: businessKey=xxx, status=2, retryCount=1]
[checkAndRecordWithTopic][重置失败记录为处理中状态成功: businessKey=xxx]
```

这表明系统正确识别了失败状态，重置为处理中状态，并允许重新处理。

## 数据库影响

修复后的行为：
- 失败记录不会被删除，而是状态被更新为 `PROCESSING`
- 错误信息被清空（`error_msg` 设置为 `null`）
- 保持原有的 `retry_count` 和其他字段不变
- 避免了删除重插入可能导致的唯一约束冲突
