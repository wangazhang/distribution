package com.hissp.distribution.module.mb.enums;

import lombok.Getter;

/**
 * 首页配置状态枚举
 */
@Getter
public enum MbHomeConfigStatusEnum {

    DISABLED(0, "未生效"),
    ENABLED(1, "已生效");

    /**
     * 状态值
     */
    private final Integer status;
    /**
     * 状态名
     */
    private final String name;
    
    MbHomeConfigStatusEnum(Integer status, String name) {
        this.status = status;
        this.name = name;
    }
} 