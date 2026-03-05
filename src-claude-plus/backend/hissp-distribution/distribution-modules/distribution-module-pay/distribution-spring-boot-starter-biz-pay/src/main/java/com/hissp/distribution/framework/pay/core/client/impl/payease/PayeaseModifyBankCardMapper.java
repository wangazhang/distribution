package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseModifyBankCardDTO;
import com.upay.sdk.serviceprovider.v3_0.declaration.builder.ModifyBankCardBuilder;
import lombok.experimental.UtilityClass;

/**
 * 首信易换卡参数映射
 */
@UtilityClass
public class PayeaseModifyBankCardMapper {

    public ModifyBankCardBuilder build(PayeaseModifyBankCardDTO dto, String merchantId) {
        ModifyBankCardBuilder builder = new ModifyBankCardBuilder(merchantId);
        builder.setRequestId(dto.getRequestId());
        //if (StrUtil.isNotBlank(dto.getSignedRequestId())) {
        //    builder.setSignedRequestId(dto.getSignedRequestId());
        //}
        builder.setSubMerchantId(dto.getSubMerchantId());
        builder.setBankCardNo(dto.getBankAccountNo());
        builder.setAccountName(dto.getBankAccountName());
        builder.setBankName(dto.getBankName());
        builder.setBankBranchName(dto.getBankBranchName());
        builder.setProvinceCode(dto.getProvinceCode());
        builder.setCityCode(dto.getCityCode());
        return builder;
    }
}
