package com.hissp.distribution.module.pay.enums.account;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户银行卡入网状态
 *
 * @author codex
 */
@Getter
@AllArgsConstructor
public enum PayBankcardNetworkStatusEnum {

    NOT_APPLY(0, "未申请"),
    APPLYING(1, "申请中"),
    SUCCESS(2, "入网成功"),
    FAIL(3, "入网失败");

    private final Integer status;
    private final String desc;
}
