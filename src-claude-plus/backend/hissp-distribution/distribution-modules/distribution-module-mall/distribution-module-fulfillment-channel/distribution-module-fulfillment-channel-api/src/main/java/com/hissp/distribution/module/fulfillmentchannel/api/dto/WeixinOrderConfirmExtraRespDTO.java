package com.hissp.distribution.module.fulfillmentchannel.api.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信确认收货组件额外参数
 *
 * @author Codex
 */
@Data
public class WeixinOrderConfirmExtraRespDTO implements Serializable {

    private String merchantId;

    private String subMerchantId;

    private String transactionId;

    private String merchantTradeNo;
}
