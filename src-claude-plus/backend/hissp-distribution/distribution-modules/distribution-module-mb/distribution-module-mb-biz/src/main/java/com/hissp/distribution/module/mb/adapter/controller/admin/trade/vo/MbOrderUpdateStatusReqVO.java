package com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台 - MB 订单状态更新 Request VO
 */
@Data
public class MbOrderUpdateStatusReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15504")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "目标状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "COMPLETED")
    @NotBlank(message = "目标状态不能为空")
    private String status;

    @Schema(description = "备注", example = "手动调整订单状态")
    private String remark;
}
