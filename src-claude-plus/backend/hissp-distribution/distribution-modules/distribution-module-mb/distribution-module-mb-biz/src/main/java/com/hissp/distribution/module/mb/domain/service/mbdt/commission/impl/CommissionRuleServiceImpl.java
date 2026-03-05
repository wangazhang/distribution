package com.hissp.distribution.module.mb.domain.service.mbdt.commission.impl;

import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleActionReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleConditionReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleMaterialReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleSaveReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleActionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleConditionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleEffectScope;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleMaterialDO;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionPolicyMapper;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionRuleActionMapper;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionRuleConditionMapper;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionRuleDefinitionMapper;
import com.hissp.distribution.module.mb.dal.mysql.commission.CommissionRuleMaterialMapper;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionRuleService;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
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
import static com.hissp.distribution.module.mb.enums.ErrorCodeConstants.COMMISSION_RULE_NOT_EXISTS;

@Service
@Validated
public class CommissionRuleServiceImpl implements CommissionRuleService {

    private static final String DEFAULT_STATUS = "ONLINE";

    @Resource
    private CommissionRuleDefinitionMapper commissionRuleDefinitionMapper;

    @Resource
    private CommissionRuleConditionMapper commissionRuleConditionMapper;

    @Resource
    private CommissionRuleActionMapper commissionRuleActionMapper;

    @Resource
    private CommissionRuleMaterialMapper commissionRuleMaterialMapper;

