package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 佣金统计 Response VO")
@Data
public class BrokerageStatisticsRespVO {

    @Schema(description = "总收入", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private Integer totalIncome;

    @Schema(description = "总支出", requiredMode = Schema.RequiredMode.REQUIRED, example = "12345")
    private Integer totalExpense;

    @Schema(description = "总笔数", requiredMode = Schema.RequiredMode.REQUIRED, example = "156")
    private Integer totalCount;

    @Schema(description = "待结算笔数", requiredMode = Schema.RequiredMode.REQUIRED, example = "12")
    private Integer pendingCount;

    @Schema(description = "待结算金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "23456")
    private Integer pendingAmount;

    @Schema(description = "已结算笔数", requiredMode = Schema.RequiredMode.REQUIRED, example = "144")
    private Integer settledCount;

    @Schema(description = "已结算金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "100000")
    private Integer settledAmount;

    public BrokerageStatisticsRespVO() {
        this.totalIncome = 0;
        this.totalExpense = 0;
        this.totalCount = 0;
        this.pendingCount = 0;
        this.pendingAmount = 0;
        this.settledCount = 0;
        this.settledAmount = 0;
    }
}
