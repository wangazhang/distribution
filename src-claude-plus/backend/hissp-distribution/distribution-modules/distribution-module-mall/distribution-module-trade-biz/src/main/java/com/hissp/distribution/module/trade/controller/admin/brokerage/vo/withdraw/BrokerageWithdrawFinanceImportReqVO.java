package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "管理后台 - 财务打款结果导入 Request VO")
@Data
public class BrokerageWithdrawFinanceImportReqVO {

    @Schema(description = "Excel 文件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "导入文件不能为空")
    private MultipartFile file;

}
