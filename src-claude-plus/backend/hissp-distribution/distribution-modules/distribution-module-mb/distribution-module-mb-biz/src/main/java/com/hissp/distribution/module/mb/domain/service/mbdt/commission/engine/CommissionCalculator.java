package com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine;

import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionContext;
import java.util.List;

public interface CommissionCalculator {

    CommissionCalculationResult calculate(CommissionContext context,
        CommissionPolicyDO policy, List<CommissionRuleDefinitionDO> rules);
}
