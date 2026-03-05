package com.hissp.distribution.module.trade.service.brokerage.bo;

import lombok.Data;

/**
 * 用户佣金统计 BO
 *
 * @author Codex
 */
@Data
public class BrokerageStatisticsRespBO {

    private Integer totalIncome;

    private Integer totalExpense;

    private Integer totalCount;

    private Integer pendingCount;

    private Integer pendingAmount;

    private Integer settledCount;

    private Integer settledAmount;

    public BrokerageStatisticsRespBO() {
        this.totalIncome = 0;
        this.totalExpense = 0;
        this.totalCount = 0;
        this.pendingCount = 0;
        this.pendingAmount = 0;
        this.settledCount = 0;
        this.settledAmount = 0;
    }
}
