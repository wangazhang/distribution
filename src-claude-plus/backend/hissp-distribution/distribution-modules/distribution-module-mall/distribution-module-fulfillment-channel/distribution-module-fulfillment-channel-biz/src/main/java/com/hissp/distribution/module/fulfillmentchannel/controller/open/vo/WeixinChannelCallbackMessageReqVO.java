package com.hissp.distribution.module.fulfillmentchannel.controller.open.vo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class WeixinChannelCallbackMessageReqVO {

    private String signature;

    @NotEmpty(message = "时间戳不能为空")
    private String timestamp;

    @NotEmpty(message = "随机数不能为空")
    private String nonce;

    private String encryptType;

    private String msgSignature;
}
