package com.hissp.distribution.module.mb.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MB业务事件Key工具类
 * 用于生成消息Key，格式为：{业务类型}:{业务ID}:{时间戳}
 * 确保消息的可搜索性和唯一性
 * 
 * @author system
 */
public class MbBusinessEventKeyUtils {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 生成子公司升级事件Key
     *
     * @param userId 用户ID
     * @return 事件Key
     */
    public static String generateSubCompanyUpgradeKey(Long userId) {
        return generateKey("sub-company-upgrade", userId);
    }

    /**
     * 生成会员等级变更事件Key
     *
     * @param userId 用户ID
     * @return 事件Key
     */
    public static String generateMemberLevelChangeKey(Long userId) {
        return generateKey("member-level-change", userId);
    }

    /**
     * 生成补货支付成功事件Key
     *
     * @param payOrderId 支付订单ID
     * @return 事件Key
     */
    public static String generateRestockPaymentSuccessKey(Long payOrderId) {
        return generateKey("restock-payment", payOrderId);
    }

    /**
     * 生成物料转化事件Key
     *
     * @param payOrderId 支付订单ID
     * @return 事件Key
     */
    public static String generateMaterialConvertKey(Long payOrderId) {
        return generateKey("material-convert", payOrderId);
    }

    /**
     * 生成物料库存变更事件Key
     *
     * @param materialId 物料ID
     * @return 事件Key
     */
    public static String generateMaterialStockChangeKey(Long materialId) {
        return generateKey("material-stock", materialId);
    }

    /**
     * 生成通用事件Key
     *
     * @param businessType 业务类型
     * @param businessId 业务ID
     * @return 事件Key，格式：{业务类型}:{业务ID}:{时间戳}
     */
    private static String generateKey(String businessType, Long businessId) {
        String timestamp = LocalDateTime.now().format(DATETIME_FORMATTER);
        return String.format("%s:%d:%s", businessType, businessId, timestamp);
    }
}
