package com.hissp.distribution.framework.pay.core.enums.order;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * 渠道的妥投状态枚举
 *
 * <p>参考首信易妥投回调定义，同时预留通道扩展能力。</p>
 */
@Getter
@AllArgsConstructor
public enum PayOrderDeliveryStatusRespEnum {

    PENDING("PENDING", "待妥投"),
    DELIVERED("DELIVERED", "已妥投"),
    NO_REQUIRED("NOREQUIRED", "无需妥投"),
    CANCELED("CANCELED", "妥投取消"),
    ;

    private final String code;
    private final String name;

    public static PayOrderDeliveryStatusRespEnum from(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toUpperCase();
        for (PayOrderDeliveryStatusRespEnum value : values()) {
            if (Objects.equals(value.code, normalized)) {
                return value;
            }
        }
        return null;
    }

    public static boolean isDelivered(PayOrderDeliveryStatusRespEnum status) {
        return status == DELIVERED || status == NO_REQUIRED;
    }

    public static boolean isPending(PayOrderDeliveryStatusRespEnum status) {
        return status == PENDING;
    }

    public static boolean isCanceled(PayOrderDeliveryStatusRespEnum status) {
        return status == CANCELED;
    }
}
