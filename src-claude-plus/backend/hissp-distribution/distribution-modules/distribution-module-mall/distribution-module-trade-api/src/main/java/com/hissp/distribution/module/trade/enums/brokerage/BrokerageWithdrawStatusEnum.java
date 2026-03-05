package com.hissp.distribution.module.trade.enums.brokerage;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

// TODO 芋艿：提现的打通，在纠结下；
/**
 * 佣金提现状态枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageWithdrawStatusEnum implements ArrayValuable<Integer> {

    AUDITING(0, "审核中"),
    AUDIT_SUCCESS(10, "审核通过"),

    PAYING(12, "余额支付中"),
    ACCT_PAY_SUCCESS(13, "余额支付成功"), // 中间状态，待取现
    WITHDRAWING(14, "提现中"),
    WITHDRAW_SUCCESS(11, "提现成功"),
    WITHDRAW_FINANCE_PAYING(15, "财务打款中"),
    WITHDRAW_SUBMIT_SUCCESS(16, "银行处理中"),

    AUDIT_FAIL(20, "审核不通过"),
    WITHDRAW_FAIL(21, "提现失败"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BrokerageWithdrawStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态
     */
    private final Integer status;
    /**
     * 名字
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
