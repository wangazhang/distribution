package com.hissp.distribution.module.mb.domain.service.mbdt.commission.engine;

import com.hissp.distribution.module.mb.api.commission.dto.CommissionMaterialGrantDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 分佣计算结果聚合
 */
@Data
public class CommissionCalculationResult {

    private List<BrokerageRecordCreateReqDTO> brokerageRecords = new ArrayList<>();

    private List<CommissionMaterialGrantDTO> materialGrants = new ArrayList<>();

    public static CommissionCalculationResult empty() {
        CommissionCalculationResult result = new CommissionCalculationResult();
        result.setBrokerageRecords(Collections.emptyList());
        result.setMaterialGrants(Collections.emptyList());
        return result;
    }
}
