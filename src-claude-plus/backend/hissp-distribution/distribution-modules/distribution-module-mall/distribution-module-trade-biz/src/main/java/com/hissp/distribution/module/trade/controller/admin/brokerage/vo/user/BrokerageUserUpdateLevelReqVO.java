package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 分销用户修改等级 Request VO")
@Data
public class BrokerageUserUpdateLevelReqVO {

    @Schema(description = "用户编号", required = true, example = "1024")
    @NotNull(message = "用户编号不能为空")
    private Long id;

    @Schema(description = "等级编号", required = true, example = "1")
    @NotNull(message = "等级编号不能为空")
    private Long levelId;

} 