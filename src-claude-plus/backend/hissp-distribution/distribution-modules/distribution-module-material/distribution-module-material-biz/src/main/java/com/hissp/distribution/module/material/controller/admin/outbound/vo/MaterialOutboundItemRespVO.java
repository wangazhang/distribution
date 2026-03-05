package com.hissp.distribution.module.material.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料出库明细 Response VO")
@Data
public class MaterialOutboundItemRespVO {

    @Schema(description = "明细ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "出库单ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long outboundId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long materialId;

    @Schema(description = "物料编码", example = "MAT-001")
    private String materialCode;

    @Schema(description = "物料名称", example = "螺丝")
    private String materialName;

    @Schema(description = "物料图片", example = "https://example.com/image.jpg")
    private String materialImage;

    @Schema(description = "出库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer quantity;

    @Schema(description = "单位", example = "个")
    private String unit;

    @Schema(description = "基础单位", example = "个")
    private String baseUnit;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
