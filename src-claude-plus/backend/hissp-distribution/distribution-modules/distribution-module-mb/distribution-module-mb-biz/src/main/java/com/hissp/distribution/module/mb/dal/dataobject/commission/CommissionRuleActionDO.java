package com.hissp.distribution.module.mb.dal.dataobject.commission;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import java.math.BigDecimal;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分佣规则动作 DO。
 */
@TableName(value = "mb_commission_rule_action", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRuleActionDO extends BaseDO {

    @TableId
    private Long id;

    private Long ruleId;

    /**
     * 动作类型，如 COMMISSION、EXTRA_BONUS 等
     */
    private String actionType;

    /**
     * 金额模式 FIXED/PERCENTAGE
     */
    private String amountMode;

    /**
     * 金额值
     */
    private BigDecimal amountValue;

    /**
     * 封顶金额
     */
    private BigDecimal capValue;

    /**
     * 额外参数
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> payloadJson;

    private Integer priority;
}
