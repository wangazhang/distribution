package com.hissp.distribution.module.material.controller.admin.inbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 物料入库审核 Request VO")
@Data
public class MaterialInboundApproveReqVO {

    @Schema(description = "入库单ID", required = true, example = "1")
    @NotNull(message = "入库单ID不能为空")
    private Long id;

    @Schema(description = "审核结果", required = true, example = "true")
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;

    @Schema(description = "审核意见", required = true, example = "审核通过")
    @NotBlank(message = "审核意见不能为空")
    private String approveReason;

}