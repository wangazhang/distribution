package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BrokerageWithdrawChannelRetryReqVO {

    @Schema(description = "提现编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "提现编号不能为空")
    private Long id;

    @Schema(description = "打款口令")
    private String password;
}
