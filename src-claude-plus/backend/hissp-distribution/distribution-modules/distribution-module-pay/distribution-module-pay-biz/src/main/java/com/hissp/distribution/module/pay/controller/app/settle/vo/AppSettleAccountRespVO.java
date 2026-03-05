package com.hissp.distribution.module.pay.controller.app.settle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AppSettleAccountRespVO {

    @Schema(description = "状态 0草稿 10审核中 20通过 30驳回")
    private Integer status;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "渠道子商户号")
    private String subMerchantId;

    private String signedName;
    private String mobile;
    private String idCardNo;
    private String idCardValidStart;
    private String idCardValidEnd;
    private String bankAccountNo;
    private String bankAccountName;
    private String bankName;
    private String bankBranchName;
    private String provinceCode;
    private String cityCode;
    private String areaCode;
    private String address;
    private String receiverAddress;
    private String email;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String bankCardFrontUrl;
    private String extra;
}
