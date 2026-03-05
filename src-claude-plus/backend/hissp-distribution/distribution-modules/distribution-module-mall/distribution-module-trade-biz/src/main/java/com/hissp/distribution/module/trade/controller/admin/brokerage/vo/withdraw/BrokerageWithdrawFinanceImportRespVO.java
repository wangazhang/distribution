package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 财务打款结果导入 Response VO")
@Data
public class BrokerageWithdrawFinanceImportRespVO {

    @Schema(description = "导入成功数量", example = "10")
    private Integer successCount;

    @Schema(description = "导入失败数量", example = "2")
    private Integer failureCount;

    @Schema(description = "失败原因")
    private List<String> failureReasons;

}
