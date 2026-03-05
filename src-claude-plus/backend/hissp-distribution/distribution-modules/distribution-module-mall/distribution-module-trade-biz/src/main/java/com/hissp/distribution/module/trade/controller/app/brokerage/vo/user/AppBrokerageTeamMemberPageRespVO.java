package com.hissp.distribution.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "用户 App - 团队成员分页 Response VO")
@Data
public class AppBrokerageTeamMemberPageRespVO {

    @Schema(description = "团队统计数据", requiredMode = Schema.RequiredMode.REQUIRED)
    private AppBrokerageTeamStatsRespVO stats;

    @Schema(description = "团队成员列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<AppBrokerageTeamMemberRespVO> list;
} 