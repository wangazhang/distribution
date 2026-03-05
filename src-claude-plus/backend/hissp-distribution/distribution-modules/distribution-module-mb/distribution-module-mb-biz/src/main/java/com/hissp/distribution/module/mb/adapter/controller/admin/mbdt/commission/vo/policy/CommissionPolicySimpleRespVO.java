package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CommissionPolicySimpleRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "策略编码")
    private String policyCode;

    @Schema(description = "展示名称")
    private String displayName;

    @Schema(description = "业务类型")
    private String bizType;

}
