package com.hissp.distribution.module.trade.controller.app.order.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信确认收货组件所需的额外字段
 *
 * @author Codex
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "用户 App - 微信确认收货额外参数")
public class AppTradeOrderWeixinExtraRespVO {

    @Schema(description = "微信支付商户号", example = "1900006511")
    @JsonProperty("merchant_id")
    private String merchantId;

    @Schema(description = "微信支付子商户号", example = "1900006512")
    @JsonProperty("sub_merchant_id")
    private String subMerchantId;

    @Schema(description = "微信侧订单号，对应微信支付 transaction_id", example = "4200001363202202140034529010")
    @JsonProperty("transaction_id")
    private String transactionId;

    @Schema(description = "商户侧订单号，需与上传发货信息的编号一致", example = "T202403181234567890")
    @JsonProperty("merchant_trade_no")
    private String merchantTradeNo;

}
