package com.hissp.distribution.module.pay.api.settle.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 提现实名账户响应 DTO。
 *
 * <p>用于跨模块传输提现资料，仅包含业务方关心的读字段。</p>
 */
@Data
public class PaySettleAccountRespDTO implements Serializable {

    private Long id;
    private Long userId;
    private Integer status;
    private String requestId;
    private String subMerchantId;
    private String rejectReason;

    private String signedName;
    private String mobile;
    private String email;
    private String idCardNo;
    private LocalDate idCardValidStart;
    private LocalDate idCardValidEnd;
    private String idCardFrontUrl;
    private String idCardBackUrl;

    private String bankAccountNo;
    private String bankAccountName;
    private String bankName;
    private String bankBranchName;
    private String bankCardFrontUrl;

    private String provinceCode;
    private String cityCode;
    private String areaCode;
    private String address;
    private String receiverAddress;

    private String extra;
}
