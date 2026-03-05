package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 佣金提现 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class BrokerageWithdrawRespVO extends BrokerageWithdrawBaseVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7161")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "用户昵称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String userNickname;

    @Schema(description = "是否支持渠道打款", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean channelPayEnabled;

    @Schema(description = "渠道打款是否已发起（隐藏按钮，仅展示进度）")
    private Boolean channelPayLocked;

}
