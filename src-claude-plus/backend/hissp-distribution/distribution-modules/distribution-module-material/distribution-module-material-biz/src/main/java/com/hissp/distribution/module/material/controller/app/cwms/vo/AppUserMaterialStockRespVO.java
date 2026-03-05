package com.hissp.distribution.module.material.controller.app.cwms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 用户物料库存信息 Response VO")
@Data
public class AppUserMaterialStockRespVO {

    @Schema(description = "物料ID（等同于商品ID）", requiredMode = Schema.RequiredMode.REQUIRED, example = "19745")
    private Long materialId;

    @Schema(description = "物料名称", example = "胶原蛋白")
    private String materialName;
    
    @Schema(description = "物料图片URL", example = "https://example.com/image.jpg")
    private String materialImage;
    
    @Schema(description = "计量单位", example = "件")
    private String unit;

    @Schema(description = "物料库存", example = "10")
    private Integer stock;

    @Schema(description = "物料余额（与stock相同，用于统一字段名）", example = "10")
    private Integer balance;
} 

