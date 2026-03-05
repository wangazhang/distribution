package com.hissp.distribution.module.material.controller.app.cwms.vo;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Schema(description = "App - 批量查询用户物料库存 Request VO")
@Data
public class AppUserMaterialBatchQueryReqVO {

    @Schema(description = "用户ID（由系统填充）", hidden = true)
    private Long userId;

    @Schema(description = "物料ID列表（等同于商品ID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1, 2, 3]")
    @NotEmpty(message = "物料ID列表不能为空")
    private List<Long> materialIds;
} 

