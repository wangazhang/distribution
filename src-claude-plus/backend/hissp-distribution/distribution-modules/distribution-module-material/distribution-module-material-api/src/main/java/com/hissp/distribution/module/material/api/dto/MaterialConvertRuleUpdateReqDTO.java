package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 物料转化规则更新请求 DTO
 */
@Data
public class MaterialConvertRuleUpdateReqDTO {

    /**
     * 规则ID
     */
    @NotNull(message = "规则ID不能为空")
    private Long id;

    /**
     * 转化规则名称
     */
    @NotBlank(message = "转化规则名称不能为空")
    private String ruleName;

    /**
     * 源物料ID
     */
    @NotNull(message = "源物料ID不能为空")
    private Long sourceMaterialId;

    /**
     * 目标物料ID
     */
    @NotNull(message = "目标物料ID不能为空")
    private Long targetMaterialId;

    /**
     * 转化比例
     */
    @NotNull(message = "转化比例不能为空")
    @DecimalMin(value = "0.0001", message = "转化比例必须大于0.0001")
    private BigDecimal convertRatio;

    /**
     * 转化费用
     */
    @DecimalMin(value = "0", message = "转化费用不能为负数")
    private BigDecimal convertPrice;

    /**
     * 状态（0:禁用 1:启用）
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 扩展属性
     */
    private String attrs;

}