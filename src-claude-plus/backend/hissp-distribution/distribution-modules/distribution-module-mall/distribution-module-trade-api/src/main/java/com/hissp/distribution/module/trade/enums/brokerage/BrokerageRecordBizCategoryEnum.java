package com.hissp.distribution.module.trade.enums.brokerage;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 佣金记录业务大类枚举，用于快速区分来源业务域。
 */
@Getter
@AllArgsConstructor
public enum BrokerageRecordBizCategoryEnum implements ArrayValuable<Integer> {

    UNKNOWN(0, "其他"),
    MALL_ORDER(1, "商城订单"),
    MB_ORDER(2, "物料订单"),
    ;

    public static final String ATTR_KEY = "bizCategory";

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BrokerageRecordBizCategoryEnum::getType)
            .toArray(Integer[]::new);

    private final Integer type;
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static BrokerageRecordBizCategoryEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }
        for (BrokerageRecordBizCategoryEnum value : values()) {
            if (value.type.equals(type)) {
                return value;
            }
        }
        return null;
    }
}
