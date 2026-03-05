package com.hissp.distribution.module.material.controller.app.cwms.vo;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 用户物料余额事实表，记录用户每种物料的余额信息 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppUserMaterialBalanceRespVO {

    @Schema(description = "余额记录ID（分布式ID生成）", requiredMode = Schema.RequiredMode.REQUIRED, example = "31790")
    @ExcelProperty("余额记录ID（分布式ID生成）")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "6122")
    @ExcelProperty("用户ID")
    private Integer userId;

    @Schema(description = "物料ID（关联mb_material表）", example = "19745")
    @ExcelProperty("物料ID（关联mb_material表）")
    private Long materialId;

    @Schema(description = "用于分享的商品spuId）", example = "19745")
    private Long shareId;


    @Schema(description = "补货使用的商品spuId", example = "27305")
    private Long refillId;

    @Schema(description = "物料名称", example = "胶原蛋白")
    @ExcelProperty("物料名称")
    private String materialName;
    
    @Schema(description = "物料图片URL", example = "https://example.com/image.jpg")
    @ExcelProperty("物料图片URL")
    private String materialImage;
    
    @Schema(description = "计量单位", example = "件")
    @ExcelProperty("计量单位")
    private String unit;

    @Schema(description = "物料类型：1-半成品 2-成品")
    private Integer materialType;

    @Schema(description = "是否支持转化")
    private Boolean supportConvert;

    @Schema(description = "转化目标SPU ID")
    private Long convertTargetSpuId;

    @Schema(description = "转化目标名称")
    private String convertTargetName;

    @Schema(description = "转化单价（单位：分）")
    private Integer convertPrice;

    @Schema(description = "是否支持补货")
    private Boolean supportRestock;

    @Schema(description = "物料余额，单位为分")
    @ExcelProperty("物料余额，单位为分")
    private Integer balance;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

}
