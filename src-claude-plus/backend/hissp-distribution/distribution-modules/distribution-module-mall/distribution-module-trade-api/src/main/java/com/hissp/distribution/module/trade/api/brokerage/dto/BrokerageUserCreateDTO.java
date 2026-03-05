package com.hissp.distribution.module.trade.api.brokerage.dto;

import lombok.Data;

@Data
public class BrokerageUserCreateDTO {

    private Long userId;

    private Long bindUserId;
    /**
     * 是否有分销资格
     */
    private Boolean brokerageEnabled;
}
