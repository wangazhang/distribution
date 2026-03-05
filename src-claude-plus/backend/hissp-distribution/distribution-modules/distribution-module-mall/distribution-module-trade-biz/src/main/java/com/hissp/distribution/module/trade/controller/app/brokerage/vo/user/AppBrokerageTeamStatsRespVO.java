package com.hissp.distribution.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 团队统计数据 Response VO")
@Data
public class AppBrokerageTeamStatsRespVO {

    @Schema(description = "总人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Integer total;

    @Schema(description = "今日新增", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer todayNew;

    @Schema(description = "本月新增", requiredMode = Schema.RequiredMode.REQUIRED, example = "50")
    private Integer monthNew;
} 