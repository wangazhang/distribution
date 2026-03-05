package com.hissp.distribution.framework.idempotency.utils;

import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 幂等性工具类（纯技术工具，不包含业务逻辑）
 * 提供通用的幂等性相关工具方法
 *
 * 重构说明：
 * 1. 移除所有业务特定的键生成方法
 * 2. 保留通用的技术工具方法
 * 3. 提供参数化的通用键生成方法
 * 4. 业务键生成逻辑移至各业务模块的常量类
 *
 * @author system
 */
public class IdempotencyUtils {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    // ==================== 通用业务键生成方法 ====================

    /**
     * 生成简单的业务键（业务类型 + ID）
     *
     * @param businessType 业务类型前缀（由业务模块常量提供）
     * @param id 业务ID
     * @return 业务键
     */
    public static String generateSimpleBusinessKey(String businessType, Long id) {
        validateBusinessType(businessType);
        return businessType + "_" + id;
    }

    /**
     * 生成复合业务键（业务类型 + 多个ID）
     *
     * @param businessType 业务类型前缀（由业务模块常量提供）
     * @param ids 业务ID数组
     * @return 业务键
     */
    public static String generateCompositeBusinessKey(String businessType, Long... ids) {
        validateBusinessType(businessType);
        if (ids == null || ids.length == 0) {
            throw new IllegalArgumentException("业务ID不能为空");
        }

        StringBuilder sb = new StringBuilder(businessType);
        for (Long id : ids) {
            sb.append("_").append(id);
        }
        return sb.toString();
    }

    /**
     * 生成基于时间戳的业务键
     *
     * @param businessType 业务类型前缀（由业务模块常量提供）
     * @param id 业务ID
     * @param timestamp 时间戳
     * @return 业务键
     */
    public static String generateTimestampBusinessKey(String businessType, Long id, LocalDateTime timestamp) {
        validateBusinessType(businessType);
        String timeStr = timestamp.format(DATETIME_FORMATTER);
        return String.format("%s_%d_%s", businessType, id, timeStr);
    }

    /**
     * 生成多参数业务键
     *
     * @param businessType 业务类型前缀（由业务模块常量提供）
     * @param params 参数数组
     * @return 业务键
     */
    public static String generateParameterizedBusinessKey(String businessType, Object... params) {
        validateBusinessType(businessType);
        if (params == null || params.length == 0) {
            throw new IllegalArgumentException("参数不能为空");
        }

        StringBuilder sb = new StringBuilder(businessType);
        for (Object param : params) {
            sb.append("_").append(param);
        }
        return sb.toString();
    }

    // ==================== 消息ID生成方法 ====================

    /**
     * 生成自定义消息ID
     * 当RocketMQ消息ID不可用时使用
     *
     * @param consumerGroup 消费者组
     * @param businessKey 业务键
     * @return 自定义消息ID
     */
    public static String generateCustomMessageId(String consumerGroup, String businessKey) {
        String source = consumerGroup + "_" + businessKey + "_" + System.currentTimeMillis();
        return DigestUtils.md5DigestAsHex(source.getBytes(StandardCharsets.UTF_8));
    }

    // ==================== 验证和工具方法 ====================

    /**
     * 验证业务键格式
     *
     * @param businessKey 业务键
     * @return 是否有效
     */
    public static boolean isValidBusinessKey(String businessKey) {
        return StringUtils.hasText(businessKey) && businessKey.length() <= 200;
    }

    /**
     * 验证消息ID格式
     *
     * @param messageId 消息ID
     * @return 是否有效
     */
    public static boolean isValidMessageId(String messageId) {
        return StringUtils.hasText(messageId) && messageId.length() <= 64;
    }

    /**
     * 截断过长的错误信息
     *
     * @param errorMsg 错误信息
     * @param maxLength 最大长度
     * @return 截断后的错误信息
     */
    public static String truncateErrorMsg(String errorMsg, int maxLength) {
        if (errorMsg == null) {
            return null;
        }
        if (errorMsg.length() <= maxLength) {
            return errorMsg;
        }
        return errorMsg.substring(0, maxLength - 3) + "...";
    }

    /**
     * 获取日期时间格式化器（供业务模块使用）
     *
     * @return 日期时间格式化器
     */
    public static DateTimeFormatter getDateTimeFormatter() {
        return DATETIME_FORMATTER;
    }

    /**
     * 验证业务类型前缀
     *
     * @param businessType 业务类型前缀
     */
    private static void validateBusinessType(String businessType) {
        if (!StringUtils.hasText(businessType)) {
            throw new IllegalArgumentException("业务类型前缀不能为空");
        }
        if (businessType.length() > 50) {
            throw new IllegalArgumentException("业务类型前缀长度不能超过50个字符");
        }
        // 允许下划线，这是合理的命名约定
        // 只检查是否包含不合法的字符（如空格、特殊符号等）
        if (!businessType.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("业务类型前缀只能包含字母、数字和下划线");
        }
    }

    // ==================== 兼容性方法（标记为过时，建议迁移） ====================

    /**
     * 生成统一的订单支付成功业务键
     *
     * @param orderId 订单ID
     * @return 业务键
     * @deprecated 请使用 {@link #generateSimpleBusinessKey(String, Long)}
     *             并传入业务常量，如：generateSimpleBusinessKey(TradeOrderConstants.PAYMENT_SUCCESS_KEY_PREFIX, orderId)
     */
    @Deprecated
    public static String generateOrderPaymentSuccessBusinessKey(Long orderId) {
        return generateSimpleBusinessKey("order_payment_success", orderId);
    }

    /**
     * 生成Trade订单退款成功业务键
     *
     * @param orderId 订单ID
     * @param payRefundId 支付退款ID
     * @return 业务键
     * @deprecated 请使用 {@link #generateCompositeBusinessKey(String, Long...)}
     *             并传入业务常量，如：generateCompositeBusinessKey(TradeOrderConstants.REFUND_SUCCESS_KEY_PREFIX, orderId, payRefundId)
     */
    @Deprecated
    public static String generateTradeOrderRefundSuccessBusinessKey(Long orderId, Long payRefundId) {
        return generateCompositeBusinessKey("trade_order_refund_success", orderId, payRefundId);
    }
}
