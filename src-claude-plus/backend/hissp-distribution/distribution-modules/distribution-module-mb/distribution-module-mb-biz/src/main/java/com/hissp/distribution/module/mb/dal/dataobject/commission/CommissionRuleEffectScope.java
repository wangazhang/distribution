package com.hissp.distribution.module.mb.dal.dataobject.commission;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

/**
 * 规则命中范围的摘要信息，序列化为 JSON 存储在 effect_scope 字段。
 */
@Data
public class CommissionRuleEffectScope implements Serializable {

    private static final long serialVersionUID = 3582471893381646521L;

    /**
     * 命中的受益层级
     */
    private String targetHierarchy;

    /**
     * 命中的目标等级（ALL 表示不限）
     */
    private String targetLevel;

    /**
     * 金额来源（ORDER_AMOUNT/UNIT_PRICE/QUANTITY等）
     */
    private String amountSource;

    /**
     * 资金承担层级
     */
    private String fundAccount;

    /**
     * 邀请顺序摘要
     */
    private InviteOrderScope inviteOrder;

    @Data
    public static class InviteOrderScope implements Serializable {

        private static final long serialVersionUID = -4879382409516128343L;

        /**
         * 运算符：EQ/IN/BETWEEN/ALWAYS
         */
        private String operator;

        /**
         * 区间下限
         */
        private Integer minIndex;

        /**
         * 区间上限
         */
        private Integer maxIndex;

        /**
         * 零散取值
         */
        private List<Integer> indexes;
    }
}
