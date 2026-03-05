package com.hissp.distribution.framework.pay.core.enums.transfer;

import cn.hutool.core.util.ArrayUtil;
import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 支撑支付核心模块的转账类型枚举
 * <p>
 * 注意：该枚举与 pay-api 中的 PayTransferTypeEnum 含义一致，但处于更底层，
 * 不能依赖上层模块，因此需要单独维护。
 */
@AllArgsConstructor
@Getter
public enum PayTransferTypeEnum implements ArrayValuable<Integer> {

    ALIPAY_BALANCE(1, "支付宝余额"),
    WX_BALANCE(2, "微信余额"),
    BANK_CARD(3, "银行卡"),
    WALLET_BALANCE(4, "钱包余额"),
    PAYEASE_ACCOUNT(5, "首信易账户");

    public interface WxPay {
    }

    public interface Alipay {
    }

    public interface PayeaseAccount {
    }

    private final Integer type;
    private final String name;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PayTransferTypeEnum::getType).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    public static PayTransferTypeEnum typeOf(Integer type) {
        return ArrayUtil.firstMatch(item -> item.getType().equals(type), values());
    }
}
