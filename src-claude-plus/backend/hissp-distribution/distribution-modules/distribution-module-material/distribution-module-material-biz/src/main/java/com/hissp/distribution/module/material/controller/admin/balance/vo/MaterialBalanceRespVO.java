package com.hissp.distribution.module.material.controller.admin.balance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料余额 Response VO")
@Data
public class MaterialBalanceRespVO {

    @Schema(description = "记录ID", required = true, example = "1")
    private Long id;

    @Schema(description = "用户ID", required = true, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", required = true, example = "张三")
    private String nickname;

    @Schema(description = "物料ID", required = true, example = "2048")
    private Long materialId;

    @Schema(description = "物料名称", required = true, example = "胶原蛋白")
    private String materialName;

    @Schema(description = "可用余额", required = true, example = "100")
    private Integer availableBalance;

    @Schema(description = "冻结余额", required = true, example = "0")
    private Integer frozenBalance;

    @Schema(description = "最后更新时间", required = true)
    private LocalDateTime updateTime;

}

