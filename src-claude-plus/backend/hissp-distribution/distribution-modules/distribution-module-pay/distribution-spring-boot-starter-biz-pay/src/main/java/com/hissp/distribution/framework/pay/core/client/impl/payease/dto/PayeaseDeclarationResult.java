package com.hissp.distribution.framework.pay.core.client.impl.payease.dto;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首信易入网申报/查询结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayeaseDeclarationResult {

    /** 是否申报成功（SUCCESS） */
    private boolean success;
    /** 原始状态：SUCCESS / FAILURE / PROCESSING */
    private String status;
    /** 首信易返回的错误码 */
    private String errorCode;
    /** 错误信息 */
    private String errorMessage;
    /** 入网请求号 */
    private String requestId;
    /** 子商户号 */
    private String subMerchantId;
    /** 子商户审核状态（PASS/NO_PASS/ESIGN_PROCESSING/…） */
    private String reviewStatus;
    /** 审核备注 */
    private String reviewRemark;
    /** 核查状态（SUCCESS/FAIL/COMPLEMENT） */
    private String postReviewStatus;
    /** 核查备注 */
    private String postReviewRemark;
    /** 电子签约链接（如需要继续签约） */
    private String electronicContractingUrl;
    /** 资质补充链接 */
    private String certificateSupplementUrl;
    /** 原始返回报文，便于排查 */
    private JSONObject raw;

    public static PayeaseDeclarationResult pending(JSONObject raw, String requestId) {
        return PayeaseDeclarationResult.builder()
                .success(false)
                .status("PROCESSING")
                .requestId(requestId)
                .raw(raw)
                .build();
    }
}
