package com.hissp.distribution.module.mb.domain.service.mbdt.commission.impl;

import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicyPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicySaveReqVO;
import com.hissp.distribution.module.mb.convert.commission.CommissionPolicyConvert;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionPolicyMapper;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionPolicyScopeRelMapper;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionPolicyService;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionPolicyMatchContext;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import static com.hissp.distribution.module.mb.enums.ErrorCodeConstants.COMMISSION_POLICY_NOT_EXISTS;

@Service
@Validated
public class CommissionPolicyServiceImpl implements CommissionPolicyService {

    @Resource
    private CommissionPolicyMapper commissionPolicyMapper;

    @Resource
    private CommissionPolicyConvert commissionPolicyConvert;

    @Resource
    private CommissionPolicyScopeRelMapper commissionPolicyScopeRelMapper;

    private static final int SCOPE_TYPE_PRODUCT = 1;
    private static final int SCOPE_TYPE_PACKAGE = 2;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPolicy(CommissionPolicySaveReqVO reqVO) {
        CommissionPolicyDO policyDO = commissionPolicyConvert.convert(reqVO);
        policyDO.setStatus("DRAFT");
        commissionPolicyMapper.insert(policyDO);
        replaceRelations(policyDO.getId(), reqVO.getApplicableProductIds(), reqVO.getApplicablePackageIds());
        return policyDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePolicy(CommissionPolicySaveReqVO reqVO) {
        CommissionPolicyDO dbPolicy = validateExists(reqVO.getId());
        CommissionPolicyDO policyDO = commissionPolicyConvert.convert(reqVO);
        policyDO.setStatus(dbPolicy.getStatus());
        commissionPolicyMapper.updateById(policyDO);
        replaceRelations(policyDO.getId(), reqVO.getApplicableProductIds(), reqVO.getApplicablePackageIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPolicy(Long id) {
        CommissionPolicyDO policy = validateExists(id);
        policy.setStatus("ONLINE");
        commissionPolicyMapper.updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlinePolicy(Long id) {
        CommissionPolicyDO policy = validateExists(id);
        policy.setStatus("OFFLINE");
        commissionPolicyMapper.updateById(policy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long clonePolicy(Long id) {
        CommissionPolicyDO dbPolicy = validateExists(id);
        CommissionPolicyDO cloned = BeanUtils.toBean(dbPolicy, CommissionPolicyDO.class);
        cloned.setId(null);
        cloned.setStatus("DRAFT");
        if (cloned.getVersionNo() != null) {
            cloned.setVersionNo(cloned.getVersionNo() + 1);
        }
        commissionPolicyMapper.insert(cloned);
        cloneRelations(dbPolicy.getId(), cloned.getId());
        return cloned.getId();
    }

    @Override
    public CommissionPolicyDO getPolicy(Long id) {
        CommissionPolicyDO policy = validateExists(id);
        fillPolicyRelations(Collections.singletonList(policy));
        return policy;
    }

    @Override
    public CommissionPolicyDO getPolicyByCode(String policyCode) {
        CommissionPolicyDO policy = commissionPolicyMapper.selectByCode(policyCode);
        if (policy != null) {
            fillPolicyRelations(Collections.singletonList(policy));
        }
        return policy;
    }

    @Override
    public CommissionPolicyDO matchOnlinePolicy(CommissionPolicyMatchContext context) {
        if (context == null || !StringUtils.hasText(context.getBizType())) {
            return null;
        }
        List<CommissionPolicyDO> candidates = commissionPolicyMapper.selectOnlineListByBizType(context.getBizType());
        if (CollectionUtils.isEmpty(candidates)) {
            return null;
        }
        fillPolicyRelations(candidates);
        return candidates.stream()
            .sorted(Comparator.comparing(CommissionPolicyDO::getVersionNo, Comparator.nullsLast(Comparator.reverseOrder())))
            .filter(policy -> matchLevel(policy, context.getTriggerLevel()))
            .filter(policy -> matchProduct(policy, context.getProductId()))
            .filter(policy -> matchPackage(policy, context.getPackageId()))
            .findFirst()
            .orElse(null);
    }

    @Override
    public List<CommissionPolicyDO> getOnlinePolicyList() {
        List<CommissionPolicyDO> list = commissionPolicyMapper.selectOnlineList();
        fillPolicyRelations(list);
        return list;
    }

    @Override
    public PageResult<CommissionPolicyDO> getPolicyPage(CommissionPolicyPageReqVO reqVO) {
        PageResult<CommissionPolicyDO> pageResult = commissionPolicyMapper.selectPage(reqVO);
        fillPolicyRelations(pageResult.getList());
        return pageResult;
    }

    private CommissionPolicyDO validateExists(Long id) {
        CommissionPolicyDO policy = commissionPolicyMapper.selectById(id);
        if (policy == null) {
            throw ServiceExceptionUtil.exception(COMMISSION_POLICY_NOT_EXISTS);
        }
        return policy;
    }

    private void replaceRelations(Long policyId, List<Long> productIds, List<Long> packageIds) {
        commissionPolicyScopeRelMapper.deleteByPolicyId(policyId);
        commissionPolicyScopeRelMapper.insertBatch(policyId, productIds, SCOPE_TYPE_PRODUCT);
        commissionPolicyScopeRelMapper.insertBatch(policyId, packageIds, SCOPE_TYPE_PACKAGE);
    }

    private void cloneRelations(Long sourcePolicyId, Long targetPolicyId) {
        List<Long> productIds = commissionPolicyScopeRelMapper.selectTargetIdsByPolicyId(sourcePolicyId, SCOPE_TYPE_PRODUCT);
        List<Long> packageIds = commissionPolicyScopeRelMapper.selectTargetIdsByPolicyId(sourcePolicyId, SCOPE_TYPE_PACKAGE);
        replaceRelations(targetPolicyId, productIds, packageIds);
    }

    private void fillPolicyRelations(List<CommissionPolicyDO> policies) {
        if (CollectionUtils.isEmpty(policies)) {
            return;
        }
        List<Long> policyIds = policies.stream()
            .map(CommissionPolicyDO::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(policyIds)) {
            return;
        }
        Map<Long, List<Long>> productMap =
            commissionPolicyScopeRelMapper.selectTargetIdsByPolicyIds(policyIds, SCOPE_TYPE_PRODUCT);
        Map<Long, List<Long>> packageMap =
            commissionPolicyScopeRelMapper.selectTargetIdsByPolicyIds(policyIds, SCOPE_TYPE_PACKAGE);
        for (CommissionPolicyDO policy : policies) {
            Long policyId = policy.getId();
            policy.setApplicableProductIds(new ArrayList<>(productMap.getOrDefault(policyId, Collections.emptyList())));
            policy.setApplicablePackageIds(new ArrayList<>(packageMap.getOrDefault(policyId, Collections.emptyList())));
        }
    }

    private boolean matchLevel(CommissionPolicyDO policy, Integer triggerLevel) {
        if (!StringUtils.hasText(policy.getApplicableLevel())) {
            return true;
        }
        String levelStr = policy.getApplicableLevel();
        if ("ALL".equalsIgnoreCase(levelStr.trim())) {
            return true;
        }
        if (triggerLevel == null) {
            return false;
        }
        String[] levels = policy.getApplicableLevel().split(",");
        for (String level : levels) {
            if ("ALL".equalsIgnoreCase(level != null ? level.trim() : null)) {
                return true;
            }
            if (level != null && level.trim().equals(String.valueOf(triggerLevel))) {
                return true;
            }
        }
        return false;
    }

    private boolean matchProduct(CommissionPolicyDO policy, Long productId) {
        List<Long> productIds = policy.getApplicableProductIds();
        if (CollectionUtils.isEmpty(productIds)) {
            return true;
        }
        return productId != null && productIds.contains(productId);
    }

    private boolean matchPackage(CommissionPolicyDO policy, Long packageId) {
        List<Long> packageIds = policy.getApplicablePackageIds();
        if (CollectionUtils.isEmpty(packageIds)) {
            return true;
        }
        return packageId != null && packageIds.contains(packageId);
    }
}
