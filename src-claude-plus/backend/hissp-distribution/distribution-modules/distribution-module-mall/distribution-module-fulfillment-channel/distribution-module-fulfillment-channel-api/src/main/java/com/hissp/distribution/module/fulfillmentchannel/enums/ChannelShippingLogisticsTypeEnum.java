package com.hissp.distribution.module.fulfillmentchannel.enums;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 发货物流模式枚举，对应微信发货接口要求
 *
 * @author Codex
 */
@Getter
@AllArgsConstructor
public enum ChannelShippingLogisticsTypeEnum implements ArrayValuable<Integer> {

    EXPRESS(1, "物流快递"),
    CITY_DELIVERY(2, "同城配送"),
    VIRTUAL_GOODS(3, "虚拟商品"),
    SELF_PICKUP(4, "用户自提");

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ChannelShippingLogisticsTypeEnum::getType)
            .toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
