package com.hissp.distribution.module.pay.controller.admin.settle.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettleAccountUpdateReqVO {

    @NotNull(message = "编号不能为空")
    private Long id;

    private String mobile;
    private String email;
    private String bankAccountNo;
    private String bankBranchName;
    private String bankCardFrontUrl;
    private String extra;
}
