package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission;

import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicyPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicyRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicySaveReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicySimpleRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionSimulationReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionSimulationRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.result.CommissionSimulationRuleMaterialRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.result.CommissionSimulationRuleRespVO;
import com.hissp.distribution.module.mb.api.commission.CommissionStrategyApi;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateReqDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateRespDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionHierarchyDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionMaterialGrantDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionOrderDTO;
import com.hissp.distribution.module.mb.convert.commission.CommissionPolicyConvert;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleActionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleEffectScope;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionPolicyService;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionRuleService;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.module.mb.enums.ErrorCodeConstants.COMMISSION_POLICY_NOT_EXISTS;

@Tag(name = "管理后台 - 分佣策略配置")
@RestController
@RequestMapping("/mb/commission-policy")
@Validated
public class CommissionPolicyController {

    @Resource
    private CommissionPolicyService commissionPolicyService;

    @Resource
    private CommissionPolicyConvert commissionPolicyConvert;

    @Resource
    private CommissionRuleService commissionRuleService;

    @Resource
    private CommissionStrategyApi commissionStrategyApi;

    @PostMapping("/create")
    @Operation(summary = "创建分佣策略")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:create')")
    public CommonResult<Long> createPolicy(@Valid @RequestBody CommissionPolicySaveReqVO createReqVO) {
        return success(commissionPolicyService.createPolicy(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新分佣策略")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:update')")
    public CommonResult<Boolean> updatePolicy(@Valid @RequestBody CommissionPolicySaveReqVO updateReqVO) {
        commissionPolicyService.updatePolicy(updateReqVO);
        return success(Boolean.TRUE);
    }

    @PostMapping("/publish")
    @Operation(summary = "发布分佣策略")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:publish')")
    public CommonResult<Boolean> publishPolicy(@RequestParam("id") Long id) {
        commissionPolicyService.publishPolicy(id);
        return success(Boolean.TRUE);
    }

    @PostMapping("/offline")
    @Operation(summary = "下线分佣策略")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:offline')")
    public CommonResult<Boolean> offlinePolicy(@RequestParam("id") Long id) {
        commissionPolicyService.offlinePolicy(id);
        return success(Boolean.TRUE);
    }

    @PostMapping("/clone")
    @Operation(summary = "克隆分佣策略")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:clone')")
    public CommonResult<Long> clonePolicy(@RequestParam("id") Long id) {
        return success(commissionPolicyService.clonePolicy(id));
    }

    @GetMapping("/get")
    @Operation(summary = "获取分佣策略详情")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:query')")
    public CommonResult<CommissionPolicyRespVO> getPolicy(@RequestParam("id") Long id) {
        CommissionPolicyDO policyDO = commissionPolicyService.getPolicy(id);
        return success(commissionPolicyConvert.convert(policyDO));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询分佣策略")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:query')")
    public CommonResult<PageResult<CommissionPolicyRespVO>> getPolicyPage(@Valid CommissionPolicyPageReqVO pageReqVO) {
        PageResult<CommissionPolicyDO> pageResult = commissionPolicyService.getPolicyPage(pageReqVO);
        return success(commissionPolicyConvert.convertPage(pageResult));
    }

    @GetMapping("/list-online")
    @Operation(summary = "查询在线分佣策略（简要）")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:query')")
    public CommonResult<List<CommissionPolicySimpleRespVO>> getOnlinePolicyList() {
        List<CommissionPolicyDO> list = commissionPolicyService.getOnlinePolicyList();
        return success(commissionPolicyConvert.convertSimpleList(list));
    }

    @PostMapping("/simulate")
    @Operation(summary = "模拟分佣结果")
    @PreAuthorize("@ss.hasPermission('mb:commission-policy:simulate')")
    public CommonResult<CommissionSimulationRespVO> simulate(@Valid @RequestBody CommissionSimulationReqVO reqVO) {
        CommissionPolicyDO policy = resolvePolicy(reqVO);
        List<CommissionRuleDefinitionDO> rules = commissionRuleService.getActiveRulesByPolicy(policy.getId());
        CommissionCalculateReqDTO calculateReq = buildSimulationRequest(reqVO, policy);
        CommissionCalculateRespDTO calculateResp = commissionStrategyApi.calculateCommission(calculateReq);
        CommissionSimulationRespVO respVO = buildSimulationResponse(policy, calculateReq, rules, calculateResp);
        return success(respVO);
    }

    private CommissionPolicyDO resolvePolicy(CommissionSimulationReqVO reqVO) {
        CommissionPolicyDO policy = null;
        if (reqVO.getPolicyId() != null) {
            policy = commissionPolicyService.getPolicy(reqVO.getPolicyId());
        }
        if (policy == null && StringUtils.hasText(reqVO.getPolicyCode())) {
            policy = commissionPolicyService.getPolicyByCode(reqVO.getPolicyCode());
        }
        if (policy == null) {
            throw ServiceExceptionUtil.exception(COMMISSION_POLICY_NOT_EXISTS);
        }
        return policy;
    }

    private CommissionCalculateReqDTO buildSimulationRequest(CommissionSimulationReqVO reqVO, CommissionPolicyDO policy) {
        CommissionCalculateReqDTO req = new CommissionCalculateReqDTO();
        req.setPolicyId(policy.getId());
        req.setPolicyCode(policy.getPolicyCode());
        req.setBizType(policy.getBizType());
        req.setProductId(reqVO.getProductId());
        req.setPackageId(reqVO.getPackageId());
        CommissionOrderDTO orderDTO = new CommissionOrderDTO();
        orderDTO.setOrderId(-1L);
        orderDTO.setProductId(reqVO.getProductId());
        orderDTO.setPackageId(reqVO.getPackageId());
        orderDTO.setQuantity(reqVO.getQuantity() != null ? reqVO.getQuantity() : 1);
        orderDTO.setUnitPrice(reqVO.getUnitPrice());
        orderDTO.setTotalPrice(reqVO.getTotalPrice());
        req.setOrder(orderDTO);
        req.setHierarchies(buildSimulationHierarchies(reqVO));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(BrokerageRecordBizCategoryEnum.ATTR_KEY, BrokerageRecordBizCategoryEnum.MB_ORDER.getType());
        req.setAttributes(attributes);
        return req;
    }

    private List<CommissionHierarchyDTO> buildSimulationHierarchies(CommissionSimulationReqVO reqVO) {
        List<CommissionHierarchyDTO> list = new ArrayList<>();
        list.add(createHierarchy("SELF", reqVO.getAgentUserId(), reqVO.getSelfLevel()));
        list.add(createHierarchy("PARENT", reqVO.getParentUserId(), reqVO.getParentLevel()));
        list.add(createHierarchy("GRANDPARENT", reqVO.getGrandParentUserId(), reqVO.getGrandParentLevel()));
        list.add(createHierarchy("TEAM", reqVO.getTeamUserId(), reqVO.getTeamLevel()));
        list.add(createHierarchy("HQ", reqVO.getHqUserId(), null));
        return list.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    private CommissionHierarchyDTO createHierarchy(String code, Long userId, Integer level) {
        if (userId == null) {
            return null;
        }
        CommissionHierarchyDTO dto = new CommissionHierarchyDTO();
        dto.setHierarchy(code);
        dto.setUserId(userId);
        dto.setLevel(level);
        dto.setBrokerageEnabled(Boolean.TRUE);
        return dto;
    }

    private CommissionSimulationRespVO buildSimulationResponse(CommissionPolicyDO policy, CommissionCalculateReqDTO reqDTO,
        List<CommissionRuleDefinitionDO> rules, CommissionCalculateRespDTO calculateResp) {
        CommissionSimulationRespVO respVO = new CommissionSimulationRespVO();
        respVO.setPolicyId(policy.getId());
        respVO.setPolicyCode(policy.getPolicyCode());
        respVO.setPolicyName(policy.getDisplayName());
        respVO.setVersionNo(policy.getVersionNo());
        CommissionOrderDTO orderDTO = reqDTO.getOrder();
        respVO.setOrderTotalPrice(orderDTO != null ? orderDTO.getTotalPrice() : null);
        respVO.setUnitPrice(orderDTO != null ? orderDTO.getUnitPrice() : null);
        respVO.setQuantity(orderDTO != null ? orderDTO.getQuantity() : null);

        Map<Long, CommissionRuleDefinitionDO> ruleMap = rules.stream().collect(Collectors.toMap(CommissionRuleDefinitionDO::getId, r -> r));
        List<BrokerageRecordCreateReqDTO> records = calculateResp.getRecords() != null ? calculateResp.getRecords() : List.of();
        Map<Long, List<CommissionMaterialGrantDTO>> materialMap = (calculateResp.getMaterialGrants() != null
            ? calculateResp.getMaterialGrants()
            : List.<CommissionMaterialGrantDTO>of()).stream()
            .filter(item -> item.getRuleId() != null)
            .collect(Collectors.groupingBy(CommissionMaterialGrantDTO::getRuleId));
        List<CommissionSimulationRuleRespVO> detailList = records.stream()
            .map(record -> buildSimulationRule(ruleMap.get(record.getStrategyRuleId()), record))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        detailList.forEach(rule -> rule.setMaterials(buildSimulationMaterials(materialMap.get(rule.getRuleId()))));

        respVO.setRules(detailList);
        respVO.setTotalCommission(detailList.stream().mapToInt(CommissionSimulationRuleRespVO::getAmount).sum());
        return respVO;
    }

    private CommissionSimulationRuleRespVO buildSimulationRule(CommissionRuleDefinitionDO rule, BrokerageRecordCreateReqDTO record) {
        if (rule == null) {
            return null;
        }
        CommissionSimulationRuleRespVO vo = new CommissionSimulationRuleRespVO();
        vo.setRuleId(rule.getId());
        vo.setRuleCode(rule.getRuleCode());
        vo.setDisplayName(rule.getDisplayName());
        vo.setPriority(rule.getPriority());
        vo.setBizCode(record.getBizType());
        CommissionRuleEffectScope scope = rule.getEffectScope();
        if (scope != null) {
            vo.setTargetHierarchy(scope.getTargetHierarchy());
            vo.setTargetLevel(scope.getTargetLevel());
        }
        CommissionRuleActionDO action = matchActionByBizType(rule, record.getBizType());
        if (action != null) {
            vo.setFundBizCode(getPayloadInteger(action, "fundBizCode"));
            vo.setAmountSource(getPayloadString(action, "amountSource"));
            vo.setAmountMode(action.getAmountMode());
            vo.setAmountValue(action.getAmountValue());
            vo.setFundAccount(getPayloadString(action, "fundAccount"));
        }
        vo.setUserId(record.getUserId());
        vo.setAmount(record.getPrice());
        return vo;
    }

    private List<CommissionSimulationRuleMaterialRespVO> buildSimulationMaterials(List<CommissionMaterialGrantDTO> grants) {
        if (grants == null || grants.isEmpty()) {
            return List.of();
        }
        return grants.stream().map(grant -> {
            CommissionSimulationRuleMaterialRespVO vo = new CommissionSimulationRuleMaterialRespVO();
            vo.setMaterialId(grant.getMaterialId());
            vo.setMaterialName(grant.getMaterialName());
            vo.setMaterialCode(grant.getMaterialCode());
            vo.setMaterialImage(grant.getMaterialImage());
            vo.setMaterialUnit(grant.getMaterialUnit());
            vo.setQuantity(grant.getQuantity());
            return vo;
        }).collect(Collectors.toList());
    }

    private CommissionRuleActionDO matchActionByBizType(CommissionRuleDefinitionDO rule, Integer bizType) {
        if (rule == null || CollectionUtils.isEmpty(rule.getActions()) || bizType == null) {
            return null;
        }
        return rule.getActions().stream()
            .filter(Objects::nonNull)
            .filter(action -> {
                Integer incomeBizCode = getPayloadInteger(action, "bizCode");
                Integer fundBizCode = getPayloadInteger(action, "fundBizCode");
                return Objects.equals(incomeBizCode, bizType) || Objects.equals(fundBizCode, bizType);
            })
            .findFirst()
            .orElse(null);
    }

    private Integer getPayloadInteger(CommissionRuleActionDO action, String key) {
        if (action == null || action.getPayloadJson() == null) {
            return null;
        }
        Object value = action.getPayloadJson().get(key);
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

    private String getPayloadString(CommissionRuleActionDO action, String key) {
        if (action == null || action.getPayloadJson() == null) {
            return null;
        }
        Object value = action.getPayloadJson().get(key);
        return value != null ? String.valueOf(value) : null;
    }
}
