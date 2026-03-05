package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CommissionRuleRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "策略ID")
    private Long policyId;

    @Schema(description = "规则内部编码")
    private String ruleCode;

    @Schema(description = "展示名称")
    private String displayName;

    @Schema(description = "状态 DRAFT/ONLINE/OFFLINE")
    private String status;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "命中范围摘要")
    private CommissionRuleEffectScopeVO effectScope;

    @Schema(description = "条件列表")
    private List<CommissionRuleConditionRespVO> conditions;

    @Schema(description = "奖励动作列表")
    private List<CommissionRuleActionRespVO> actions;

    @Schema(description = "物料奖励列表")
    private List<CommissionRuleMaterialRespVO> materials;
}
