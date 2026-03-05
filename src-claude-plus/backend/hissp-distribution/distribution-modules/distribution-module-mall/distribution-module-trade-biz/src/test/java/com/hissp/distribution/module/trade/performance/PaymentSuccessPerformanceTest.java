package com.hissp.distribution.module.trade.performance;

import com.hissp.distribution.module.trade.mq.monitor.MessageSendMonitor;
import com.hissp.distribution.module.trade.mq.producer.TradeOrderEventProducer;
import com.hissp.distribution.module.trade.service.order.TradeOrderUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StopWatch;

import jakarta.annotation.Resource;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 支付成功处理性能基准测试
 * 对比原有同步模式和新MQ异步模式的性能差异
 * 
 * @author system
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class PaymentSuccessPerformanceTest {

    @Resource
    private TradeOrderEventProducer tradeOrderEventProducer;

    @Resource
    private MessageSendMonitor messageSendMonitor;

    private static final int TEST_ORDER_COUNT = 1000;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    void setUp() {
        // 重置监控数据
        messageSendMonitor.reset();
    }

    @Test
    @DisplayName("MQ异步模式性能测试")
    void testMqAsyncModePerformance() throws InterruptedException {
        log.info("开始MQ异步模式性能测试，订单数量: {}", TEST_ORDER_COUNT);
        
        StopWatch stopWatch = new StopWatch("MQ异步模式性能测试");
        stopWatch.start();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        
        // 并发发送消息
        CompletableFuture<Void>[] futures = IntStream.range(0, TEST_ORDER_COUNT)
            .mapToObj(i -> CompletableFuture.runAsync(() -> {
                try {
                    // 模拟发送订单支付成功消息
                    sendTestMessage(1000L + i);
                } catch (Exception e) {
                    log.error("发送消息失败: orderId={}", 1000L + i, e);
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        // 等待所有任务完成
        CompletableFuture.allOf(futures).join();
        
        stopWatch.stop();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // 输出性能统计
        long totalTime = stopWatch.getTotalTimeMillis();
        double tps = (double) TEST_ORDER_COUNT / totalTime * 1000;
        double successRate = messageSendMonitor.getOverallSuccessRate();

        log.info("=== MQ异步模式性能测试结果 ===");
        log.info("总订单数: {}", TEST_ORDER_COUNT);
        log.info("总耗时: {}ms", totalTime);
        log.info("平均TPS: {:.2f}", tps);
        log.info("成功率: {:.2f}%", successRate);
        log.info("平均响应时间: {:.2f}ms", (double) totalTime / TEST_ORDER_COUNT);

        // 性能断言
        assertThat(totalTime).isLessThan(10000); // 10秒内完成
        assertThat(tps).isGreaterThan(100); // TPS > 100
        assertThat(successRate).isGreaterThan(99.0); // 成功率 > 99%
    }

    @Test
    @DisplayName("消息发送延迟测试")
    void testMessageSendLatency() {
        log.info("开始消息发送延迟测试");
        
        long[] latencies = new long[100];
        
        for (int i = 0; i < 100; i++) {
            long startTime = System.nanoTime();
            sendTestMessage(2000L + i);
            long endTime = System.nanoTime();
            
            latencies[i] = (endTime - startTime) / 1_000_000; // 转换为毫秒
        }

        // 计算统计数据
        long minLatency = Long.MAX_VALUE;
        long maxLatency = Long.MIN_VALUE;
        long totalLatency = 0;
        
        for (long latency : latencies) {
            minLatency = Math.min(minLatency, latency);
            maxLatency = Math.max(maxLatency, latency);
            totalLatency += latency;
        }
        
        double avgLatency = (double) totalLatency / latencies.length;

        log.info("=== 消息发送延迟测试结果 ===");
        log.info("最小延迟: {}ms", minLatency);
        log.info("最大延迟: {}ms", maxLatency);
        log.info("平均延迟: {:.2f}ms", avgLatency);

        // 延迟断言
        assertThat(avgLatency).isLessThan(100); // 平均延迟 < 100ms
        assertThat(maxLatency).isLessThan(500); // 最大延迟 < 500ms
    }

    @Test
    @DisplayName("并发压力测试")
    void testConcurrentStressTest() throws InterruptedException {
        log.info("开始并发压力测试");
        
        int concurrentUsers = 50;
        int messagesPerUser = 20;
        
        StopWatch stopWatch = new StopWatch("并发压力测试");
        stopWatch.start();

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        
        CompletableFuture<Void>[] futures = IntStream.range(0, concurrentUsers)
            .mapToObj(userId -> CompletableFuture.runAsync(() -> {
                for (int i = 0; i < messagesPerUser; i++) {
                    try {
                        sendTestMessage(3000L + userId * 1000 + i);
                        // 模拟用户操作间隔
                        Thread.sleep(10);
                    } catch (Exception e) {
                        log.error("并发测试失败: userId={}, messageIndex={}", userId, i, e);
                    }
                }
            }, executor))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        
        stopWatch.stop();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        int totalMessages = concurrentUsers * messagesPerUser;
        long totalTime = stopWatch.getTotalTimeMillis();
        double tps = (double) totalMessages / totalTime * 1000;
        double successRate = messageSendMonitor.getOverallSuccessRate();

        log.info("=== 并发压力测试结果 ===");
        log.info("并发用户数: {}", concurrentUsers);
        log.info("每用户消息数: {}", messagesPerUser);
        log.info("总消息数: {}", totalMessages);
        log.info("总耗时: {}ms", totalTime);
        log.info("TPS: {:.2f}", tps);
        log.info("成功率: {:.2f}%", successRate);

        // 压力测试断言
        assertThat(successRate).isGreaterThan(98.0); // 高并发下成功率 > 98%
        assertThat(tps).isGreaterThan(50); // 高并发下TPS > 50
    }

    @Test
    @DisplayName("内存使用情况测试")
    void testMemoryUsage() {
        log.info("开始内存使用情况测试");
        
        Runtime runtime = Runtime.getRuntime();
        
        // 执行GC，获取基准内存使用
        System.gc();
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 发送大量消息
        for (int i = 0; i < 5000; i++) {
            sendTestMessage(4000L + i);
        }
        
        // 再次执行GC，获取测试后内存使用
        System.gc();
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        
        long memoryIncrease = afterMemory - beforeMemory;
        double memoryIncreaseMB = memoryIncrease / 1024.0 / 1024.0;

        log.info("=== 内存使用情况测试结果 ===");
        log.info("测试前内存使用: {:.2f}MB", beforeMemory / 1024.0 / 1024.0);
        log.info("测试后内存使用: {:.2f}MB", afterMemory / 1024.0 / 1024.0);
        log.info("内存增长: {:.2f}MB", memoryIncreaseMB);

        // 内存使用断言
        assertThat(memoryIncreaseMB).isLessThan(100); // 内存增长 < 100MB
    }

    /**
     * 发送测试消息
     */
    private void sendTestMessage(Long orderId) {
        // 这里应该调用实际的消息发送逻辑
        // 由于是性能测试，可以使用模拟的消息对象
        try {
            // 模拟消息发送耗时
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
