package com.hissp.distribution.module.fulfillmentchannel.enums;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 渠道发货枚举
 *
 * @author Codex
 */
@Getter
@AllArgsConstructor
public enum ChannelShippingChannelEnum implements ArrayValuable<Integer> {

    WECHAT_MINIAPP(10, "微信小程序");

    private final Integer code;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ChannelShippingChannelEnum::getCode)
            .toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
