package com.hissp.distribution.module.material.controller.admin.definition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料定义 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialDefinitionRespVO extends MaterialDefinitionCreateReqVO {

    @Schema(description = "物料ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "关联SPU名称", example = "微电商体验装")
    private String spuName;

    @Schema(description = "转化目标SPU名称", example = "线下体验券")
    private String convertedSpuName;

}
