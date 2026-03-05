package com.hissp.distribution.module.material.controller.admin.convert.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料转化规则 Response VO")
@Data
public class MaterialConvertRuleRespVO {

    @Schema(description = "规则ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "转化规则名称", required = true, example = "积分转金币规则")
    private String ruleName;

    @Schema(description = "源物料ID", required = true, example = "1024")
    private Long sourceMaterialId;

    @Schema(description = "源物料名称", required = true, example = "积分")
    private String sourceMaterialName;

    @Schema(description = "源物料单位", example = "个")
    private String sourceMaterialUnit;

    @Schema(description = "目标物料ID", required = true, example = "2048")
    private Long targetMaterialId;

    @Schema(description = "目标物料名称", required = true, example = "金币")
    private String targetMaterialName;

    @Schema(description = "目标物料单位", example = "个")
    private String targetMaterialUnit;

    @Schema(description = "转化比例", required = true, example = "1.5")
    private BigDecimal convertRatio;

    @Schema(description = "转化费用", example = "10.50")
    private BigDecimal convertPrice;

    @Schema(description = "状态（0:禁用 1:启用）", required = true, example = "1")
    private Integer status;

    @Schema(description = "状态名称", required = true, example = "启用")
    private String statusName;

    @Schema(description = "规则描述", example = "1个积分可以转化为1.5个金币")
    private String description;

    @Schema(description = "扩展属性", example = "{\"maxDailyConvert\":100}")
    private String attrs;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

}