package com.hissp.distribution.module.product.enums.packagex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 套包状态枚举
 * 0: 草稿 1: 启用 2: 禁用
 */
@Getter
@AllArgsConstructor
public enum ProductPackageStatusEnum {

    DRAFT(0, "草稿"),
    ENABLE(1, "启用"),
    DISABLE(2, "禁用");

    private final Integer code;
    private final String name;
}

