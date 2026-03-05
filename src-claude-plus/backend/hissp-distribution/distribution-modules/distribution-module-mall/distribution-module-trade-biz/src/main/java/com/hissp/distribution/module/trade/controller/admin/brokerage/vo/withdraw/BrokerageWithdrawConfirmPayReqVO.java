package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 财务确认打款 Request VO")
@Data
public class BrokerageWithdrawConfirmPayReqVO {

    @Schema(description = "提现编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10086")
    @NotNull(message = "提现编号不能为空")
    private Long id;

    @Schema(description = "备注")
    private String remark;

}
