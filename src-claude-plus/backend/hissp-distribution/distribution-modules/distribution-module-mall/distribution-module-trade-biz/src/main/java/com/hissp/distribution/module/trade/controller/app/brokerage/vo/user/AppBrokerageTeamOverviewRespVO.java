package com.hissp.distribution.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 团队总览 Response VO")
@Data
public class AppBrokerageTeamOverviewRespVO {

    @Schema(description = "社群统计")
    private AppBrokerageTeamStatsRespVO communityStats;

    @Schema(description = "直接下级统计")
    private AppBrokerageTeamStatsRespVO directStats;

    @Schema(description = "间接下级统计")
    private AppBrokerageTeamStatsRespVO indirectStats;

    @Schema(description = "等级筛选项")
    private List<AppBrokerageTeamLevelOptionRespVO> levelOptions;
}

