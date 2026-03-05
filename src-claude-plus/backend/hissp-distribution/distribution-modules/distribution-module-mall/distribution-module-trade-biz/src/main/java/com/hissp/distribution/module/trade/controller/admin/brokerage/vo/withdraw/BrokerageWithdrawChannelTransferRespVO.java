package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

import java.time.LocalDateTime;

@Data
@Schema(description = "管理后台 - 佣金提现渠道打款详情 Response VO")
public class BrokerageWithdrawChannelTransferRespVO {

    @Schema(description = "提现编号")
    private Long withdrawId;

    @Schema(description = "提现状态")
    private Integer withdrawStatus;

    @Schema(description = "提现类型")
    private Integer withdrawType;

    @Schema(description = "提现金额（分）")
    private Integer withdrawPrice;

    @Schema(description = "是否允许渠道打款")
    private Boolean channelPayEnabled;

    @Schema(description = "重新发起是否需要口令")
    private Boolean channelRetryPasswordRequired;

    @Schema(description = "渠道转账详情")
    private TransferDetail transfer;

    @Data
    public static class TransferDetail {
        @Schema(description = "转账编号")
        private Long id;
        @Schema(description = "转账状态")
        private Integer status;
        @Schema(description = "转账状态名")
        private String statusName;
        @Schema(description = "金额（分）")
        private Integer price;
        @Schema(description = "渠道流水号")
        private String channelTransferNo;
        @Schema(description = "渠道错误码")
        private String channelErrorCode;
        @Schema(description = "渠道错误提示")
        private String channelErrorMsg;
        @Schema(description = "成功时间")
        private LocalDateTime successTime;
        @Schema(description = "创建时间")
        private LocalDateTime createTime;
        @Schema(description = "更新时间")
        private LocalDateTime updateTime;
        @Schema(description = "渠道扩展参数")
        private Map<String, String> channelExtras;
        @Schema(description = "渠道原始通知 JSON")
        private String channelNotifyData;
        @Schema(description = "账户划拨阶段")
        private BrokerageWithdrawChannelTransferStageRespVO transferStage;
        @Schema(description = "银行卡出款阶段")
        private BrokerageWithdrawChannelTransferStageRespVO withdrawStage;
    }
}
