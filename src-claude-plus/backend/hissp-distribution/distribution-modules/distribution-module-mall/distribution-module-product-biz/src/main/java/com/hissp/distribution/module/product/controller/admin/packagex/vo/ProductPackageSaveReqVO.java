package com.hissp.distribution.module.product.controller.admin.packagex.vo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductPackageSaveReqVO {

    private Long id; // 更新时必填

    @NotBlank(message = "name 不能为空")
    private String name;

    @NotNull(message = "spuId 不能为空")
    private Long spuId;

    /** 0:草稿 1:启用 2:禁用 */
    @NotNull(message = "status 不能为空")
    private Integer status;

    private String remark;

    @NotEmpty(message = "items 不能为空")
    @Valid
    private List<ProductPackageItemVO> items;

    @Valid
    private List<ProductPackageCommissionVO> commissions;
}

