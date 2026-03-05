package com.hissp.distribution.module.material.controller.admin.inbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;

@Schema(description = "管理后台 - 物料入库创建 Request VO")
@Data
public class MaterialInboundCreateReqVO {

    @Schema(description = "物料ID", required = true, example = "2048")
    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    @Schema(description = "入库数量", required = true, example = "100")
    @NotNull(message = "入库数量不能为空")
    @Min(value = 1, message = "入库数量必须大于0")
    private Integer quantity;

    @Schema(description = "单价（分）", required = true, example = "1000")
    @NotNull(message = "单价不能为空")
    @Min(value = 0, message = "单价不能为负数")
    private Integer unitPrice;

    @Schema(description = "供应商", required = true, example = "XX供应商")
    @NotBlank(message = "供应商不能为空")
    private String supplier;

    @Schema(description = "入库原因", required = true, example = "新采购入库")
    @NotBlank(message = "入库原因不能为空")
    private String reason;

}