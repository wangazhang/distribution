package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CommissionRuleSaveReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "策略ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Long policyId;

    @Schema(description = "规则内部编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String ruleCode;

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String displayName;

    @Schema(description = "状态 DRAFT/ONLINE/OFFLINE")
    private String status;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer priority;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "命中条件列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    private List<CommissionRuleConditionReqVO> conditions;

    @Schema(description = "奖励动作列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Valid
    private List<CommissionRuleActionReqVO> actions;

    @Schema(description = "物料奖励配置")
    @Valid
    private List<CommissionRuleMaterialReqVO> materials;
}
