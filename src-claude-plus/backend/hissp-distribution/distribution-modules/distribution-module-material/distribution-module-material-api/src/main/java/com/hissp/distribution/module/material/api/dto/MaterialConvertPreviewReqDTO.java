package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 物料转化预览请求 DTO
 */
@Data
public class MaterialConvertPreviewReqDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 转化规则ID
     */
    @NotNull(message = "转化规则ID不能为空")
    private Long ruleId;

    /**
     * 源物料数量
     */
    @NotNull(message = "源物料数量不能为空")
    @Positive(message = "源物料数量必须大于0")
    private Integer sourceQuantity;

}