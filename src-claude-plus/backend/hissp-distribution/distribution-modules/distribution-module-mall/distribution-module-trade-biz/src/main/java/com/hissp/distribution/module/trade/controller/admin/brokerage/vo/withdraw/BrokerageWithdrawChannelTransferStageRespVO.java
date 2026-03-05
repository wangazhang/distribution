package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 渠道打款阶段详情 VO
 */
@Data
public class BrokerageWithdrawChannelTransferStageRespVO {

    @Schema(description = "阶段标识，例如账户划拨/银行卡出款")
    private String stage;

    @Schema(description = "阶段状态原值 SUCCESS/FAILED/PROCESSING")
    private String status;

    @Schema(description = "阶段状态名")
    private String statusName;

    @Schema(description = "渠道流水号")
    private String channelTransferNo;

    @Schema(description = "渠道请求号")
    private String requestId;

    @Schema(description = "错误码")
    private String errorCode;

    @Schema(description = "错误提示")
    private String errorMessage;

    @Schema(description = "完成时间")
    private LocalDateTime successTime;

    @Schema(description = "渠道原始数据")
    private Map<String, Object> raw;
}
