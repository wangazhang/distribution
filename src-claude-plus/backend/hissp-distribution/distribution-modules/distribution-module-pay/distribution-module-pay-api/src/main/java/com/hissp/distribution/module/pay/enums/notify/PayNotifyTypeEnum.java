package com.hissp.distribution.module.pay.enums.notify;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付通知类型
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayNotifyTypeEnum {

    ORDER(1, "支付单"),
    REFUND(2, "退款单"),
    TRANSFER(3, "转账单"),
    ACCOUNT_OPEN(4, "开户"),
    CARD_BIND(5, "绑卡");

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 名字
     */
    private final String name;

}
