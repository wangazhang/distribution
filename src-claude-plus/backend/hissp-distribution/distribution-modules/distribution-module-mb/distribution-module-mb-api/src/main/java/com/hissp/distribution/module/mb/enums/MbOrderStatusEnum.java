package com.hissp.distribution.module.mb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户物料订单状态枚举
 * 整合订单处理状态和退款状态，避免字段冗余
 *
 * @author system
 */
@AllArgsConstructor
@Getter
public enum MbOrderStatusEnum {

    /**
     * 待处理 - 订单已创建，等待支付
     */
    PENDING("PENDING", "待处理", 1),

    /**
     * 处理中 - 支付成功，正在处理物料分配
     */
    PROCESSING("PROCESSING", "处理中", 2),

    /**
     * 待确认收货 - 虚拟发货已完成，等待用户确认
     */
    DELIVERED("DELIVERED", "待确认收货", 3),

    /**
     * 已完成 - 物料分配完成，订单正常结束
     */
    COMPLETED("COMPLETED", "已完成", 4),

    /**
     * 处理失败 - 物料分配失败
     */
    FAILED("FAILED", "处理失败", 5),

    /**
     * 退款中 - 正在处理退款逆操作
     */
    REFUNDING("REFUNDING", "退款中", 6),

    /**
     * 已退款 - 退款逆操作完成
     */
    REFUNDED("REFUNDED", "已退款", 7),

    /**
     * 退款失败 - 退款逆操作失败
     */
    REFUND_FAILED("REFUND_FAILED", "退款失败", 8);

    /**
     * 状态代码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String description;
    /**
     * App 前端展示使用的状态编码（保持有序，避免直接依赖枚举顺序）
     */
    private final int appCode;

    /**
     * 根据代码获取枚举
     *
     * @param code 状态代码
     * @return 状态枚举
     */
    public static MbOrderStatusEnum fromCode(String code) {
        for (MbOrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知的订单状态代码: " + code);
    }

    /**
     * 判断是否为终态（不可再变更的状态）
     *
     * @return true-终态，false-可变更状态
     */
    public boolean isFinalStatus() {
        return this == COMPLETED || this == REFUNDED || this == REFUND_FAILED;
    }

    /**
     * 判断是否为退款相关状态
     *
     * @return true-退款相关，false-非退款状态
     */
    public boolean isRefundRelated() {
        return this == REFUNDING || this == REFUNDED || this == REFUND_FAILED;
    }

    /**
     * 判断是否可以发起退款
     *
     * @return true-可以退款，false-不可退款
     */
    public boolean canRefund() {
        return this == DELIVERED || this == COMPLETED;
    }

    /**
     * 判断是否已经退款成功
     *
     * @return true-已退款，false-未退款
     */
    public boolean isRefunded() {
        return this == REFUNDED;
    }

    /**
     * 获取状态流转规则
     *
     * @param targetStatus 目标状态
     * @return true-可以流转，false-不可流转
     */
    public boolean canTransitionTo(MbOrderStatusEnum targetStatus) {
        switch (this) {
            case PENDING:
                return targetStatus == PROCESSING || targetStatus == FAILED;
            case PROCESSING:
                return targetStatus == DELIVERED || targetStatus == COMPLETED || targetStatus == FAILED;
            case DELIVERED:
                return targetStatus == COMPLETED || targetStatus == REFUNDING;
            case COMPLETED:
                return targetStatus == REFUNDING;
            case FAILED:
                return false; // 失败状态不可流转
            case REFUNDING:
                return targetStatus == REFUNDED || targetStatus == REFUND_FAILED;
            case REFUNDED:
            case REFUND_FAILED:
                return false; // 终态不可流转
            default:
                return false;
        }
    }

    /**
     * App 端状态编码
     *
     * @return 数字编码
     */
    public int getAppCode() {
        return appCode;
    }
}
