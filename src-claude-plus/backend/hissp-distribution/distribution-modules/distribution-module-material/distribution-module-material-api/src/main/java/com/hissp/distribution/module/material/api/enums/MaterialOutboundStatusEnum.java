package com.hissp.distribution.module.material.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物料出库状态枚举
 */
@Getter
@AllArgsConstructor
public enum MaterialOutboundStatusEnum {

    PENDING(0, "待审核"),
    APPROVED(1, "已审核待发货"),
    SHIPPED(2, "已发货"),
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REJECTED(5, "审核拒绝");

    /**
     * 状态值
     */
    private final Integer status;
    
    /**
     * 状态名称
     */
    private final String name;

}