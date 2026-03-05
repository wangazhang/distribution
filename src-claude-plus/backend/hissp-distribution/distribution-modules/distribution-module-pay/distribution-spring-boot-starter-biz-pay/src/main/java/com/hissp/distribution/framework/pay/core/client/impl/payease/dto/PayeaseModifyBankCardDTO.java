package com.hissp.distribution.framework.pay.core.client.impl.payease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首信易修改结算卡 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayeaseModifyBankCardDTO {

    /** 请求流水号 */
    private String requestId;
    /** 入网请求号（original declare requestId） */
    private String signedRequestId;
    /** 首信易子商户号 */
    private String subMerchantId;

    private String bankAccountNo;
    private String bankAccountName;
    private String bankName;
    private String bankBranchName;
    private String provinceCode;
    private String cityCode;
    private String areaCode;
}
