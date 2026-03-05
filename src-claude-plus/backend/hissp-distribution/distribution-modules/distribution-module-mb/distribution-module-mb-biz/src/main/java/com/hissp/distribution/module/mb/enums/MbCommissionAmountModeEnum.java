package com.hissp.distribution.module.mb.enums;

import java.util.Arrays;

public enum MbCommissionAmountModeEnum {
    PERCENTAGE,
    FIXED;

    public static MbCommissionAmountModeEnum of(String value) {
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}
