package com.hissp.distribution.module.pay.controller.app.settle.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * App 端提现资料保存 VO
 */
@Data
public class AppSettleAccountSaveReqVO {

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String signedName;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "身份证号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "身份证号不能为空")
    private String idCardNo;

    @Schema(description = "身份证有效期起", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "身份证有效期不能为空")
    private String idCardValidStart;

    @Schema(description = "身份证有效期止", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "身份证有效期不能为空")
    private String idCardValidEnd;

    @Schema(description = "身份证正面", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请上传身份证正面")
    private String idCardFrontUrl;

    @Schema(description = "身份证反面", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请上传身份证反面")
    private String idCardBackUrl;

    @Schema(description = "银行卡号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "银行卡号不能为空")
    private String bankAccountNo;

    @Schema(description = "开户人", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "开户人不能为空")
    private String bankAccountName;

    @Schema(description = "开户银行", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "开户银行不能为空")
    private String bankName;

    @Schema(description = "银行卡照片", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String bankCardFrontUrl;

    @Schema(description = "省编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择省份")
    private String provinceCode;

    @Schema(description = "市编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择城市")
    private String cityCode;

    @Schema(description = "区县编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请选择区县")
    private String areaCode;

    @Schema(description = "详细地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请输入详细地址")
    private String address;

    @Schema(description = "合同邮寄地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "请填写合同邮寄地址")
    private String receiverAddress;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "支行名称")
    private String bankBranchName;

    @Schema(description = "备注")
    private String extra;
}
