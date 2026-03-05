package com.hissp.distribution.module.mb.adapter.controller.app.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * App - MB订单渠道确认收货校验请求
 */
@Data
@Schema(description = "App - MB订单渠道确认收货校验 Request VO")
public class AppMbOrderChannelConfirmReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "订单编号不能为空")
    private Long orderId;
}
