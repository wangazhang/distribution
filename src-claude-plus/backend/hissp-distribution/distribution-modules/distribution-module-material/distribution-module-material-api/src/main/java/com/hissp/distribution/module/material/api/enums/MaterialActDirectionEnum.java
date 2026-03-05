package com.hissp.distribution.module.material.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 物料动作方向枚举
 */
@Getter
@AllArgsConstructor
public enum MaterialActDirectionEnum {

    IN(1, "入账"),
    OUT(-1, "回退");

    private final Integer value;
    private final String description;

}

