package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import lombok.Data;

@Data
public class CommissionRuleConditionRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "条件类型")
    private String conditionType;

    @Schema(description = "运算符")
    private String operator;

    @Schema(description = "条件值 JSON")
    private Map<String, Object> value;

    @Schema(description = "扩展信息 JSON")
    private Map<String, Object> extra;

    @Schema(description = "优先级")
    private Integer priority;
}
