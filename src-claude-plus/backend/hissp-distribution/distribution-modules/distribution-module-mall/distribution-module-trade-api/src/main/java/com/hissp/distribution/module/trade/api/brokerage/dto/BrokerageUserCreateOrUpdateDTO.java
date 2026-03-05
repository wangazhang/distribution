package com.hissp.distribution.module.trade.api.brokerage.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrokerageUserCreateOrUpdateDTO {

    /**
     * 用户编号
     * <p>
     * 对应 MemberUserDO 的 id 字段
     */
    private Long id;

    /**
     * 推广员编号
     * <p>
     * 关联 MemberUserDO 的 id 字段
     */
    private Long bindUserId;
    /**
     * 推广员绑定时间
     */
    private LocalDateTime bindUserTime;

    /**
     * 是否有分销资格
     */
    private Boolean brokerageEnabled;
    /**
     * 成为分销员时间
     */
    private LocalDateTime brokerageTime;

    /**
     * 可用佣金
     */
    private Integer brokeragePrice;
    /**
     * 冻结佣金
     */
    private Integer frozenPrice;

    /**
     * 累计佣金
     */
    private Integer totalBrokeragePrice;

    /**
     * 会员等级编号
     * <p>
     * 关联 MemberLevel 的 id 字段
     */
    private Long levelId;
}
