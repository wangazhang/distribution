package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 物料转化预览响应 DTO
 */
@Data
public class MaterialConvertPreviewRespDTO {

    /**
     * 转化规则ID
     */
    private Long ruleId;

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
     * 源物料数量
     */
    private Integer sourceQuantity;

    /**
     * 目标物料ID
     */
    private Long targetMaterialId;

    /**
     * 目标物料名称
     */
    private String targetMaterialName;

    /**
     * 目标物料数量
     */
    private Integer targetQuantity;

    /**
     * 转化比例
     */
    private BigDecimal convertRatio;

    /**
     * 转化费用
     */
    private BigDecimal convertPrice;

    /**
     * 总转化费用
     */
    private BigDecimal totalConvertPrice;

    /**
     * 用户当前余额是否充足
     */
    private Boolean hasEnoughBalance;

    /**
     * 用户当前源物料余额
     */
    private Integer currentSourceBalance;

}