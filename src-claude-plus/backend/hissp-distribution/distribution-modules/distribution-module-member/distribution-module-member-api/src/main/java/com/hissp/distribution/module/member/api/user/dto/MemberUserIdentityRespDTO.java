package com.hissp.distribution.module.member.api.user.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户身份认证信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class MemberUserIdentityRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 用户ID，关联member_user.id
     */
    private Long userId;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 身份证号
     */
    private String idCardNo;

    /**
     * 身份证正面照片URL
     */
    private String idCardFrontUrl;

    /**
     * 身份证反面照片URL
     */
    private String idCardBackUrl;

    /**
     * 身份证有效期
     */
    private LocalDate idCardExpireDate;
    private LocalDate idEffectiveDateStart;
    private LocalDate idEffectiveDateEnd;
    private Integer idEffectiveLongTerm;

    /**
     * 身份证地址
     */
    private String idCardAddress;
    /** 身份证省/市/区编码 */
    private String idCardProvinceCode;
    private String idCardCityCode;
    private String idCardDistrictCode;

    /**
     * 认证手机号
     */
    private String mobile;

    /**
     * 认证状态：0-待审核，1-审核通过，2-审核拒绝
     */
    private Integer verifyStatus;

    /**
     * 认证时间
     */
    private LocalDateTime verifyTime;

    /**
     * 审核备注
     */
    private String verifyRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
