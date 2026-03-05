package com.hissp.distribution.module.mb.api.commission;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateReqDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateRespDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionHierarchyDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionOrderDTO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionPolicyService;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionRuleService;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine.CommissionCalculationResult;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine.CommissionCalculator;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionVirtualAccounts;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionContext;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionContext.CommissionOrderSnapshot;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionPolicyMatchContext;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@Slf4j
public class CommissionStrategyApiImpl implements CommissionStrategyApi {
    private static final String ATTR_INVITE_ORDER_INDEX = "inviteOrderIndex";

    @Resource
    private CommissionPolicyService commissionPolicyService;

    @Resource
    private CommissionRuleService commissionRuleService;

    @Resource
    private CommissionCalculator commissionCalculator;
    @Resource
    private BrokerageUserApi brokerageUserApi;

    @Override
    public CommissionCalculateRespDTO calculateCommission(CommissionCalculateReqDTO reqDTO) {
        // 0. 请求兜底：bizType 为空直接返回空结果
        if (reqDTO == null || !StringUtils.hasText(reqDTO.getBizType())) {
            return CommissionCalculateRespDTO.empty();
        }
        // 1. 匹配策略
        CommissionPolicyDO policy = resolvePolicy(reqDTO);
        if (policy == null) {
            log.debug("[CommissionStrategyApi][calculateCommission] 未匹配到策略, bizType={}, productId={}, packageId={}",
                reqDTO.getBizType(), reqDTO.getProductId(), reqDTO.getPackageId());
            return CommissionCalculateRespDTO.empty();
        }
        // 2. 拉取启用规则
        List<CommissionRuleDefinitionDO> rules = commissionRuleService.getActiveRulesByPolicy(policy.getId());
        if (CollectionUtils.isEmpty(rules)) {
            log.warn("[CommissionStrategyApi][calculateCommission] 策略({})未配置启用规则", policy.getPolicyCode());
            return CommissionCalculateRespDTO.empty();
        }
        // 3. 构建上下文并执行引擎
        CommissionContext context = buildContext(reqDTO);
        CommissionCalculationResult calcResult = commissionCalculator.calculate(context, policy, rules);
        List<BrokerageRecordCreateReqDTO> records = calcResult.getBrokerageRecords();

        CommissionCalculateRespDTO resp = new CommissionCalculateRespDTO();
        resp.setPolicyId(policy.getId());
        resp.setPolicyCode(policy.getPolicyCode());
        resp.setPolicyDisplayName(policy.getDisplayName());
        resp.setRecords(records != null ? records : Collections.emptyList());
        resp.setMaterialGrants(calcResult.getMaterialGrants() != null ? calcResult.getMaterialGrants() : Collections.emptyList());
        resp.setMatched(true);
        return resp;
    }

    /**
     * 策略匹配优先级：
     * 1. 指定 policyId/policyCode；
     * 2. 按 bizType + 层级 + 商品/套包 自动匹配上线策略。
     */
    private CommissionPolicyDO resolvePolicy(CommissionCalculateReqDTO reqDTO) {
        CommissionPolicyDO policy = null;
        if (reqDTO.getPolicyId() != null) {
            policy = commissionPolicyService.getPolicy(reqDTO.getPolicyId());
        }
        if (policy == null && StringUtils.hasText(reqDTO.getPolicyCode())) {
            policy = commissionPolicyService.getPolicyByCode(reqDTO.getPolicyCode());
        }
        if (policy != null) {
            return policy;
        }
        Integer triggerLevel = extractHierarchyLevel(reqDTO.getHierarchies(), "SELF");
        CommissionPolicyMatchContext matchContext = new CommissionPolicyMatchContext()
            .setBizType(reqDTO.getBizType())
            .setTriggerLevel(triggerLevel)
            .setProductId(resolveProductId(reqDTO))
            .setPackageId(resolvePackageId(reqDTO));
        return commissionPolicyService.matchOnlinePolicy(matchContext);
    }

