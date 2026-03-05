package com.hissp.distribution.module.mb.dal.mysql.commission;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleConditionDO;
import java.util.Collections;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommissionRuleConditionMapper extends BaseMapperX<CommissionRuleConditionDO> {

    default List<CommissionRuleConditionDO> selectByRuleIds(List<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CommissionRuleConditionDO>()
            .in(CommissionRuleConditionDO::getRuleId, ruleIds)
            .orderByAsc(CommissionRuleConditionDO::getRuleId, CommissionRuleConditionDO::getPriority, CommissionRuleConditionDO::getId));
    }

    default void deleteByRuleId(Long ruleId) {
        delete(new LambdaQueryWrapperX<CommissionRuleConditionDO>()
            .eq(CommissionRuleConditionDO::getRuleId, ruleId));
    }
}
