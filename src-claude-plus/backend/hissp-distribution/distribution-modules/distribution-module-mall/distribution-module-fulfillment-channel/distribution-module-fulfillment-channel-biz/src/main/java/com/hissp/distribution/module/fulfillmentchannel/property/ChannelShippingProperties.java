package com.hissp.distribution.module.fulfillmentchannel.property;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "mall.channel-shipping")
public class ChannelShippingProperties {

    private Weixin weixin = new Weixin();

    @Data
    public static class Weixin {
        private Boolean enabled = Boolean.TRUE;
        private String appId;
        private String mchId;
        private String subMchId;
        /**
         * 微信消息推送 Token，用于校验签名
         */
        private String messageToken;
        /**
         * 微信消息推送 EncodingAESKey，可选
         */
        private String messageAesKey;
    }
}
