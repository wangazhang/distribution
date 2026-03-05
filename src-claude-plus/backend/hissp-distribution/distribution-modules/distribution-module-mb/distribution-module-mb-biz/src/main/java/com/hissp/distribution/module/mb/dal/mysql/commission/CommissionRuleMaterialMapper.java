package com.hissp.distribution.module.mb.dal.mysql.commission;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleMaterialDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CommissionRuleMaterialMapper extends BaseMapperX<CommissionRuleMaterialDO> {

    default List<CommissionRuleMaterialDO> selectByRuleIds(Collection<Long> ruleIds) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<CommissionRuleMaterialDO>()
            .in(CommissionRuleMaterialDO::getRuleId, ruleIds));
    }

    default List<CommissionRuleMaterialDO> selectByRuleId(Long ruleId) {
        if (ruleId == null) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<CommissionRuleMaterialDO>()
            .eq(CommissionRuleMaterialDO::getRuleId, ruleId));
    }

    default void deleteByRuleId(Long ruleId) {
        if (ruleId == null) {
            return;
        }
        delete(new LambdaQueryWrapperX<CommissionRuleMaterialDO>().eq(CommissionRuleMaterialDO::getRuleId, ruleId));
    }
}
