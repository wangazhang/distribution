package com.hissp.distribution.module.pay.api.settle;

import com.hissp.distribution.module.pay.api.settle.dto.PaySettleAccountRespDTO;
import com.hissp.distribution.module.pay.convert.settle.PaySettleAccountApiConvert;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import com.hissp.distribution.module.pay.service.settle.PaySettleAccountService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * PaySettleAccount API 实现。
 *
 * <p>内部依赖 Service，外部仅通过 DTO 交互。</p>
 */
@Service
public class PaySettleAccountApiImpl implements PaySettleAccountApi {

    @Resource
    private PaySettleAccountService paySettleAccountService;

    @Override
    public PaySettleAccountRespDTO getApprovedAccount(Long userId) {
        PaySettleAccountDO account = paySettleAccountService.getApprovedAccount(userId);
        if (account == null) {
            return null;
        }
        return PaySettleAccountApiConvert.INSTANCE.convert(account);
    }
}
