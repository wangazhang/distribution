package com.hissp.distribution.module.pay.api.settle;

import com.hissp.distribution.module.pay.api.settle.dto.PaySettleAccountRespDTO;

/**
 * 提现实名账户 API。
 *
 * <p>面向业务模块暴露最小能力集，避免直接引用 pay-biz 内部 Service / DO。</p>
 */
public interface PaySettleAccountApi {

    /**
     * 查询已通过审核的提现账户
     *
     * @param userId 用户编号
     * @return 已通过账户；若不存在或未通过返回 null
     */
    PaySettleAccountRespDTO getApprovedAccount(Long userId);
}
