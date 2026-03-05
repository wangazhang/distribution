package com.hissp.distribution.module.product.controller.admin.packagex.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductPackageCommissionVO {

    /** 1: 上级, 2: 上上级 */
    @NotNull(message = "level 不能为空")
    private Integer level;

    /** 1: 固定金额(分), 2: 固定比例(%) */
    @NotNull(message = "commissionType 不能为空")
    private Integer commissionType;

    @NotNull(message = "commissionValue 不能为空")
    @DecimalMin(value = "0.01", message = "commissionValue 必须大于 0")
    private BigDecimal commissionValue;

    /** 1:订单项实付, 2:自定义基准 */
    private Integer baseType;

    /** 自定义基准(分) */
    private Integer baseAmount;
}

