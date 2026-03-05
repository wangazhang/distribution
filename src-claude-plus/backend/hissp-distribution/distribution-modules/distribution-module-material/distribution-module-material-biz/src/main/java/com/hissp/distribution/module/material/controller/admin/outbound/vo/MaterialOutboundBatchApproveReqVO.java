package com.hissp.distribution.module.material.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 物料出库批量审核 Request VO")
@Data
public class MaterialOutboundBatchApproveReqVO {

    @Schema(description = "出库单ID列表", required = true)
    @NotEmpty(message = "出库单ID列表不能为空")
    private List<Long> ids;

}