    /**
     * 构建分佣上下文：包含层级关系、订单快照、业务属性，以及邀请顺序等扩展指标。
     */
    private CommissionContext buildContext(CommissionCalculateReqDTO reqDTO) {
        // 1. 构建上下文基本骨架
        CommissionContext context = new CommissionContext();
        // 2. 还原上下层级链路（SELF/PARENT/TEAM 等）
        if (!CollectionUtils.isEmpty(reqDTO.getHierarchies())) {
            for (CommissionHierarchyDTO hierarchy : reqDTO.getHierarchies()) {
                if (hierarchy == null || !StringUtils.hasText(hierarchy.getHierarchy())) {
                    continue;
                }
                context.assignHierarchy(hierarchy.getHierarchy(), hierarchy.getUserId(), hierarchy.getLevel());
            }
        }
        // 3. 构建订单快照，后续金额计算需要
        CommissionOrderDTO orderDTO = reqDTO.getOrder();
        if (orderDTO != null) {
            CommissionOrderSnapshot snapshot = new CommissionOrderSnapshot();
            snapshot.setOrderId(orderDTO.getOrderId());
            snapshot.setBizOrderNo(orderDTO.getBizOrderNo());
            snapshot.setProductId(orderDTO.getProductId() != null ? orderDTO.getProductId() : reqDTO.getProductId());
            snapshot.setPackageId(orderDTO.getPackageId() != null ? orderDTO.getPackageId() : reqDTO.getPackageId());
            snapshot.setQuantity(orderDTO.getQuantity());
            snapshot.setUnitPrice(orderDTO.getUnitPrice());
            snapshot.setTotalPrice(orderDTO.getTotalPrice());
            context.setOrder(snapshot);
        } else {
            CommissionOrderSnapshot snapshot = new CommissionOrderSnapshot();
            snapshot.setProductId(reqDTO.getProductId());
            snapshot.setPackageId(reqDTO.getPackageId());
            context.setOrder(snapshot);
        }
        // 4. 写入业务属性 + 调用方透传的 attributes
        context.getAttributes().put("bizType", reqDTO.getBizType());
        Map<String, Object> attributes = reqDTO.getAttributes();
        if (attributes != null && !attributes.isEmpty()) {
            context.mergeAttributes(attributes);
        }
        // 5. 自动补充邀请顺序，供 INVITE_ORDER 条件匹配
        enrichInviteOrderAttribute(context);
        // 6. 确保虚拟层级存在（平台/HQ），防止规则引用时遗漏
        context.ensureHierarchy(CommissionVirtualAccounts.HIERARCHY_PLATFORM,
            CommissionVirtualAccounts.PLATFORM_VIRTUAL_USER_ID, null);
        context.ensureHierarchy(CommissionVirtualAccounts.HIERARCHY_HQ,
            CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID, null);
        return context;
    }

    /**
     * 提取指定层级（SELF/PARENT ...）的用户等级，辅助策略匹配。
     */
    private Integer extractHierarchyLevel(List<CommissionHierarchyDTO> hierarchies, String hierarchy) {
        if (CollectionUtils.isEmpty(hierarchies)) {
            return null;
        }
        return hierarchies.stream()
            .filter(item -> item != null && StringUtils.hasText(item.getHierarchy())
                && hierarchy.equalsIgnoreCase(item.getHierarchy()))
            .map(CommissionHierarchyDTO::getLevel)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    /**
     * 商品 ID 优先取订单快照，退化到请求级字段。
     */
    private Long resolveProductId(CommissionCalculateReqDTO reqDTO) {
        if (reqDTO.getOrder() != null && reqDTO.getOrder().getProductId() != null) {
            return reqDTO.getOrder().getProductId();
        }
        return reqDTO.getProductId();
    }

    /**
     * 套包 ID 优先取订单快照，退化到请求级字段。
     */
    private Long resolvePackageId(CommissionCalculateReqDTO reqDTO) {
        if (reqDTO.getOrder() != null && reqDTO.getOrder().getPackageId() != null) {
            return reqDTO.getOrder().getPackageId();
        }
        return reqDTO.getPackageId();
    }

    /**
     * 注入邀请顺序：
     * 1. 优先使用调用方显式传入的 inviteOrderIndex / inviteOrderRank；
     * 2. 否则查询 trade 模块中推广关系，按绑定时间排序计算被邀请序号；
     * 3. 同时写入两个 attribute，兼容旧逻辑。
     */
    private void enrichInviteOrderAttribute(CommissionContext context) {
        Integer preset = normalizeInviteOrder(context.getAttribute(ATTR_INVITE_ORDER_INDEX));
        if (preset == null) {
            preset = normalizeInviteOrder(context.getAttribute("inviteOrderRank"));
        }
        if (preset != null) {
            context.getAttributes().put(ATTR_INVITE_ORDER_INDEX, preset);
            context.getAttributes().put("inviteOrderRank", preset);
            return;
        }
        Long triggerUserId = context.getTriggerUserId();
        if (triggerUserId == null) {
            return;
        }
        Integer inviteOrder = brokerageUserApi != null ? brokerageUserApi.getInviteOrderIndex(triggerUserId) : null;
        if (inviteOrder != null) {
            context.getAttributes().put(ATTR_INVITE_ORDER_INDEX, inviteOrder);
            context.getAttributes().put("inviteOrderRank", inviteOrder);
        }
    }

    /**
     * 兼容数值/字符串形式的 inviteOrderIndex。
     */
    private Integer normalizeInviteOrder(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String str && StringUtils.hasText(str)) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignore) {
                return null;
            }
        }
        return null;
    }
}
