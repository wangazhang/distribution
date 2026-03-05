package com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - mb订单新增/修改 Request VO")
@Data
public class MbOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15504")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7561")
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "restock")
    @NotNull(message = "业务类型不能为空")
    private String bizType;

    @Schema(description = "代理用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30083")
    @NotNull(message = "代理用户ID不能为空")
    private Long agentUserId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数量不能为空")
    private Integer quantity;

    @Schema(description = "单价（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "17617")
    @NotNull(message = "单价不能为空")
    private Integer unitPrice;

    @Schema(description = "总价（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "6935")
    @NotNull(message = "总价不能为空")
    private Integer totalPrice;

    @Schema(description = "支付单ID", example = "14834")
    private String paymentId;

}