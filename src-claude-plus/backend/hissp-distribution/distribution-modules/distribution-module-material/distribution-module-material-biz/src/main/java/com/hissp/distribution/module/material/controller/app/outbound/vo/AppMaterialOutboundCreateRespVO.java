package com.hissp.distribution.module.material.controller.app.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP - 物料出库创建 Response VO")
@Data
public class AppMaterialOutboundCreateRespVO {

    @Schema(description = "出库单ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "出库单号", required = true, example = "OUT20240918001")
    private String outboundNo;

    @Schema(description = "状态：1待审核 2审核通过 3审核拒绝 4已发货 5已签收 6已取消", 
            required = true, example = "1")
    private Integer status;

    @Schema(description = "状态名称", required = true, example = "待审核")
    private String statusName;

}