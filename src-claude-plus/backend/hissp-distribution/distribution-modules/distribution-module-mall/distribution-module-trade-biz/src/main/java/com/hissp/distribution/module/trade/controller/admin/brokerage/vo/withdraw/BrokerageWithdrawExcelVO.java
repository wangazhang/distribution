package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import com.alibaba.excel.annotation.ExcelProperty;
import com.hissp.distribution.framework.excel.core.annotations.DictFormat;
import com.hissp.distribution.framework.excel.core.convert.DictConvert;
import com.hissp.distribution.framework.excel.core.convert.MoneyConvert;
import com.hissp.distribution.module.trade.enums.DictTypeConstants;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 佣金提现 Excel VO
 *
 * @author 
 */
@Data
public class BrokerageWithdrawExcelVO {

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

    @ExcelProperty(value = "当前总佣金", converter = MoneyConvert.class)
    private Integer totalPrice;

    @ExcelProperty(value = "提现方式", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BROKERAGE_WITHDRAW_TYPE)
    private Integer type;

    @ExcelProperty("真实姓名")
    private String name;

    @ExcelProperty("账号")
    private String accountNo;

    @ExcelProperty("银行名称")
    private String bankName;

    @ExcelProperty("开户地址")
    private String bankAddress;

    @ExcelProperty("收款码地址")
    private String accountQrCodeUrl;

    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS)
    private Integer status;

    @ExcelProperty("审核说明")
    private String auditReason;

    @ExcelProperty("审核时间")
    private LocalDateTime auditTime;

    @ExcelProperty("备注")
    private String remark;

    @ExcelProperty("申请时间")
    private LocalDateTime createTime;

}
