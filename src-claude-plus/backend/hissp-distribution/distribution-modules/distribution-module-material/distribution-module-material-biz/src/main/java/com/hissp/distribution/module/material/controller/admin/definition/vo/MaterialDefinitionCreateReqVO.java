package com.hissp.distribution.module.material.controller.admin.definition.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 物料定义创建 Request VO")
@Data
public class MaterialDefinitionCreateReqVO {

    @Schema(description = "物料名称", required = true, example = "胶原蛋白")
    @NotBlank(message = "物料名称不能为空")
    private String name;

    @Schema(description = "物料编码", example = "MAT001")
    private String code;

    @Schema(description = "关联SPU ID", example = "12345")
    private Long spuId;

    @Schema(description = "物料图片", example = "https://example.com/image.jpg")
    private String image;

    @Schema(description = "物料描述", example = "高品质胶原蛋白产品")
    private String description;

    @Schema(description = "基础单位", required = true, example = "瓶")
    @NotBlank(message = "基础单位不能为空")
    private String baseUnit;

    @Schema(description = "物料类型", example = "1")
    private Integer type;

    @Schema(description = "SPU关联模式", example = "1")
    private Integer linkMode;

    @Schema(description = "是否自动同步SPU信息", example = "true")
    private Boolean autoSync;

    @Schema(description = "SPU信息快照")
    private String spuSnapshot;

    @Schema(description = "转化状态", example = "0")
    private Integer convertStatus;

    @Schema(description = "转化后的SPU ID")
    private Long convertedSpuId;

    @Schema(description = "转化单价（单位：分）", example = "19900")
    private Integer convertPrice;

    @Schema(description = "是否支持出库", example = "true")
    private Boolean supportOutbound;

    @Schema(description = "是否支持转化", example = "true")
    private Boolean supportConvert;

    @Schema(description = "状态", required = true, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "扩展属性", example = "{\"weight\": \"100g\"}")
    private String attrs;

}
