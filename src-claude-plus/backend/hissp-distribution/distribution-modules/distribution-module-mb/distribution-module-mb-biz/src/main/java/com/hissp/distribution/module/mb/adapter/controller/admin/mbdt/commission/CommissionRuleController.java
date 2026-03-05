package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleEffectScopeVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleEffectScopeVO.InviteOrderScopeVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleMaterialRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleSaveReqVO;
import com.hissp.distribution.module.mb.convert.commission.CommissionRuleConvert;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleEffectScope;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleMaterialDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 分佣规则配置")
@RestController
@RequestMapping("/mb/commission-rule")
@Validated
public class CommissionRuleController {

    @Resource
    private CommissionRuleService commissionRuleService;

    @Resource
    private CommissionRuleConvert commissionRuleConvert;

    @PostMapping("/create")
    @Operation(summary = "创建分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:create')")
    public CommonResult<Long> createRule(@Valid @RequestBody CommissionRuleSaveReqVO createReqVO) {
        return success(commissionRuleService.createRule(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:update')")
    public CommonResult<Boolean> updateRule(@Valid @RequestBody CommissionRuleSaveReqVO updateReqVO) {
        commissionRuleService.updateRule(updateReqVO);
        return success(Boolean.TRUE);
    }

    @PostMapping("/enable")
    @Operation(summary = "启用分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:enable')")
    public CommonResult<Boolean> enableRule(@RequestParam("id") Long id) {
        commissionRuleService.enableRule(id);
        return success(Boolean.TRUE);
    }

    @PostMapping("/disable")
    @Operation(summary = "停用分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:disable')")
    public CommonResult<Boolean> disableRule(@RequestParam("id") Long id) {
        commissionRuleService.disableRule(id);
        return success(Boolean.TRUE);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:delete')")
    public CommonResult<Boolean> deleteRule(@RequestParam("id") Long id) {
        commissionRuleService.deleteRule(id);
        return success(Boolean.TRUE);
    }

    @PostMapping("/clone")
    @Operation(summary = "克隆分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:clone')")
    public CommonResult<Long> cloneRule(@RequestParam("id") Long id) {
        return success(commissionRuleService.cloneRule(id));
    }

    @GetMapping("/list")
    @Operation(summary = "查询策略下的全部分佣规则")
    @PreAuthorize("@ss.hasPermission('mb:commission-rule:query')")
    public CommonResult<List<CommissionRuleRespVO>> getRuleList(@RequestParam("policyId") Long policyId) {
        List<CommissionRuleDefinitionDO> list = commissionRuleService.getRuleListByPolicy(policyId);
        List<CommissionRuleRespVO> respList = list.stream()
            .filter(Objects::nonNull)
            .map(rule -> {
                CommissionRuleRespVO vo = commissionRuleConvert.convert(rule);
                vo.setEffectScope(convertEffectScope(rule.getEffectScope()));
                vo.setConditions(commissionRuleConvert.convertConditions(rule.safeConditions()));
                vo.setActions(commissionRuleConvert.convertActions(rule.safeActions()));
                vo.setMaterials(buildMaterialRespList(rule.safeMaterials()));
                return vo;
            })
            .collect(Collectors.toList());
        return success(respList);
    }

    private CommissionRuleEffectScopeVO convertEffectScope(CommissionRuleEffectScope scope) {
        if (scope == null) {
            return null;
        }
        CommissionRuleEffectScopeVO vo = new CommissionRuleEffectScopeVO();
        vo.setTargetHierarchy(scope.getTargetHierarchy());
        vo.setTargetLevel(scope.getTargetLevel());
        vo.setAmountSource(scope.getAmountSource());
        vo.setFundAccount(scope.getFundAccount());
        if (scope.getInviteOrder() != null) {
            InviteOrderScopeVO inviteVO = new InviteOrderScopeVO();
            inviteVO.setOperator(scope.getInviteOrder().getOperator());
            inviteVO.setMinIndex(scope.getInviteOrder().getMinIndex());
            inviteVO.setMaxIndex(scope.getInviteOrder().getMaxIndex());
            inviteVO.setIndexes(scope.getInviteOrder().getIndexes());
            vo.setInviteOrder(inviteVO);
        }
        return vo;
    }

    private List<CommissionRuleMaterialRespVO> buildMaterialRespList(List<CommissionRuleMaterialDO> materials) {
        if (materials == null || materials.isEmpty()) {
            return List.of();
        }
        return materials.stream().map(item -> {
            CommissionRuleMaterialRespVO vo = new CommissionRuleMaterialRespVO();
            vo.setId(item.getId());
            vo.setMaterialId(item.getMaterialId());
            vo.setMaterialName(item.getMaterialName());
            vo.setMaterialCode(item.getMaterialCode());
            vo.setMaterialImage(item.getMaterialImage());
            vo.setMaterialUnit(item.getMaterialUnit());
            vo.setQuantity(item.getQuantity());
            return vo;
        }).collect(Collectors.toList());
    }
}
