package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料转化规则响应 DTO
 */
@Data
public class MaterialConvertRuleRespDTO {

    /**
     * 规则ID
     */
    private Long id;

    /**
     * 转化规则名称
     */
    private String ruleName;

    /**
     * 源物料ID
     */
    private Long sourceMaterialId;

    /**
     * 源物料名称
     */
    private String sourceMaterialName;

    /**
     * 源物料编码
     */
    private String sourceMaterialCode;

    /**
     * 目标物料ID
     */
    private Long targetMaterialId;

    /**
     * 目标物料名称
     */
    private String targetMaterialName;

    /**
     * 目标物料编码
     */
    private String targetMaterialCode;

    /**
     * 转化比例
     */
    private BigDecimal convertRatio;

    /**
     * 转化费用
     */
    private BigDecimal convertPrice;

    /**
     * 状态：0禁用 1启用
     */
    private Integer status;

    /**
     * 规则描述
     */
    private String description;

}