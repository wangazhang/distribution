package com.hissp.distribution.module.trade.controller.app.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 用户 App - 渠道收货状态校验请求
 */
@Data
@Schema(description = "用户 App - 渠道收货状态校验 Request VO")
public class AppTradeOrderChannelConfirmReqVO {

    @Schema(description = "交易订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

}
