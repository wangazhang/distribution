package com.hissp.distribution.module.mb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * MB业务退款类型枚举
 *
 * @author system
 */
@AllArgsConstructor
@Getter
public enum MbRefundTypeEnum {

    /**
     * 物料补货订单退款
     */
    MATERIAL_RESTOCK("MR", "物料补货订单退款"),

    /**
     * 物料转化订单退款
     */
    MATERIAL_CONVERT("MC", "物料转化订单退款"),

    /**
     * MB产品购买退款（普通产品）
     */
    MB_NORMAL_PRODUCT("MB_NORMAL", "MB普通产品购买退款"),

    /**
     * MB代理商开通退款（生涯产品）
     */
    MB_CAREER_PRODUCT("MB_CAREER", "MB代理商开通退款"),

    /**
     * 未知类型
     */
    UNKNOWN("UNKNOWN", "未知退款类型");

    /**
     * 类型标识
     */
    private final String type;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据订单号或前缀判断退款类型
     *
     * @param merchantOrderId 商户订单号
     * @return 退款类型
     */
    public static MbRefundTypeEnum getRefundType(String merchantOrderId) {
        if (merchantOrderId == null || merchantOrderId.trim().isEmpty()) {
            return UNKNOWN;
        }

        // 物料补货订单（MR前缀）
        if (merchantOrderId.startsWith("MR")) {
            return MATERIAL_RESTOCK;
        }

        // 物料转化订单（MC前缀）
        if (merchantOrderId.startsWith("MC")) {
            return MATERIAL_CONVERT;
        }

        // 其他情况需要通过订单信息进一步判断
        return UNKNOWN;
    }

    /**
     * 是否为MB模块内部处理的退款类型
     *
     * @return true-MB模块内部处理，false-需要其他模块处理
     */
    public boolean isMbInternalRefund() {
        return this == MATERIAL_RESTOCK || this == MATERIAL_CONVERT;
    }

    /**
     * 是否为Trade模块处理的MB相关退款类型
     *
     * @return true-Trade模块处理，false-其他模块处理
     */
    public boolean isTradeModuleMbRefund() {
        return this == MB_NORMAL_PRODUCT || this == MB_CAREER_PRODUCT;
    }
}
