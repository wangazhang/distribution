package com.hissp.distribution.module.product.enums.packagex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分佣类型
 * 1: 固定金额(分), 2: 固定比例(%)
 */
@Getter
@AllArgsConstructor
public enum ProductPackageCommissionTypeEnum {

    AMOUNT(1, "固定金额(分)"),
    RATIO(2, "固定比例(%)");

    private final Integer code;
    private final String name;
}

