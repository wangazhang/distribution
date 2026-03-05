package com.hissp.distribution.module.pay.controller.admin.settle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SettleAccountRespVO {

    private Long id;
    private Long userId;
    private Integer status;
    private String rejectReason;
    private String requestId;
    private String subMerchantId;

    private String signedName;
    private String mobile;
    private String email;
    private String idCardNo;
    private LocalDate idCardValidStart;
    private LocalDate idCardValidEnd;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String idCardFrontLocalUrl;
    private String idCardBackLocalUrl;

    private String bankAccountNo;
    private String bankAccountName;
    private String bankName;
    private String bankBranchName;
    private String bankCardFrontUrl;
    private String bankCardFrontLocalUrl;

    private String provinceCode;
    private String cityCode;
    private String areaCode;
    private String address;
    private String receiverAddress;
    private String extra;

    private LocalDateTime createTime;
}