    @Resource
    private CommissionPolicyMapper commissionPolicyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRule(CommissionRuleSaveReqVO reqVO) {
        validatePolicy(reqVO.getPolicyId());
        CommissionRuleDefinitionDO definition = buildDefinition(reqVO);
        commissionRuleDefinitionMapper.insert(definition);
        saveRuleConditions(definition.getId(), reqVO.getConditions());
        saveRuleActions(definition.getId(), reqVO.getActions());
        saveRuleMaterials(definition.getId(), definition.getPolicyId(), reqVO.getMaterials());
        return definition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRule(CommissionRuleSaveReqVO reqVO) {
        CommissionRuleDefinitionDO existed = validateExists(reqVO.getId());
        CommissionRuleDefinitionDO definition = buildDefinition(reqVO);
        definition.setId(existed.getId());
        if (definition.getPolicyId() == null) {
            definition.setPolicyId(existed.getPolicyId());
        }
        commissionRuleDefinitionMapper.updateById(definition);
        saveRuleConditions(definition.getId(), reqVO.getConditions());
        saveRuleActions(definition.getId(), reqVO.getActions());
        saveRuleMaterials(definition.getId(), definition.getPolicyId(), reqVO.getMaterials());
    }

    @Override
    public void enableRule(Long id) {
        CommissionRuleDefinitionDO definition = validateExists(id);
        definition.setStatus("ONLINE");
        commissionRuleDefinitionMapper.updateById(definition);
    }

    @Override
    public void disableRule(Long id) {
        CommissionRuleDefinitionDO definition = validateExists(id);
        definition.setStatus("OFFLINE");
        commissionRuleDefinitionMapper.updateById(definition);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        CommissionRuleDefinitionDO definition = validateExists(id);
        commissionRuleConditionMapper.deleteByRuleId(id);
        commissionRuleActionMapper.deleteByRuleId(id);
        commissionRuleMaterialMapper.deleteByRuleId(id);
        commissionRuleDefinitionMapper.deleteById(definition.getId());
    }

    @Override
    public List<CommissionRuleDefinitionDO> getActiveRulesByPolicy(Long policyId) {
        List<CommissionRuleDefinitionDO> rules = commissionRuleDefinitionMapper.selectOnlineByPolicyId(policyId);
        attachRuleDetails(rules);
        return rules;
    }

    @Override
    public List<CommissionRuleDefinitionDO> getRuleListByPolicy(Long policyId) {
        List<CommissionRuleDefinitionDO> rules = commissionRuleDefinitionMapper.selectAllByPolicyId(policyId);
        attachRuleDetails(rules);
        return rules;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long cloneRule(Long id) {
        CommissionRuleDefinitionDO source = validateExists(id);
        CommissionRuleDefinitionDO clone = BeanUtils.toBean(source, CommissionRuleDefinitionDO.class);
        clone.setId(null);
        clone.setRuleCode(generateClonedRuleCode(source.getRuleCode()));
        clone.setDisplayName(source.getDisplayName() + "（副本）");
        clone.setStatus("DRAFT");
        clone.setCreateTime(null);
        clone.setUpdateTime(null);
        commissionRuleDefinitionMapper.insert(clone);

        List<CommissionRuleConditionDO> conditions = commissionRuleConditionMapper.selectByRuleIds(List.of(id));
        for (CommissionRuleConditionDO condition : conditions) {
            condition.setId(null);
            condition.setRuleId(clone.getId());
            commissionRuleConditionMapper.insert(condition);
        }
        List<CommissionRuleActionDO> actions = commissionRuleActionMapper.selectByRuleIds(List.of(id));
        for (CommissionRuleActionDO action : actions) {
            action.setId(null);
            action.setRuleId(clone.getId());
            commissionRuleActionMapper.insert(action);
        }
        List<CommissionRuleMaterialDO> materials = commissionRuleMaterialMapper.selectByRuleId(id);
        for (CommissionRuleMaterialDO material : materials) {
            CommissionRuleMaterialDO copy = BeanUtils.toBean(material, CommissionRuleMaterialDO.class);
            copy.setId(null);
            copy.setRuleId(clone.getId());
            copy.setPolicyId(clone.getPolicyId());
            copy.setCreateTime(null);
            copy.setUpdateTime(null);
            copy.setCreator(null);
            copy.setUpdater(null);
            commissionRuleMaterialMapper.insert(copy);
        }
        return clone.getId();
    }

    /**
     * 统一封装规则主体字段，补齐默认状态/版本号，并构建 effectScope。
     */
    private CommissionRuleDefinitionDO buildDefinition(CommissionRuleSaveReqVO reqVO) {
        CommissionRuleDefinitionDO definition = BeanUtils.toBean(reqVO, CommissionRuleDefinitionDO.class);
        definition.setStatus(StringUtils.hasText(reqVO.getStatus()) ? reqVO.getStatus() : DEFAULT_STATUS);
        definition.setVersionNo(reqVO.getVersionNo() != null ? reqVO.getVersionNo() : 1);
        definition.setEffectScope(buildEffectScope(reqVO));
        if (definition.getMetadata() == null) {
            definition.setMetadata(new HashMap<>());
        }
        return definition;
    }

    /**
     * effectScope 存放层级、等级、金额来源等摘要信息，便于前端快速展示命中范围。
     */
    private CommissionRuleEffectScope buildEffectScope(CommissionRuleSaveReqVO reqVO) {
        CommissionRuleEffectScope scope = new CommissionRuleEffectScope();
        if (!CollectionUtils.isEmpty(reqVO.getConditions())) {
            for (CommissionRuleConditionReqVO condition : reqVO.getConditions()) {
                if ("TARGET_HIERARCHY".equalsIgnoreCase(condition.getConditionType())) {
                    scope.setTargetHierarchy(extractFirstString(condition.getValue()));
                } else if ("TARGET_LEVEL".equalsIgnoreCase(condition.getConditionType())) {
                    String level = extractFirstString(condition.getValue());
                    scope.setTargetLevel(level);
                } else if ("INVITE_ORDER".equalsIgnoreCase(condition.getConditionType())) {
                    scope.setInviteOrder(buildInviteOrderScope(condition));
                }
            }
        }
        CommissionRuleActionReqVO commissionAction = resolveCommissionAction(reqVO.getActions());
        if (commissionAction != null && commissionAction.getPayload() != null) {
            Object amountSource = commissionAction.getPayload().get("amountSource");
            Object fundAccount = commissionAction.getPayload().get("fundAccount");
            scope.setAmountSource(amountSource != null ? String.valueOf(amountSource) : null);
            scope.setFundAccount(fundAccount != null ? String.valueOf(fundAccount) : null);
        }
        return scope;
    }

    private CommissionRuleEffectScope.InviteOrderScope buildInviteOrderScope(CommissionRuleConditionReqVO condition) {
        CommissionRuleEffectScope.InviteOrderScope inviteOrderScope = new CommissionRuleEffectScope.InviteOrderScope();
        inviteOrderScope.setOperator(condition.getOperator());
        if (condition.getValue() != null) {
            Object min = condition.getValue().get("min");
            Object max = condition.getValue().get("max");
            inviteOrderScope.setMinIndex(convertToInteger(min));
            inviteOrderScope.setMaxIndex(convertToInteger(max));
            Object indexes = condition.getValue().get("indexes");
            if (indexes instanceof List<?>) {
                @SuppressWarnings("unchecked")
                List<Integer> list = ((List<?>) indexes).stream()
                    .map(this::convertToInteger)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                inviteOrderScope.setIndexes(list);
            }
        }
        return inviteOrderScope;
    }

    /**
     * 找到佣金动作（若未显式配置则回退第一条），方便 effectScope 提取金额来源等元数据。
     */
    private CommissionRuleActionReqVO resolveCommissionAction(List<CommissionRuleActionReqVO> actions) {
        if (CollectionUtils.isEmpty(actions)) {
            return null;
        }
        for (CommissionRuleActionReqVO action : actions) {
            if (action != null && "COMMISSION".equalsIgnoreCase(action.getActionType())) {
                return action;
            }
        }
        return actions.get(0);
    }

    /**
     * 先删后插，确保条件配置幂等；未显式给出优先级时按传入顺序自增。
     */
    private void saveRuleConditions(Long ruleId, List<CommissionRuleConditionReqVO> conditions) {
        commissionRuleConditionMapper.deleteByRuleId(ruleId);
        if (CollectionUtils.isEmpty(conditions)) {
            return;
        }
        int index = 0;
        for (CommissionRuleConditionReqVO req : conditions) {
            if (req == null) {
                continue;
            }
            CommissionRuleConditionDO entity = new CommissionRuleConditionDO();
            entity.setRuleId(ruleId);
            entity.setConditionType(req.getConditionType());
            entity.setOperator(req.getOperator());
            entity.setValueJson(req.getValue());
            entity.setExtraJson(req.getExtra());
            entity.setPriority(req.getPriority() != null ? req.getPriority() : ++index);
            commissionRuleConditionMapper.insert(entity);
        }
    }

    /**
     * 动作同样采用“全量重建”策略，保持优先级与前端展示一致。
     */
    private void saveRuleActions(Long ruleId, List<CommissionRuleActionReqVO> actions) {
        commissionRuleActionMapper.deleteByRuleId(ruleId);
        if (CollectionUtils.isEmpty(actions)) {
            return;
        }
        int index = 0;
        for (CommissionRuleActionReqVO req : actions) {
            if (req == null) {
                continue;
            }
            CommissionRuleActionDO entity = new CommissionRuleActionDO();
            entity.setRuleId(ruleId);
            entity.setActionType(req.getActionType());
            entity.setAmountMode(req.getAmountMode());
            entity.setAmountValue(req.getAmountValue());
            entity.setCapValue(req.getCapValue());
            entity.setPayloadJson(req.getPayload());
            entity.setPriority(req.getPriority() != null ? req.getPriority() : ++index);
            commissionRuleActionMapper.insert(entity);
        }
    }

    /**
     * 物料信息量较小，直接全量删除后重建，避免判断增删改的复杂度。
     */
    private void saveRuleMaterials(Long ruleId, Long policyId, List<CommissionRuleMaterialReqVO> materials) {
        commissionRuleMaterialMapper.deleteByRuleId(ruleId);
        if (CollectionUtils.isEmpty(materials)) {
            return;
        }
        for (CommissionRuleMaterialReqVO vo : materials) {
            if (vo == null || vo.getMaterialId() == null || vo.getQuantity() == null) {
                continue;
            }
            CommissionRuleMaterialDO materialDO = new CommissionRuleMaterialDO();
            materialDO.setRuleId(ruleId);
            materialDO.setPolicyId(policyId);
            materialDO.setMaterialId(vo.getMaterialId());
            materialDO.setMaterialName(vo.getMaterialName());
            materialDO.setMaterialCode(vo.getMaterialCode());
            materialDO.setMaterialImage(vo.getMaterialImage());
            materialDO.setMaterialUnit(vo.getMaterialUnit());
            materialDO.setQuantity(vo.getQuantity());
            commissionRuleMaterialMapper.insert(materialDO);
        }
    }

    /**
     * 一次性加载所有附属配置（条件/动作/物料），避免调用层 N+1 查询。
     */
    private void attachRuleDetails(List<CommissionRuleDefinitionDO> rules) {
        if (CollectionUtils.isEmpty(rules)) {
            return;
        }
        List<Long> ruleIds = rules.stream()
            .map(CommissionRuleDefinitionDO::getId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if (ruleIds.isEmpty()) {
            return;
        }
        Map<Long, List<CommissionRuleConditionDO>> conditionMap = commissionRuleConditionMapper.selectByRuleIds(ruleIds)
            .stream()
            .collect(Collectors.groupingBy(CommissionRuleConditionDO::getRuleId));
        Map<Long, List<CommissionRuleActionDO>> actionMap = commissionRuleActionMapper.selectByRuleIds(ruleIds)
            .stream()
            .collect(Collectors.groupingBy(CommissionRuleActionDO::getRuleId));
        Map<Long, List<CommissionRuleMaterialDO>> materialMap = commissionRuleMaterialMapper.selectByRuleIds(ruleIds)
            .stream()
            .collect(Collectors.groupingBy(CommissionRuleMaterialDO::getRuleId));
        rules.forEach(rule -> {
            rule.setConditions(conditionMap.getOrDefault(rule.getId(), new ArrayList<>()));
            rule.setActions(actionMap.getOrDefault(rule.getId(), new ArrayList<>()));
            rule.setMaterials(materialMap.getOrDefault(rule.getId(), new ArrayList<>()));
        });
    }

    private void validatePolicy(Long policyId) {
        CommissionPolicyDO policy = commissionPolicyMapper.selectById(policyId);
        if (policy == null) {
            throw ServiceExceptionUtil.exception(COMMISSION_POLICY_NOT_EXISTS);
        }
    }

    private CommissionRuleDefinitionDO validateExists(Long id) {
        CommissionRuleDefinitionDO definition = commissionRuleDefinitionMapper.selectById(id);
        if (definition == null) {
            throw ServiceExceptionUtil.exception(COMMISSION_RULE_NOT_EXISTS);
        }
        return definition;
    }

    private Integer convertToInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String str) {
            if (!StringUtils.hasText(str)) {
                return null;
            }
            return Integer.parseInt(str);
        }
        return null;
    }

    private String extractFirstString(Map<String, Object> value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        Object values = value.get("values");
        if (values instanceof List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            return first != null ? String.valueOf(first) : null;
        }
        Object single = value.get("value");
        if (single != null) {
            return String.valueOf(single);
        }
        return null;
    }

    private String generateClonedRuleCode(String ruleCode) {
        String base = StringUtils.hasText(ruleCode) ? ruleCode : "rule";
        return base + "_COPY_" + System.currentTimeMillis();
    }
}
