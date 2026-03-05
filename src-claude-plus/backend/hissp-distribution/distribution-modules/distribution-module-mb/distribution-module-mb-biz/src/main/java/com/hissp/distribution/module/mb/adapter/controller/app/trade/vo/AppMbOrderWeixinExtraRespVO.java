package com.hissp.distribution.module.mb.adapter.controller.app.trade.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * App - MB订单微信确认收货额外参数
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "App - MB订单微信确认收货额外参数")
public class AppMbOrderWeixinExtraRespVO {

    @Schema(description = "微信支付商户号", example = "1900000109")
    @JsonProperty("merchant_id")
    private String merchantId;

    @Schema(description = "微信支付子商户号", example = "1900000109")
    @JsonProperty("sub_merchant_id")
    private String subMerchantId;

    @Schema(description = "微信支付单号", example = "4200000306202301011234567890")
    @JsonProperty("transaction_id")
    private String transactionId;

    @Schema(description = "商户侧订单号", example = "MR123456789")
    @JsonProperty("merchant_trade_no")
    private String merchantTradeNo;

}
