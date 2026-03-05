package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料转化价格响应 DTO
 */
@Data
public class MaterialConvertPriceRespDTO {

    /**
     * 转化规则ID
     */
    private Long ruleId;

    /**
     * 转化价格
     */
    private BigDecimal convertPrice;

    /**
     * 转化规则名称
     */
    private String ruleName;

    /**
     * 源物料名称
     */
    private String sourceMaterialName;

    /**
     * 目标物料名称
     */
    private String targetMaterialName;

}