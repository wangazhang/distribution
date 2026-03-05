package com.hissp.distribution.module.material.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物料SPU关联模式枚举
 */
@AllArgsConstructor
@Getter
public enum MaterialLinkModeEnum {

    MAPPING(1, "映射", "实时映射SPU信息"),
    SNAPSHOT(2, "快照", "保存SPU信息快照");

    private final Integer mode;
    private final String name;
    private final String description;

    public static MaterialLinkModeEnum fromMode(Integer mode) {
        if (mode == null) {
            return null;
        }
        for (MaterialLinkModeEnum modeEnum : values()) {
            if (modeEnum.getMode().equals(mode)) {
                return modeEnum;
            }
        }
        return null;
    }

    public static String getNameByMode(Integer mode) {
        MaterialLinkModeEnum modeEnum = fromMode(mode);
        return modeEnum != null ? modeEnum.getName() : "未知模式";
    }
}