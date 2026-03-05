package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommissionSimulationRuleMaterialRespVO {

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "物料名称")
    private String materialName;

    @Schema(description = "物料编码")
    private String materialCode;

    @Schema(description = "物料图片")
    private String materialImage;

    @Schema(description = "单位")
    private String materialUnit;

    @Schema(description = "发放数量")
    private Integer quantity;
}
