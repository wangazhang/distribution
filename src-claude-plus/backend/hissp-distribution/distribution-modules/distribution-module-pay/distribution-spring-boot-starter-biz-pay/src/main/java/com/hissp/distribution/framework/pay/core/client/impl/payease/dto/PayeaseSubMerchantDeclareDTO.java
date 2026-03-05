package com.hissp.distribution.framework.pay.core.client.impl.payease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 首信易子商户入网申报 DTO
 *
 * <p>字段命名尽量贴近官方文档，绝大部分字段都是非必填项，默认值由上层业务决定。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayeaseSubMerchantDeclareDTO {

    /** 报单流水号（requestId） */
    private String requestId;
    /** 操作类型：CREATE / MODIFY */
    private String operationType;
    /** 回调地址，可选 */
    private String notifyUrl;
    /** 修改场景需要传递的首信易子商户号 */
    private String subMerchantId;

    /** 基础信息 */
    private String signedType;
    private String registerRole;
    private String merchantType;
    private String cerType;
    private String signedName;
    private String signedShortName;
    private String mobile;
    private String email;
    private String provinceCode;
    private String cityCode;
    private String areaCode;
    private String address;
    private String businessClassification;
    private String belongAgentNumber;
    private String productPackage;
    private String desireAuth;

    /** 证件信息 */
    private String idCardNo;
    private String idCardValidStart;
    private String idCardValidEnd;
    private String idCardFrontPath;
    private String idCardBackPath;
    private String bankCardFrontPath;
    private String legalPersonPhone;

    /** 银行信息 */
    private String bankAccountNo;
    private String bankAccountName;
    private String bankName;
    private String bankBranchName;
    private String bankReservedPhone;
    private String accountType;
    private String liquidationType;
    private String withdrawRateType;
    private String withdrawRate;
    private String reserveDays;

    /** 合同邮寄信息 */
    private String contractReceiverName;
    private String contractReceiverPhone;
    private String contractReceiverAddress;
    private String contractType;

    /** 额外可扩展参数 */
    private Map<String, String> extendedParameters;
    /** 附加说明 */
    private String extra;
}
