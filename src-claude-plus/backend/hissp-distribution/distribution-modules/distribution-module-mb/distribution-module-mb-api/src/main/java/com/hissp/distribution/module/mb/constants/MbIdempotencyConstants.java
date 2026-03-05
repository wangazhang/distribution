package com.hissp.distribution.module.mb.constants;

/**
 * MB模块幂等性业务键常量
 * 纯常量定义，不包含任何方法调用，实现真正的解耦
 *
 * @author system
 */
public class MbIdempotencyConstants {

    // ==================== 业务键前缀常量 ====================

    /**
     * 普通产品购买支付成功业务键前缀
     */
    public static final String NORMAL_PRODUCT_PAYMENT_PREFIX = "normal_product_payment";

    /**
     * 生涯产品购买支付成功业务键前缀
     */
    public static final String CAREER_PRODUCT_PAYMENT_PREFIX = "career_product_payment";

    /**
     * 补货支付成功业务键前缀
     */
    public static final String RESTOCK_PAYMENT_PREFIX = "restock_payment";

    /**
     * 物料转化支付成功业务键前缀
     */
    public static final String MATERIAL_CONVERT_PAYMENT_PREFIX = "material_convert_payment";

    /**
     * 会员等级变更业务键前缀
     */
    public static final String MEMBER_LEVEL_CHANGE_PREFIX = "member_level_change";

    /**
     * 子公司升级业务键前缀
     */
    public static final String SUB_COMPANY_UPGRADE_PREFIX = "sub_company_upgrade";

    // 私有构造函数，防止实例化
    private MbIdempotencyConstants() {
    }
}
