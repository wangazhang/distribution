package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

@Data
public class CommissionRuleEffectScopeVO {

    @Schema(description = "受益层级 SELF/PARENT/GRANDPARENT 等")
    private String targetHierarchy;

    @Schema(description = "受益等级，ALL 表示不限")
    private String targetLevel;

    @Schema(description = "金额来源")
    private String amountSource;

    @Schema(description = "资金承担方")
    private String fundAccount;

    @Schema(description = "邀请顺序摘要")
    private InviteOrderScopeVO inviteOrder;

    @Data
    public static class InviteOrderScopeVO {

        @Schema(description = "运算符 EQ/BETWEEN/ALWAYS")
        private String operator;

        @Schema(description = "最小邀请序号")
        private Integer minIndex;

        @Schema(description = "最大邀请序号")
        private Integer maxIndex;

        @Schema(description = "指定序号集合")
        private List<Integer> indexes;
    }
}
