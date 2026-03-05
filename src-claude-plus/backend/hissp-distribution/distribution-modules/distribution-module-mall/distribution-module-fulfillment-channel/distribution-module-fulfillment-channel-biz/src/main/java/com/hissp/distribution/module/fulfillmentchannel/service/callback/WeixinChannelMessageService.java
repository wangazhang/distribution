package com.hissp.distribution.module.fulfillmentchannel.service.callback;

import cn.binarywang.wx.miniapp.bean.WxMaMessage;

/**
 * 微信渠道消息处理 Service
 */
public interface WeixinChannelMessageService {

    /**
     * 处理微信推送消息
     *
     * @param message 微信推送消息
     */
    void handle(WxMaMessage message);
}
