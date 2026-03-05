package com.hissp.distribution.module.mb.api.commission.dto;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Data;

/**
 * 分佣计算结果 DTO
 */
@Data
public class CommissionCalculateRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long policyId;

    private String policyCode;

    private String policyDisplayName;

    private List<BrokerageRecordCreateReqDTO> records = Collections.emptyList();

    private List<CommissionMaterialGrantDTO> materialGrants = Collections.emptyList();

    private boolean matched;

    public static CommissionCalculateRespDTO empty() {
        CommissionCalculateRespDTO resp = new CommissionCalculateRespDTO();
        resp.setMatched(false);
        resp.setRecords(Collections.emptyList());
        resp.setMaterialGrants(Collections.emptyList());
        return resp;
    }
}
