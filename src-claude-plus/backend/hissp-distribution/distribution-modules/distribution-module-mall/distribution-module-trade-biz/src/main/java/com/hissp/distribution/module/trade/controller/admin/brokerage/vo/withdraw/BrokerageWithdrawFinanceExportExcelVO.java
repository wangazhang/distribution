package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import com.alibaba.excel.annotation.ExcelProperty;
import com.hissp.distribution.framework.excel.core.annotations.DictFormat;
import com.hissp.distribution.framework.excel.core.annotations.ExcelColumnSelect;
import com.hissp.distribution.framework.excel.core.convert.DictConvert;
import com.hissp.distribution.framework.excel.core.convert.MoneyConvert;
import com.hissp.distribution.module.trade.enums.DictTypeConstants;
import com.hissp.distribution.module.trade.framework.excel.BrokerageWithdrawFinanceStatusSelectFunction;
import lombok.Data;

/**
 * 财务打款用 佣金提现 Excel VO
 */
@Data
public class BrokerageWithdrawFinanceExportExcelVO {

    @ExcelProperty("提现编号")
    private Long id;

    @ExcelProperty("用户编号")
    private Long userId;

    @ExcelProperty("用户昵称")
    private String userNickname;

    @ExcelProperty(value = "提现金额", converter = MoneyConvert.class)
    private Integer price;

    @ExcelProperty(value = "手续费", converter = MoneyConvert.class)
    private Integer feePrice;

    @ExcelProperty("真实姓名")
    private String name;

    @ExcelProperty("银行名称")
    private String bankName;

    @ExcelProperty("开户地址")
    private String bankAddress;

    @ExcelProperty("银行卡号/账号")
    private String accountNo;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty(value = "当前状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS)
    private Integer status;

    @ExcelProperty(value = "打款状态（请选择）", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS)
    @ExcelColumnSelect(functionName = BrokerageWithdrawFinanceStatusSelectFunction.NAME)
    private Integer financeStatus;

}
