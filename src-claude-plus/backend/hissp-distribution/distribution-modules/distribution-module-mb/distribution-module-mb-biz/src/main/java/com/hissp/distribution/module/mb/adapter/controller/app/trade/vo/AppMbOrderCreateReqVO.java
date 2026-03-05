package com.hissp.distribution.module.mb.adapter.controller.app.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "App - mb订单创建 Request VO")
@Data
public class AppMbOrderCreateReqVO {

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "restock")
    @NotNull(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "代理用户ID，由系统自动设置", hidden = true)
    private Long agentUserId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;

    @Schema(description = "单价（单位：分），从系统获取，无需填写", hidden = true)
    private Integer unitPrice;

    @Schema(description = "总价（单位：分），从系统计算，无需填写", hidden = true)
    private Integer totalPrice;

    @Schema(description = "是否验证库存（针对物料转化），前端不需传递", hidden = true)
    private Boolean validateStock;
}