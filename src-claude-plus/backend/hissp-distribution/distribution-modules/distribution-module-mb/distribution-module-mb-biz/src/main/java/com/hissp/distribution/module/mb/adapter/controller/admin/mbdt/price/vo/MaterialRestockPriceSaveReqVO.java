package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 补货价格表：记录不同用户等级对应的商品补货价格新增/修改 Request VO")
@Data
public class MaterialRestockPriceSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10548")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "21675")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "用户等级ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "用户等级ID不能为空")
    private Long levelId;

    @Schema(description = "补货价格（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "16931")
    @NotNull(message = "补货价格（单位：分）不能为空")
    private Integer restockPrice;

    @Schema(description = "状态：0-禁用，1-启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
