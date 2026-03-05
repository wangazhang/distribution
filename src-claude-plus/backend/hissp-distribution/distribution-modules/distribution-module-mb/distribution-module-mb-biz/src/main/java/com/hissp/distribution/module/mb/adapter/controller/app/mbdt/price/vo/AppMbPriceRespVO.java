package com.hissp.distribution.module.mb.adapter.controller.app.mbdt.price.vo;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - 补货价格表响应 VO")
@Data
@ExcelIgnoreUnannotated
public class AppMbPriceRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long productId;
    
    @Schema(description = "商品名称", example = "示例物料")
    private String productName;
    
    @Schema(description = "商品图片", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "用户等级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long levelId;
    
    @Schema(description = "用户等级名称", example = "SVIP")
    private String userLevelName;

    @Schema(description = "补货价格（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "30000")
    private Integer restockPrice;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

} 
