package com.hissp.distribution.module.trade.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 交易订单后台退款请求 VO
 */
@Data
public class TradeOrderRefundReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long id;

    @Schema(description = "退款原因", example = "后台手动退款")
    private String reason;

    @Schema(description = "退款密码")
    private String password;
}
