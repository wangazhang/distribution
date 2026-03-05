package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import com.hissp.distribution.framework.common.validation.InEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 佣金提现 Excel 导出 Request VO")
@Data
public class BrokerageWithdrawExportReqVO {

    @Schema(description = "用户编号", example = "11436")
    private Long userId;

    @Schema(description = "提现类型", example = "1")
    @InEnum(value = BrokerageWithdrawTypeEnum.class, message = "提现类型必须是 {value}")
    private Integer type;

    @Schema(description = "真实姓名", example = "赵六")
    private String name;

    @Schema(description = "账号", example = "886779132")
    private String accountNo;

    @Schema(description = "银行名称", example = "招商银行")
    private String bankName;

    @Schema(description = "状态", example = "10")
    @InEnum(value = BrokerageWithdrawStatusEnum.class, message = "状态必须是 {value}")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
