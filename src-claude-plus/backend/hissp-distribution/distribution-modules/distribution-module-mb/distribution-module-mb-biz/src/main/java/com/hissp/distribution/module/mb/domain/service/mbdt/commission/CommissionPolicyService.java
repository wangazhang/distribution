package com.hissp.distribution.module.mb.domain.service.mbdt.commission;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicyPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicySaveReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionPolicyMatchContext;

import jakarta.validation.Valid;
import java.util.List;

public interface CommissionPolicyService {

    Long createPolicy(@Valid CommissionPolicySaveReqVO reqVO);

    void updatePolicy(@Valid CommissionPolicySaveReqVO reqVO);

    void publishPolicy(Long id);

    void offlinePolicy(Long id);

    Long clonePolicy(Long id);

    CommissionPolicyDO getPolicy(Long id);

    CommissionPolicyDO getPolicyByCode(String policyCode);

    CommissionPolicyDO matchOnlinePolicy(@Valid CommissionPolicyMatchContext context);

    List<CommissionPolicyDO> getOnlinePolicyList();

    PageResult<CommissionPolicyDO> getPolicyPage(CommissionPolicyPageReqVO reqVO);
}
