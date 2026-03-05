package com.hissp.distribution.module.material.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物料类型枚举
 */
@AllArgsConstructor
@Getter
public enum MaterialTypeEnum {

    SEMI_FINISHED(1, "半成品"),
    FINISHED(2, "成品");

    private final Integer type;
    private final String name;

    public static MaterialTypeEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }
        for (MaterialTypeEnum typeEnum : values()) {
            if (typeEnum.getType().equals(type)) {
                return typeEnum;
            }
        }
        return null;
    }

    public static String getNameByType(Integer type) {
        MaterialTypeEnum typeEnum = fromType(type);
        return typeEnum != null ? typeEnum.getName() : "未知类型";
    }
}