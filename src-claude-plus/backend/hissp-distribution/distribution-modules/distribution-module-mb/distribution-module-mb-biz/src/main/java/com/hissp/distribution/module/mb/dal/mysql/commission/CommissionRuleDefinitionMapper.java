package com.hissp.distribution.module.mb.dal.mysql.commission;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommissionRuleDefinitionMapper extends BaseMapperX<CommissionRuleDefinitionDO> {

    default List<CommissionRuleDefinitionDO> selectOnlineByPolicyId(Long policyId) {
        return selectList(new LambdaQueryWrapperX<CommissionRuleDefinitionDO>()
            .eq(CommissionRuleDefinitionDO::getPolicyId, policyId)
            .eq(CommissionRuleDefinitionDO::getStatus, "ONLINE")
            .orderByAsc(CommissionRuleDefinitionDO::getPriority)
            .orderByAsc(CommissionRuleDefinitionDO::getId));
    }

    default List<CommissionRuleDefinitionDO> selectAllByPolicyId(Long policyId) {
        return selectList(new LambdaQueryWrapperX<CommissionRuleDefinitionDO>()
            .eq(CommissionRuleDefinitionDO::getPolicyId, policyId)
            .orderByAsc(CommissionRuleDefinitionDO::getPriority)
            .orderByAsc(CommissionRuleDefinitionDO::getId));
    }
}
