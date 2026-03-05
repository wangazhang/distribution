package com.hissp.distribution.module.trade.api.config.dto;

import lombok.Data;

/**
 * 交易配置对外传输对象。
 */
@Data
public class TradeConfigRespDTO {

    /**
     * 佣金默认冻结天数
     */
    private Integer brokerageFrozenDays;

}
