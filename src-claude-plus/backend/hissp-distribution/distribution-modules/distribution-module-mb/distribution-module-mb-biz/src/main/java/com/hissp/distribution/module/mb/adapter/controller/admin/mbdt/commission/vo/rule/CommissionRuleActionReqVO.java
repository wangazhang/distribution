package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

@Data
public class CommissionRuleActionReqVO {

    @Schema(description = "动作类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String actionType;

    @Schema(description = "金额模式 FIXED/PERCENTAGE")
    private String amountMode;

    @Schema(description = "金额值")
    private BigDecimal amountValue;

    @Schema(description = "封顶金额")
    private BigDecimal capValue;

    @Schema(description = "扩展信息 JSON")
    private Map<String, Object> payload;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer priority;
}
