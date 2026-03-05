package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料转化结果响应 DTO
 */
@Data
public class MaterialConvertResultRespDTO {

    /**
     * 转化记录ID
     */
    private Long convertRecordId;

    /**
     * 转化是否成功
     */
    private Boolean success;

    /**
     * 失败原因（如果转化失败）
     */
    private String failureReason;

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
     * 消耗的源物料数量
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
     * 获得的目标物料数量
     */
    private Integer targetQuantity;

    /**
     * 转化费用
     */
    private BigDecimal convertPrice;

    /**
     * 转化时间
     */
    private LocalDateTime convertTime;

}