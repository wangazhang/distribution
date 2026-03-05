package com.hissp.distribution.module.mb.dal.dataobject.commission;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分佣规则条件 DO。
 */
@TableName(value = "mb_commission_rule_condition", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRuleConditionDO extends BaseDO {

    @TableId
    private Long id;

    private Long ruleId;

    private String conditionType;

    private String operator;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> valueJson;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extraJson;

    private Integer priority;
}
