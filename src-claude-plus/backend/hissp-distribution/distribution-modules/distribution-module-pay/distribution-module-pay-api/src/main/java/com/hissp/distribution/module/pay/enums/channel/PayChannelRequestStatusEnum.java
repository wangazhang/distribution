package com.hissp.distribution.module.pay.enums.channel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道请求状态枚举
 *
 * @author Cascade
 */
@Getter
@AllArgsConstructor
public enum PayChannelRequestStatusEnum {

    PROCESSING(0, "处理中"),
    SUCCESS(1, "成功"),
    FAILURE(2, "失败");

    private final Integer status;
    private final String description;

}

