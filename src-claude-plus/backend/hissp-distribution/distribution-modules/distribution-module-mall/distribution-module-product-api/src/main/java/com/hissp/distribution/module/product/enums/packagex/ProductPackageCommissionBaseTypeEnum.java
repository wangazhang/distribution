package com.hissp.distribution.module.product.enums.packagex;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 分佣基准类型
 * 1: 订单项实付, 2: 自定义基准
 */
@Getter
@AllArgsConstructor
public enum ProductPackageCommissionBaseTypeEnum {

    ORDER_ITEM_PAID(1, "订单项实付"),
    CUSTOM(2, "自定义基准");

    private final Integer code;
    private final String name;
}

