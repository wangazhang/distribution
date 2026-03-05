package com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine.impl;

import com.hissp.distribution.module.mb.api.commission.dto.CommissionMaterialGrantDTO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleActionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleConditionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleEffectScope;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleMaterialDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine.CommissionCalculationResult;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine.CommissionCalculator;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionContext;
import com.hissp.distribution.module.mb.enums.MbCommissionAmountModeEnum;
import com.hissp.distribution.module.mb.enums.MbCommissionAmountSourceEnum;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
@Slf4j
public class CommissionCalculatorImpl implements CommissionCalculator {

    private static final String MATERIAL_GRANT_BIZ_TYPE = "MB_COMMISSION_MATERIAL_GRANT";
    private static final String ATTR_INVITE_ORDER_INDEX = "inviteOrderIndex";

    @Override
    public CommissionCalculationResult calculate(CommissionContext context,
        CommissionPolicyDO policy, List<CommissionRuleDefinitionDO> rules) {
        CommissionCalculationResult result = new CommissionCalculationResult();
        if (CollectionUtils.isEmpty(rules)) {
            result.setBrokerageRecords(Collections.emptyList());
            result.setMaterialGrants(Collections.emptyList());
            return result;
        }
        List<BrokerageRecordCreateReqDTO> allRecords = new ArrayList<>();
        List<CommissionMaterialGrantDTO> allMaterialGrants = new ArrayList<>();
        for (CommissionRuleDefinitionDO rule : rules) {
            if (!matchConditions(context, rule)) {
                continue;
            }
            Long orderId = context.getOrder() != null ? context.getOrder().getOrderId() : null;
            Long triggerUserId = context.getTriggerUserId();
            log.info("[CommissionCalculator][ruleHit][orderId={}, ruleId={}, ruleCode={}, userId={}]",
                orderId != null ? orderId : "NA",
                rule.getId(),
                rule.getRuleCode(),
                triggerUserId != null ? triggerUserId : "NA");
            RuleExecutionResult perRule = executeRule(context, policy, rule);
            allRecords.addAll(perRule.getBrokerageRecords());
            allMaterialGrants.addAll(perRule.getMaterialGrants());
        }
        result.setBrokerageRecords(allRecords);
        result.setMaterialGrants(allMaterialGrants);
        return result;
    }

    /**
     * 执行单条规则，分别处理金钱与物料动作。
     */
    private RuleExecutionResult executeRule(CommissionContext context,
        CommissionPolicyDO policy, CommissionRuleDefinitionDO rule) {
        RuleExecutionResult result = new RuleExecutionResult();
        Long targetUserId = resolveTargetUser(context, rule);
        if (targetUserId == null) {
            return result;
        }
        for (CommissionRuleActionDO action : rule.safeActions()) {
            if (action == null || !StringUtils.hasText(action.getActionType())) {
                continue;
            }
            switch (action.getActionType().toUpperCase(Locale.ROOT)) {
                case "COMMISSION":
                case "EXTRA_COMMISSION":
                    List<BrokerageRecordCreateReqDTO> records = buildCommissionRecords(context, policy, rule, action, targetUserId);
                    result.getBrokerageRecords().addAll(records);
                    break;
                default:
                    log.debug("[executeRule][ruleId={}, actionType={}] 暂不支持该动作类型，忽略", rule.getId(), action.getActionType());
                    break;
            }
        }

        List<CommissionMaterialGrantDTO> materialGrants = buildMaterialGrants(context, policy, rule, targetUserId);
        result.getMaterialGrants().addAll(materialGrants);
        return result;
    }

