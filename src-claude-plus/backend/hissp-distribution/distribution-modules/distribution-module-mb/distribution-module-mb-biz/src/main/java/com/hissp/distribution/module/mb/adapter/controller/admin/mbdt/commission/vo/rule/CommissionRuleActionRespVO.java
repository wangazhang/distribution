package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import lombok.Data;

@Data
public class CommissionRuleActionRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "动作类型")
    private String actionType;

    @Schema(description = "金额模式")
    private String amountMode;

    @Schema(description = "金额值")
    private BigDecimal amountValue;

    @Schema(description = "封顶金额")
    private BigDecimal capValue;

    @Schema(description = "扩展信息 JSON")
    private Map<String, Object> payload;

    @Schema(description = "优先级")
    private Integer priority;
}
