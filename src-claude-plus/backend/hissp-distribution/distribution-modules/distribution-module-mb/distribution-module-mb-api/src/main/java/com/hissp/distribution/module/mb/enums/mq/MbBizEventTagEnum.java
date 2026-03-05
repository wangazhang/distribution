package com.hissp.distribution.module.mb.enums.mq;

/**
 * MB业务事件Tag枚举
 * 按照具体业务事件类型设计Tag，用于消息过滤和路由
 * 
 * @author system
 */
public enum MbBizEventTagEnum {

    /**
     * 子公司升级事件
     */
    SUB_COMPANY_UPGRADE("sub-company-upgrade", "子公司升级"),

    /**
     * 会员等级变更事件
     */
    MEMBER_LEVEL_CHANGE("member-level-change", "会员等级变更"),

    /**
     * 补货支付成功事件
     */
    RESTOCK_PAYMENT_SUCCESS("restock-payment-success", "补货支付成功"),

    /**
     * 物料转化事件
     */
    MATERIAL_CONVERT("material-convert", "物料转化"),

    /**
     * 物料库存变更事件
     */
    MATERIAL_STOCK_CHANGE("material-stock-change", "物料库存变更");

    /**
     * Tag名称
     */
    private final String tag;

    /**
     * Tag描述
     */
    private final String description;

    MbBizEventTagEnum(String tag, String description) {
        this.tag = tag;
        this.description = description;
    }

    public String getTag() {
        return tag;
    }

    public String getDescription() {
        return description;
    }
}
