package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommissionRuleMaterialReqVO {

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "物料ID不能为空")
    private Long materialId;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料图片")
    private String materialImage;

    @Schema(description = "物料单位")
    private String materialUnit;

    @Schema(description = "发放数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发放数量不能为空")
    @Min(value = 1, message = "发放数量必须大于 0")
    private Integer quantity;
}
