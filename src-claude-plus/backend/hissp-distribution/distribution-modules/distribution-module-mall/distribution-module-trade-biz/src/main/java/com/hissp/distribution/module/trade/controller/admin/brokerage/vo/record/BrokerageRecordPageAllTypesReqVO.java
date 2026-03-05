package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.validation.InEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "应用 App - 分销记录分页 Request VO")
@Data
public class BrokerageRecordPageAllTypesReqVO extends PageParam {

    @Schema(description = "用户编号", example = "25973")
    private Long userId;


    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "用户类型", example = "1")
    private Integer sourceUserLevel;

}
