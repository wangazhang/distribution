package com.hissp.distribution.module.material.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物料转化状态枚举
 */
@AllArgsConstructor
@Getter
public enum MaterialConvertStatusEnum {

    NOT_CONVERTIBLE(0, "不可转化"),
    CONVERTIBLE(1, "可转化"),
    CONVERTED(2, "已转化");

    private final Integer status;
    private final String name;

    public static MaterialConvertStatusEnum fromStatus(Integer status) {
        if (status == null) {
            return null;
        }
        for (MaterialConvertStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus().equals(status)) {
                return statusEnum;
            }
        }
        return null;
    }

    public static String getNameByStatus(Integer status) {
        MaterialConvertStatusEnum statusEnum = fromStatus(status);
        return statusEnum != null ? statusEnum.getName() : "未知状态";
    }
}