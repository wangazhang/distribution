package com.hissp.distribution.module.material.controller.admin.convert.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 物料转化规则创建 Request VO")
@Data
public class MaterialConvertRuleCreateReqVO {

    @Schema(description = "转化规则名称", required = true, example = "积分转金币规则")
    @NotBlank(message = "转化规则名称不能为空")
    private String ruleName;

    @Schema(description = "源物料ID", required = true, example = "1024")
    @NotNull(message = "源物料ID不能为空")
    private Long sourceMaterialId;

    @Schema(description = "目标物料ID", required = true, example = "2048")
    @NotNull(message = "目标物料ID不能为空")
    private Long targetMaterialId;

    @Schema(description = "转化比例", required = true, example = "1.5")
    @NotNull(message = "转化比例不能为空")
    @DecimalMin(value = "0.0001", message = "转化比例必须大于0.0001")
    private BigDecimal convertRatio;

    @Schema(description = "转化费用", example = "10.50")
    @DecimalMin(value = "0", message = "转化费用不能为负数")
    private BigDecimal convertPrice;

    @Schema(description = "状态（0:禁用 1:启用）", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "规则描述", example = "1个积分可以转化为1.5个金币")
    private String description;

    @Schema(description = "扩展属性", example = "{\"maxDailyConvert\":100}")
    private String attrs;

}