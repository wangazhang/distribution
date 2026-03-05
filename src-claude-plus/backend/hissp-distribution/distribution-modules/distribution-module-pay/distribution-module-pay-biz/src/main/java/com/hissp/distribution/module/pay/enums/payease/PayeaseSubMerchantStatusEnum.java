package com.hissp.distribution.module.pay.enums.payease;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 首信易子商户入网状态
 *
 * <p>约定：0 草稿、10 审核中、20 已通过、30 已驳回。</p>
 */
@Getter
@AllArgsConstructor
public enum PayeaseSubMerchantStatusEnum implements ArrayValuable<Integer> {

    DRAFT(0, "草稿"),
    AUDITING(10, "审核中"),
    PASS(20, "审核通过"),
    REJECT(30, "审核驳回");

    private final Integer status;
    private final String name;

    private static final Integer[] ARRAYS =
            Arrays.stream(values()).map(PayeaseSubMerchantStatusEnum::getStatus).toArray(Integer[]::new);

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
