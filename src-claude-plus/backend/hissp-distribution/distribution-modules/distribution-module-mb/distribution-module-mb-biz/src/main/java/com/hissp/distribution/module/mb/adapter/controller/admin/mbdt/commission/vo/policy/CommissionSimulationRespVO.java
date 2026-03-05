package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy;

import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.result.CommissionSimulationRuleRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class CommissionSimulationRespVO {

    @Schema(description = "策略ID")
    private Long policyId;

    @Schema(description = "策略编码")
    private String policyCode;

    @Schema(description = "策略名称")
    private String policyName;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "订单总价（分）")
    private Integer orderTotalPrice;

    @Schema(description = "单价（分）")
    private Integer unitPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "总分佣金额（分）")
    private Integer totalCommission;

    @Schema(description = "规则命中明细")
    private List<CommissionSimulationRuleRespVO> rules;
}
