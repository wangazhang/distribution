package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;

@Data
public class CommissionRuleConditionReqVO {

    @Schema(description = "条件类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String conditionType;

    @Schema(description = "运算符", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String operator;

    @Schema(description = "条件值 JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Map<String, Object> value;

    @Schema(description = "扩展信息 JSON")
    private Map<String, Object> extra;

    @Schema(description = "优先级，越小越先判断")
    private Integer priority;
}
