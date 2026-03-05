package com.hissp.distribution.module.pay.controller.app.settle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * App 端换卡请求 VO
 */
@Data
public class AppSettleAccountChangeCardReqVO {

    @Schema(description = "银行卡号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "银行卡号不能为空")
    private String bankAccountNo;

    @Schema(description = "开户人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "开户人不能为空")
    private String bankAccountName;

    @Schema(description = "开户银行（中文名称）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择开户银行")
    private String bankName;

    @Schema(description = "开户支行")
    private String bankBranchName;

    @Schema(description = "银行卡正面图片", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String bankCardFrontUrl;

    @Schema(description = "省编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择开户省份")
    private String provinceCode;

    @Schema(description = "市编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择开户城市")
    private String cityCode;

    @Schema(description = "区县编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择开户区县")
    private String areaCode;

    @Schema(description = "合同邮寄地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请填写合同邮寄地址")
    private String receiverAddress;
}
