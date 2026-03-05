package com.hissp.distribution.module.fulfillmentchannel.enums;

import com.hissp.distribution.framework.common.exception.ErrorCode;

/**
 * 履约渠道错误码定义
 *
 * <p>区间：1-014-000-000 ~ 1-014-999-999</p>
 *
 * @author Codex
 */
public interface ErrorCodeConstants {

    ErrorCode WEIXIN_CHANNEL_MESSAGE_TOKEN_MISSING = new ErrorCode(1_014_000_000,
            "微信消息推送未配置 Token 或 EncodingAESKey");
    ErrorCode WEIXIN_CHANNEL_MESSAGE_DECRYPT_FAIL = new ErrorCode(1_014_000_001,
            "解密微信消息失败");
    ErrorCode WEIXIN_CHANNEL_MESSAGE_HANDLE_ERROR = new ErrorCode(1_014_000_002,
            "处理微信渠道消息失败");

}
