package com.hissp.distribution.module.mb.domain.service.mbdt.commission;

import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleSaveReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;

import jakarta.validation.Valid;
import java.util.List;

public interface CommissionRuleService {

    Long createRule(@Valid CommissionRuleSaveReqVO reqVO);

    void updateRule(@Valid CommissionRuleSaveReqVO reqVO);

    void enableRule(Long id);

    void disableRule(Long id);

    void deleteRule(Long id);

    List<CommissionRuleDefinitionDO> getActiveRulesByPolicy(Long policyId);

    List<CommissionRuleDefinitionDO> getRuleListByPolicy(Long policyId);

    Long cloneRule(Long id);
}