    /**
     * 条件匹配入口：逐条评估规则里的条件，只要有一条不满足即短路。
     */
    private boolean matchConditions(CommissionContext context, CommissionRuleDefinitionDO rule) {
        List<CommissionRuleConditionDO> conditions = rule.safeConditions();
        if (CollectionUtils.isEmpty(conditions)) {
            return true;
        }
        for (CommissionRuleConditionDO condition : conditions) {
            if (!matchCondition(context, rule, condition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 不同条件类型分别解析：层级/等级使用层级树，邀请顺序读取 context attribute。
     */
    private boolean matchCondition(CommissionContext context, CommissionRuleDefinitionDO rule,
        CommissionRuleConditionDO condition) {
        if (condition == null || !StringUtils.hasText(condition.getConditionType())) {
            return true;
        }
        String type = condition.getConditionType().toUpperCase(Locale.ROOT);
        return switch (type) {
            case "TARGET_HIERARCHY" -> matchHierarchy(rule, condition);
            case "TARGET_LEVEL" -> matchTargetLevel(context, rule, condition);
            case "INVITE_ORDER" -> matchInviteOrder(context, condition);
            default -> true;
        };
    }

    private boolean matchHierarchy(CommissionRuleDefinitionDO rule, CommissionRuleConditionDO condition) {
        String currentHierarchy = resolveTargetHierarchy(rule);
        List<String> expected = readStringList(condition.getValueJson());
        if (CollectionUtils.isEmpty(expected)) {
            return true;
        }
        return expected.stream().anyMatch(item -> equalsHierarchy(item, currentHierarchy));
    }

    private boolean matchTargetLevel(CommissionContext context, CommissionRuleDefinitionDO rule,
        CommissionRuleConditionDO condition) {
        if ("ALWAYS".equalsIgnoreCase(condition.getOperator())) {
            return true;
        }
        String hierarchy = resolveTargetHierarchy(rule);
        Integer actualLevel = context.lookupLevelByHierarchy(hierarchy);
        if (actualLevel == null) {
            return false;
        }
        List<String> expected = readStringList(condition.getValueJson());
        if (CollectionUtils.isEmpty(expected)) {
            return true;
        }
        return expected.stream().anyMatch(item -> String.valueOf(actualLevel).equals(item));
    }

    /**
     * 邀请顺序支持 EQ/IN/BETWEEN/GE/LE 等多种 operator，统一转为数值比较。
     */
    private boolean matchInviteOrder(CommissionContext context, CommissionRuleConditionDO condition) {
        if ("ALWAYS".equalsIgnoreCase(condition.getOperator())) {
            return true;
        }
        Integer inviteOrder = resolveInviteOrder(context);
        if (inviteOrder == null) {
            return false;
        }
        Map<String, Object> value = condition.getValueJson() != null ? condition.getValueJson() : new HashMap<>();
        List<Integer> indexes = readIntegerList(value.get("indexes"));
        Integer min = asInteger(value.get("min"));
        Integer max = asInteger(value.get("max"));
        String op = condition.getOperator().toUpperCase(Locale.ROOT);
        return switch (op) {
            case "EQ" -> !indexes.isEmpty() && indexes.contains(inviteOrder);
            case "IN" -> indexes.isEmpty() || indexes.contains(inviteOrder);
            case "BETWEEN" -> {
                if (min == null && max == null) {
                    yield true;
                }
                if (min != null && inviteOrder < min) {
                    yield false;
                }
                if (max != null && inviteOrder > max) {
                    yield false;
                }
                yield true;
            }
            case "GE" -> min == null || inviteOrder >= min;
            case "LE" -> max == null || inviteOrder <= max;
            default -> true;
        };
    }

    /**
     * 根据动作配置生成收益/出资两条流水；payload 中的资金账户、字典编码在此前已写入。
     */
    private List<BrokerageRecordCreateReqDTO> buildCommissionRecords(CommissionContext context,
        CommissionPolicyDO policy, CommissionRuleDefinitionDO rule, CommissionRuleActionDO action, Long targetUserId) {
        Integer amount = calculateAmount(context, action);
        if (amount == null || amount == 0) {
            return Collections.emptyList();
        }
        Integer normalizedAmount = Math.abs(amount);
        Integer bizCode = getPayloadInteger(action, "bizCode");
        if (bizCode == null) {
            log.warn("[buildCommissionRecords][ruleId={}] 动作未配置 bizCode，跳过佣金记录", rule.getId());
            return Collections.emptyList();
        }
        BrokerageRecordCreateReqDTO incomeRecord = new BrokerageRecordCreateReqDTO();
        incomeRecord.setUserId(targetUserId)
            .setBizId(resolveBizId(context))
            .setBizType(bizCode)
            .setTitle(rule.getDisplayName())
            .setDescription(rule.getDisplayName())
            .setPrice(normalizedAmount)
            .setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus())
            .setSourceUserLevel(context.getTriggerLevel())
            .setSourceUserId(context.getTriggerUserId())
            .setStrategyCode(policy.getPolicyCode())
            .setStrategyRuleId(rule.getId())
            .setStrategyDisplayName(rule.getDisplayName())
            .setBizCategory(resolveBizCategory(context));

        List<BrokerageRecordCreateReqDTO> records = new ArrayList<>();
        records.add(incomeRecord);

        Integer fundBizCode = getPayloadInteger(action, "fundBizCode");
        String fundAccount = getPayloadString(action, "fundAccount");
        if (fundBizCode != null && StringUtils.hasText(fundAccount)) {
            Long fundUserId = resolveFundUser(context, fundAccount);
            if (fundUserId != null) {
                BrokerageRecordCreateReqDTO fundRecord = new BrokerageRecordCreateReqDTO();
                fundRecord.setUserId(fundUserId)
                    .setBizId(incomeRecord.getBizId())
                    .setBizType(fundBizCode)
                    .setTitle(rule.getDisplayName() + " - 出资扣减")
                    .setDescription(rule.getDisplayName() + " - 出资扣减")
                    .setPrice(-normalizedAmount)
                    .setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus())
                    .setSourceUserId(context.getTriggerUserId())
                    .setSourceUserLevel(context.getTriggerLevel())
                    .setStrategyCode(policy.getPolicyCode())
                    .setStrategyRuleId(rule.getId())
                    .setStrategyDisplayName(rule.getDisplayName())
                    .setBizCategory(resolveBizCategory(context));
                records.add(fundRecord);
            } else {
                log.warn("[buildCommissionRecords][ruleId={}] fundAccount({}) 未命中层级，跳过出资记录",
                    rule.getId(), fundAccount);
            }
        }
        return records;
    }

    /**
     * 金额计算：根据 amountSource 抽取 base，再依据 FIXED/PERCENTAGE 计算最终分值。
     */
    private Integer calculateAmount(CommissionContext context, CommissionRuleActionDO action) {
        if (action == null || !StringUtils.hasText(action.getAmountMode())) {
            return null;
        }
        String amountSourceCode = getPayloadString(action, "amountSource");
        MbCommissionAmountSourceEnum sourceEnum = MbCommissionAmountSourceEnum.of(amountSourceCode);
        MbCommissionAmountModeEnum modeEnum = MbCommissionAmountModeEnum.of(action.getAmountMode());
        if (sourceEnum == null || modeEnum == null || action.getAmountValue() == null) {
            return null;
        }
        BigDecimal base = sourceEnum.extractBase(context);
        if (base == null) {
            return null;
        }
        if (modeEnum == MbCommissionAmountModeEnum.PERCENTAGE) {
            return base.multiply(action.getAmountValue())
                .divide(BigDecimal.valueOf(100), 0, BigDecimal.ROUND_HALF_UP)
                .intValue();
        }
        return action.getAmountValue().intValue();
    }

    /**
     * 物料奖励直接转为 DTO，下游依 bizKey 做幂等；与佣金互不影响。
     */
    private List<CommissionMaterialGrantDTO> buildMaterialGrants(CommissionContext context,
        CommissionPolicyDO policy, CommissionRuleDefinitionDO rule, Long targetUserId) {
        List<CommissionRuleMaterialDO> materials = rule.safeMaterials();
        if (CollectionUtils.isEmpty(materials)) {
            return Collections.emptyList();
        }
        String bizId = resolveBizId(context);
        String baseBizKey = StringUtils.hasText(bizId)
            ? bizId
            : "POLICY-" + (policy.getId() != null ? policy.getId() : "unknown");
        String targetHierarchy = resolveTargetHierarchy(rule);
        List<CommissionMaterialGrantDTO> grants = new ArrayList<>(materials.size());
        for (CommissionRuleMaterialDO material : materials) {
            if (material == null || material.getMaterialId() == null || material.getQuantity() == null
                || material.getQuantity() <= 0) {
                continue;
            }
            CommissionMaterialGrantDTO dto = new CommissionMaterialGrantDTO();
            dto.setPolicyId(policy.getId());
            dto.setPolicyCode(policy.getPolicyCode());
            dto.setRuleId(rule.getId());
            dto.setRuleDisplayName(rule.getDisplayName());
            dto.setTargetHierarchy(targetHierarchy);
            dto.setUserId(targetUserId);
            dto.setMaterialId(material.getMaterialId());
            dto.setMaterialName(material.getMaterialName());
            dto.setMaterialCode(material.getMaterialCode());
            dto.setMaterialImage(material.getMaterialImage());
            dto.setMaterialUnit(material.getMaterialUnit());
            dto.setQuantity(material.getQuantity());
            dto.setBizType(MATERIAL_GRANT_BIZ_TYPE);
            dto.setReason((rule.getDisplayName() != null ? rule.getDisplayName() : "分佣规则") + " - 物料奖励");
            dto.setBizKey(String.format("%s:R%s:M%s:U%s", baseBizKey,
                rule.getId() != null ? rule.getId() : "NA",
                material.getMaterialId(),
                targetUserId != null ? targetUserId : -1));
            grants.add(dto);
        }
        return grants;
    }

    private String resolveBizId(CommissionContext context) {
        if (context.getOrder() != null && context.getOrder().getOrderId() != null) {
            return String.valueOf(context.getOrder().getOrderId());
        }
        Object bizId = context.getAttribute("bizId");
        return bizId != null ? String.valueOf(bizId) : null;
    }

    private Long resolveTargetUser(CommissionContext context, CommissionRuleDefinitionDO rule) {
        String hierarchy = resolveTargetHierarchy(rule);
        if (!StringUtils.hasText(hierarchy)) {
            return null;
        }
        return context.resolveUserIdByHierarchy(hierarchy);
    }

    private String resolveTargetHierarchy(CommissionRuleDefinitionDO rule) {
        CommissionRuleEffectScope scope = rule.getEffectScope();
        if (scope != null && StringUtils.hasText(scope.getTargetHierarchy())) {
            return scope.getTargetHierarchy();
        }
        return "PARENT";
    }

    private Long resolveFundUser(CommissionContext context, String hierarchy) {
        return context.resolveUserIdByHierarchy(hierarchy);
    }

    private Integer resolveInviteOrder(CommissionContext context) {
        Object value = context.getAttribute(ATTR_INVITE_ORDER_INDEX);
        if (value == null) {
            value = context.getAttribute("inviteOrderRank");
        }
        return asInteger(value);
    }

    private Integer resolveBizCategory(CommissionContext context) {
        Object value = context.getAttribute(BrokerageRecordBizCategoryEnum.ATTR_KEY);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignore) {
                // ignore invalid format
            }
        }
        return BrokerageRecordBizCategoryEnum.MB_ORDER.getType();
    }

