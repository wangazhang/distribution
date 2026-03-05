package com.hissp.distribution.module.material.controller.admin.balance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "管理后台 - 物料余额调整 Request VO")
@Data
public class MaterialBalanceAdjustReqVO {

    @Schema(description = "用户ID", required = true, example = "1024")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @Schema(description = "物料ID", required = true, example = "2048")
    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    @Schema(description = "调整数量", required = true, example = "100")
    @NotNull(message = "调整数量不能为空")
    private Integer adjustAmount;

    @Schema(description = "调整类型", required = true, example = "1")
    @NotNull(message = "调整类型不能为空")
    private Integer adjustType; // 1-增加 0-减少

    @Schema(description = "调整原因", required = true, example = "管理员手动调整")
    @NotBlank(message = "调整原因不能为空")
    private String reason;

}