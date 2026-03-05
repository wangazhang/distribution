package com.hissp.distribution.module.product.enums.spu;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 商品类型枚举
 *
 * <p>用于区分实物商品与虚拟商品，影响后续履约与微信发货模式。</p>
 *
 * @author Codex
 */
@Getter
@AllArgsConstructor
public enum ProductSpuTypeEnum implements ArrayValuable<Integer> {

    PHYSICAL(1, "实物商品"),
    VIRTUAL(2, "虚拟商品");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(ProductSpuTypeEnum::getType)
            .toArray(Integer[]::new);

    /**
     * 类型值
     */
    private final Integer type;
    /**
     * 类型名称
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static boolean isPhysical(Integer type) {
        return PHYSICAL.type.equals(type);
    }

    public static boolean isVirtual(Integer type) {
        return VIRTUAL.type.equals(type);
    }

    public static Integer getDefault() {
        return PHYSICAL.getType();
    }
}