    private List<String> readStringList(Map<String, Object> valueJson) {
        if (valueJson == null) {
            return Collections.emptyList();
        }
        Object values = valueJson.get("values");
        if (values instanceof List<?> list) {
            return list.stream()
                .map(item -> item != null ? String.valueOf(item) : null)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
        }
        Object single = valueJson.get("value");
        if (single instanceof String str && StringUtils.hasText(str)) {
            return List.of(str);
        }
        return Collections.emptyList();
    }

    private List<Integer> readIntegerList(Object source) {
        if (!(source instanceof List<?> list) || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Integer> result = new ArrayList<>(list.size());
        for (Object item : list) {
            Integer value = asInteger(item);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }

    private Integer getPayloadInteger(CommissionRuleActionDO action, String key) {
        if (action.getPayloadJson() == null) {
            return null;
        }
        return asInteger(action.getPayloadJson().get(key));
    }

    private String getPayloadString(CommissionRuleActionDO action, String key) {
        if (action.getPayloadJson() == null) {
            return null;
        }
        Object value = action.getPayloadJson().get(key);
        return value != null ? String.valueOf(value) : null;
    }

    private boolean equalsHierarchy(String expected, String actual) {
        if (!StringUtils.hasText(expected) || !StringUtils.hasText(actual)) {
            return false;
        }
        return expected.trim().equalsIgnoreCase(actual.trim());
    }

    private static class RuleExecutionResult {
        private final List<BrokerageRecordCreateReqDTO> brokerageRecords = new ArrayList<>();
        private final List<CommissionMaterialGrantDTO> materialGrants = new ArrayList<>();

        public List<BrokerageRecordCreateReqDTO> getBrokerageRecords() {
            return brokerageRecords;
        }

        public List<CommissionMaterialGrantDTO> getMaterialGrants() {
            return materialGrants;
        }
    }
}
