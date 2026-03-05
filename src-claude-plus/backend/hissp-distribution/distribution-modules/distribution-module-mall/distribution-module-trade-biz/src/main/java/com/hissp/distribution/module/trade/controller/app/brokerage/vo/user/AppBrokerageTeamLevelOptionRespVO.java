package com.hissp.distribution.module.trade.controller.app.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 团队成员等级筛选项 Response VO")
@Data
public class AppBrokerageTeamLevelOptionRespVO {

    @Schema(description = "等级编号", example = "1")
    private Long id;

    @Schema(description = "等级名称", example = "VIP会员")
    private String name;
}

