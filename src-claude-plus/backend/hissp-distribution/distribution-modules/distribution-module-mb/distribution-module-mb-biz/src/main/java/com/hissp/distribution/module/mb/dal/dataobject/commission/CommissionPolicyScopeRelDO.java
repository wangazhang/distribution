package com.hissp.distribution.module.mb.dal.dataobject.commission;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@TableName("mb_commission_policy_scope_rel")
@KeySequence("mb_commission_policy_scope_rel_seq")
@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionPolicyScopeRelDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 策略ID
     */
    private Long policyId;

    /**
     * 作用类型：1-商品 2-套包
     */
    private Integer scopeType;

    /**
     * 关联ID（商品SPU或套包ID）
     */
    private Long targetId;
}
