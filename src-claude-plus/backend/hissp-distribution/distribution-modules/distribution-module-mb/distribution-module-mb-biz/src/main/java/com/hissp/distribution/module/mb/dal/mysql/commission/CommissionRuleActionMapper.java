package com.hissp.distribution.module.mb.dal.mysql.commission;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleActionDO;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommissionRuleActionMapper extends BaseMapperX<CommissionRuleActionDO> {

    default List<CommissionRuleActionDO> selectByRuleIds(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CommissionRuleActionDO>()
            .in(CommissionRuleActionDO::getRuleId, ruleIds)
            .orderByAsc(CommissionRuleActionDO::getRuleId, CommissionRuleActionDO::getPriority, CommissionRuleActionDO::getId));
    }

    default void deleteByRuleId(Long ruleId) {
        delete(new LambdaQueryWrapperX<CommissionRuleActionDO>()
            .eq(CommissionRuleActionDO::getRuleId, ruleId));
    }
}
