package com.hissp.distribution.module.material.controller.app.cwms.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "APP - 物料变更记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class AppMaterialRecordRespVO {

    @Schema(description = "记录ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("记录ID")
    private Long id;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @ExcelProperty("用户ID")
    private Long userId;

    @Schema(description = "物料ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("物料ID")
    private Long materialId;
    
    @Schema(description = "物料名称", example = "ECM胶原蛋白")
    @ExcelProperty("物料名称")
    private String materialName;
    
    @Schema(description = "物料图片", example = "https://example.com/image.jpg")
    @ExcelProperty("物料图片")
    private String materialImage;
    
    @Schema(description = "计量单位", example = "件")
    @ExcelProperty("计量单位")
    private String unit;

    @Schema(description = "操作类型：0-获得，1-使用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("操作类型：0-获得，1-使用")
    private Integer actionType;
    
    @Schema(description = "操作类型文本", example = "获得")
    @ExcelProperty("操作类型文本")
    private String actionTypeText;

    @Schema(description = "变更数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("变更数量")
    private Integer amount;

    @Schema(description = "来源类型：0-订单获得，1-活动赠送，2-手动添加，3-系统调整", example = "2")
    @ExcelProperty("来源类型：0-订单获得，1-活动赠送，2-手动添加，3-系统调整")
    private Integer sourceType;
    
    @Schema(description = "来源类型文本", example = "手动添加")
    @ExcelProperty("来源类型文本")
    private String sourceTypeText;

    @Schema(description = "来源ID")
    @ExcelProperty("来源ID")
    private String sourceId;

    @Schema(description = "变更前数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @ExcelProperty("变更前数量")
    private Integer beforeAmount;

    @Schema(description = "变更后数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "30")
    @ExcelProperty("变更后数量")
    private Integer afterAmount;

    @Schema(description = "变更日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("变更日期")
    private LocalDate actionDate;

    @Schema(description = "变更描述", example = "活动赠送")
    @ExcelProperty("变更描述")
    private String sourceDesc;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
    
    public String getActionTypeText() {
        if (this.actionType == null) {
            return null;
        }
        return this.actionType == 0 ? "获得" : "使用";
    }
}

