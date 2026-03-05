package com.hissp.distribution.module.mb.enums;

import com.hissp.distribution.module.mb.domain.service.mbdt.commission.model.CommissionContext;

import java.math.BigDecimal;
import java.util.Arrays;

public enum MbCommissionAmountSourceEnum {
    ORDER_AMOUNT {
        @Override
        public BigDecimal extractBase(CommissionContext context) {
            Integer totalPrice = context.getOrderTotalPrice();
            return totalPrice != null ? BigDecimal.valueOf(totalPrice) : null;
        }
    },
    UNIT_PRICE {
        @Override
        public BigDecimal extractBase(CommissionContext context) {
            Integer unitPrice = context.getOrderUnitPrice();
            return unitPrice != null ? BigDecimal.valueOf(unitPrice) : null;
        }
    },
    QUANTITY {
        @Override
        public BigDecimal extractBase(CommissionContext context) {
            Integer quantity = context.getOrderQuantity();
            return quantity != null ? BigDecimal.valueOf(quantity) : null;
        }
    };

    public abstract BigDecimal extractBase(CommissionContext context);

    public static MbCommissionAmountSourceEnum of(String value) {
        return Arrays.stream(values())
            .filter(item -> item.name().equalsIgnoreCase(value))
            .findFirst()
            .orElse(null);
    }
}
