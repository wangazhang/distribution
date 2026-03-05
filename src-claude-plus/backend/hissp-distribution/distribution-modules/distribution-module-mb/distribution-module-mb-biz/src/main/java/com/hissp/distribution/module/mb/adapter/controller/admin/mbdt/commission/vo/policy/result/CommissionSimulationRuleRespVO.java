package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.result;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class CommissionSimulationRuleRespVO {

    @Schema(description = "规则ID")
    private Long ruleId;

    @Schema(description = "规则内部编码")
    private String ruleCode;

    @Schema(description = "规则展示名称")
    private String displayName;

    @Schema(description = "分佣流水类型")
    private Integer bizCode;

    @Schema(description = "出资流水类型")
    private Integer fundBizCode;

    @Schema(description = "目标层级")
    private String targetHierarchy;

    @Schema(description = "目标等级")
    private String targetLevel;

    @Schema(description = "金额来源")
    private String amountSource;

    @Schema(description = "金额模式")
    private String amountMode;

    @Schema(description = "金额取值")
    private BigDecimal amountValue;

    @Schema(description = "资金账户")
    private String fundAccount;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "命中用户ID")
    private Long userId;

    @Schema(description = "计算金额（分）")
    private Integer amount;

    @Schema(description = "物料奖励列表")
    private List<CommissionSimulationRuleMaterialRespVO> materials;
}
