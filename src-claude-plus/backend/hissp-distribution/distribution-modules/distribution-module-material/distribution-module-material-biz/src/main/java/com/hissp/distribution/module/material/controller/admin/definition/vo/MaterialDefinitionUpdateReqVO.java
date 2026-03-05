package com.hissp.distribution.module.material.controller.admin.definition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 物料定义更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialDefinitionUpdateReqVO extends MaterialDefinitionCreateReqVO {

    @Schema(description = "物料ID", required = true, example = "1024")
    @NotNull(message = "物料ID不能为空")
    private Long id;

}

