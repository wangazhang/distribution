package com.hissp.distribution.module.member.api.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 用户身份认证信息创建请求 DTO
 *
 * @author 芋道源码
 */
@Data
public class MemberUserIdentityCreateReqDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    /**
     * 身份证号
     */
    @NotBlank(message = "身份证号不能为空")
    private String idCardNo;

    /**
     * 身份证正面照片URL（已上传）
     */
    @NotBlank(message = "身份证正面照片不能为空")
    private String idCardFrontUrl;

    /**
     * 身份证反面照片URL（已上传）
     */
    @NotBlank(message = "身份证反面照片不能为空")
    private String idCardBackUrl;

    /**
     * 身份证有效期
     */
    @NotNull(message = "身份证有效期不能为空")
    private LocalDate idCardExpireDate;

    /**
     * 身份证地址
     */
    @NotBlank(message = "身份证地址不能为空")
    private String idCardAddress;

    /**
     * 认证手机号
     */
    @NotBlank(message = "认证手机号不能为空")
    private String mobile;

}