package com.hissp.distribution.module.mb.api.commission;

import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateReqDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateRespDTO;

/**
 * 分佣策略对外能力
 */
public interface CommissionStrategyApi {

    /**
     * 计算分佣流水
     *
     * @param reqDTO 请求参数
     * @return 计算结果
     */
    CommissionCalculateRespDTO calculateCommission(CommissionCalculateReqDTO reqDTO);
}
