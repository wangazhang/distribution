package com.hissp.distribution.module.promotion.dal.dataobject.cms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CMS审核状态枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum AuditStatusEnum {

    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已拒绝");

    /**
     * 状态
     */
    private final String status;

    /**
     * 描述
     */
    private final String description;

}
