package com.hissp.distribution.module.pay.dal.dataobject.account;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.time.LocalDate;

/**
 * 提现实名账户 DO（首信易）
 */
@TableName(value = "pay_settle_account", autoResultMap = true)
@KeySequence("pay_settle_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaySettleAccountDO extends BaseDO {

    private Long id;
    private Long userId;
    private Integer userType;
    /**
     * 状态：0 草稿 10 审核中 20 通过 30 驳回
     */
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
    /**
     * 区县编码
     */
    private String areaCode;
    private String address;
    /**
     * 合同邮寄地址
     */
    private String receiverAddress;

    private String extra;
}
