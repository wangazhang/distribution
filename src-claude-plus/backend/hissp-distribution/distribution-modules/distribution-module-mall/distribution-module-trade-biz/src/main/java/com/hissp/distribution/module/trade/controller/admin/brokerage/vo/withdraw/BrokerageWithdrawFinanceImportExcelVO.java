package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import com.alibaba.excel.annotation.ExcelProperty;
import com.hissp.distribution.framework.excel.core.annotations.DictFormat;
import com.hissp.distribution.framework.excel.core.convert.DictConvert;
import com.hissp.distribution.module.trade.enums.DictTypeConstants;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 财务打款结果导入 Excel VO
 */
@Data
@Accessors(chain = false) // 设置 chain = false，避免用户导入有问题
public class BrokerageWithdrawFinanceImportExcelVO {

    @ExcelProperty("提现编号")
    private Long id;

    @ExcelProperty(value = "打款状态（请选择）", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS)
    private Integer financeStatus;

    @ExcelProperty("备注")
    private String remark;

}
