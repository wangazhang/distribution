package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class CommissionSimulationReqVO {

    @Schema(description = "策略ID", example = "1")
    private Long policyId;

    @Schema(description = "策略编码", example = "RESTOCK_SVIP_V1")
    private String policyCode;

    @Schema(description = "下单人等级", example = "101")
    private Integer selfLevel;

    @Schema(description = "直属上级等级")
    private Integer parentLevel;

    @Schema(description = "上上级等级")
    private Integer grandParentLevel;

    @Schema(description = "团队/分公司等级")
    private Integer teamLevel;

    @Schema(description = "商品ID")
    private Long productId;

    @Schema(description = "套包ID")
    private Long packageId;

    @Schema(description = "下单人ID")
    private Long agentUserId;

    @Schema(description = "直属上级ID")
    private Long parentUserId;

    @Schema(description = "上上级ID")
    private Long grandParentUserId;

    @Schema(description = "团队/分公司ID")
    private Long teamUserId;

    @Schema(description = "总部账户ID")
    private Long hqUserId;

    @Schema(description = "商品单价（分）")
    @Min(value = 0, message = "单价不能为负数")
    private Integer unitPrice;

    @Schema(description = "数量")
    @Min(value = 1, message = "数量必须大于等于 1")
    private Integer quantity;

    @Schema(description = "订单总价（分）")
    @Min(value = 0, message = "总价不能为负数")
    private Integer totalPrice;

    @AssertTrue(message = "策略ID或策略编码至少填写一个")
    public boolean isValidPolicyIdentifier() {
        return policyId != null || StringUtils.hasText(policyCode);
    }
}
