package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - 补货价格表：记录不同用户等级对应的商品补货价格 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MaterialRestockPriceRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10548")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21675")
    @ExcelProperty("商品ID")
    private Long productId;

    @Schema(description = "商品名称", example = "示例物料")
    @ExcelProperty("商品名称")
    private String productName;

    @Schema(description = "用户等级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @ExcelProperty("用户等级ID")
    private Long levelId;

    @Schema(description = "用户等级名称", example = "SVIP美学师")
    @ExcelProperty("用户等级名称")
    private String levelName;

    @Schema(description = "补货价格（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "16931")
    @ExcelProperty("补货价格（单位：分）")
    private Integer restockPrice;

    @Schema(description = "状态：0-禁用，1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

}
