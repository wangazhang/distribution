package com.hissp.distribution.module.trade.service.brokerage.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 财务打款结果导入返回 BO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrokerageWithdrawFinanceImportRespBO {

    private Integer successCount;

    private Integer failureCount;

    @Builder.Default
    private List<String> failureReasons = new ArrayList<>();

    public void addFailure(String reason) {
        this.failureCount = this.failureCount == null ? 1 : this.failureCount + 1;
        this.failureReasons.add(reason);
    }

    public void incrementSuccess() {
        this.successCount = this.successCount == null ? 1 : this.successCount + 1;
    }

}
