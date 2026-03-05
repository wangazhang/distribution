package com.hissp.distribution.module.mb.dal.dataobject.commission;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分佣规则定义 DO，对应 mb_commission_rule_definition。
 */
@TableName(value = "mb_commission_rule_definition", autoResultMap = true)
@KeySequence("mb_commission_rule_definition_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRuleDefinitionDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 所属策略
     */
    private Long policyId;

    /**
     * 规则内部编码（唯一）
     */
    private String ruleCode;

    /**
     * 展示名称
     */
    private String displayName;

    /**
     * 状态：DRAFT/ONLINE/OFFLINE
     */
    private String status;

    /**
     * 优先级，越小越先匹配
     */
    private Integer priority;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 命中范围摘要
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private CommissionRuleEffectScope effectScope;

    /**
     * 额外元数据
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> metadata;

    /**
     * 备注
     */
    private String remark;

    /**
     * 条件列表（非表字段）
     */
    @TableField(exist = false)
    @Builder.Default
    private List<CommissionRuleConditionDO> conditions = new ArrayList<>();

    /**
     * 动作列表（非表字段）
     */
    @TableField(exist = false)
    @Builder.Default
    private List<CommissionRuleActionDO> actions = new ArrayList<>();

    /**
     * 物料奖励（非表字段）
     */
    @TableField(exist = false)
    @Builder.Default
    private List<CommissionRuleMaterialDO> materials = new ArrayList<>();

    public List<CommissionRuleConditionDO> safeConditions() {
        return conditions == null ? new ArrayList<>() : conditions;
    }

    public List<CommissionRuleActionDO> safeActions() {
        return actions == null ? new ArrayList<>() : actions;
    }

    public List<CommissionRuleMaterialDO> safeMaterials() {
        return materials == null ? new ArrayList<>() : materials;
    }
}
