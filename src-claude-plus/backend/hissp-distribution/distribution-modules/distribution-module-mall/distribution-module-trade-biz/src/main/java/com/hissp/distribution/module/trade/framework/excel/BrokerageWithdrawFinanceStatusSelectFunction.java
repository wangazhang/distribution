package com.hissp.distribution.module.trade.framework.excel;

import com.hissp.distribution.framework.dict.core.DictFrameworkUtils;
import com.hissp.distribution.framework.excel.core.function.ExcelColumnSelectFunction;
import com.hissp.distribution.module.trade.enums.DictTypeConstants;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 佣金提现财务状态下拉（仅保留财务打款中、银行处理中）
 */
@Component
public class BrokerageWithdrawFinanceStatusSelectFunction implements ExcelColumnSelectFunction {

    public static final String NAME = "getBrokerageFinanceStatusOptions";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getOptions() {
        return Arrays.asList(
                DictFrameworkUtils.getDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS,
                        BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus()),
                DictFrameworkUtils.getDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS,
                        BrokerageWithdrawStatusEnum.WITHDRAW_SUBMIT_SUCCESS.getStatus())
        );
    }
}
