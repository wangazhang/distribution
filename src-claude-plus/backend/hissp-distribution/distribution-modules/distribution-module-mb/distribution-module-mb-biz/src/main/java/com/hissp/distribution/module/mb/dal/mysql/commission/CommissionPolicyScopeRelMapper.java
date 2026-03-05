package com.hissp.distribution.module.mb.dal.mysql.commission;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.util.CollectionUtils;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyScopeRelDO;

@Mapper
public interface CommissionPolicyScopeRelMapper extends BaseMapperX<CommissionPolicyScopeRelDO> {

    default void deleteByPolicyId(Long policyId) {
        delete(new LambdaQueryWrapperX<CommissionPolicyScopeRelDO>()
            .eq(CommissionPolicyScopeRelDO::getPolicyId, policyId));
    }

    default void insertBatch(Long policyId, Collection<Long> targetIds, Integer scopeType) {
        if (policyId == null || scopeType == null || CollectionUtils.isEmpty(targetIds)) {
            return;
        }
        targetIds.stream()
            .filter(id -> id != null)
            .distinct()
            .map(id -> {
                CommissionPolicyScopeRelDO rel = new CommissionPolicyScopeRelDO();
                rel.setPolicyId(policyId);
                rel.setScopeType(scopeType);
                rel.setTargetId(id);
                return rel;
            })
            .forEach(this::insert);
    }

    default List<Long> selectTargetIdsByPolicyId(Long policyId, Integer scopeType) {
        if (policyId == null || scopeType == null) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CommissionPolicyScopeRelDO>()
            .eq(CommissionPolicyScopeRelDO::getPolicyId, policyId)
            .eq(CommissionPolicyScopeRelDO::getScopeType, scopeType))
            .stream()
            .map(CommissionPolicyScopeRelDO::getTargetId)
            .filter(id -> id != null)
            .collect(Collectors.toList());
    }

    default Map<Long, List<Long>> selectTargetIdsByPolicyIds(Collection<Long> policyIds, Integer scopeType) {
        if (CollectionUtils.isEmpty(policyIds) || scopeType == null) {
            return Collections.emptyMap();
        }
        return selectList(new LambdaQueryWrapperX<CommissionPolicyScopeRelDO>()
            .in(CommissionPolicyScopeRelDO::getPolicyId, policyIds)
            .eq(CommissionPolicyScopeRelDO::getScopeType, scopeType))
            .stream()
            .filter(rel -> rel.getPolicyId() != null && rel.getTargetId() != null)
            .collect(Collectors.groupingBy(CommissionPolicyScopeRelDO::getPolicyId,
                Collectors.mapping(CommissionPolicyScopeRelDO::getTargetId, Collectors.toList())));
    }
}
