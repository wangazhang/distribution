package com.hissp.distribution.module.product.enums.packagex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 套包物料类型
 * 1: 商品 2: 权益 3: 自定义
 */
@Getter
@AllArgsConstructor
public enum ProductPackageItemTypeEnum {

    PRODUCT(1, "商品"),
    ENTITLEMENT(2, "权益"),
    CUSTOM(3, "自定义");

    private final Integer code;
    private final String name;
}

