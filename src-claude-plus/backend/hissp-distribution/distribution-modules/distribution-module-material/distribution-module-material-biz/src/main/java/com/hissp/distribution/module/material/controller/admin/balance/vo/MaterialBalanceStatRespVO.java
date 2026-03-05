package com.hissp.distribution.module.material.controller.admin.balance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 物料余额统计 Response VO")
@Data
public class MaterialBalanceStatRespVO {

    @Schema(description = "总用户数", required = true, example = "1000")
    private Long totalUsers;

    @Schema(description = "有余额用户数", required = true, example = "500")
    private Long usersWithBalance;

    @Schema(description = "总物料种类数", required = true, example = "50")
    private Long totalMaterials;

    @Schema(description = "总余额数量", required = true, example = "100000")
    private Long totalBalance;

    @Schema(description = "平均余额", required = true, example = "200.0")
    private Double averageBalance;

}