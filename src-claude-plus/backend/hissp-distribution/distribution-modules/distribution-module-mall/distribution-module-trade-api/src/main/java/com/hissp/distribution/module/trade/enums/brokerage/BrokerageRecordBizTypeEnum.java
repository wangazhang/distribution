package com.hissp.distribution.module.trade.enums.brokerage;

import com.hissp.distribution.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 佣金记录业务类型枚举
 *
 * @author owen
 */
@AllArgsConstructor
@Getter
public enum BrokerageRecordBizTypeEnum implements ArrayValuable<Integer> {

    ORDER_ADD(1001, "（普）获得推广佣金", "（普）获得推广佣金 {}", true),
    // 成员补货商品收入：补货业务直供佣金
    MB_MEMBER_RESTOCK_INCOME(3001, "成员补货商品收入", "成员补货商品收入 {}", true),
    MB_DIRECT_SUPPLY(3002, "物料直供佣金", "物料直供佣金 {}", true),
    // 平台代付物料直供成本
    MB_PLATFORM_DIRECT_FUND(4201, "【出资】（美）商品", "【出资】（美）商品 {}", false),
    // 总部兜底出资：成员补货时总部垫付
    MB_HQ_RESTOCK_FUND(4104, "补货出资", "总部补货出资 {}", false),
    WITHDRAW(9999, "提现申请", "提现申请扣除佣金 {}", false),
    WITHDRAW_REJECT(9998, "提现申请驳回", "提现申请驳回，返还佣金 {}", true),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(BrokerageRecordBizTypeEnum::getType).toArray(Integer[]::new);

    /**
     * 类型
     */
    private final Integer type;
    /**
     * 标题
     */
    private final String title;
    /**
     * 描述
     */
    private final String description;
    /**
     * 是否为增加佣金
     */
    private final boolean add;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

    // 新增查找方法
    public static BrokerageRecordBizTypeEnum fromType(Integer type) {
        if (type == null) {
            return null;
        }
        for (BrokerageRecordBizTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
