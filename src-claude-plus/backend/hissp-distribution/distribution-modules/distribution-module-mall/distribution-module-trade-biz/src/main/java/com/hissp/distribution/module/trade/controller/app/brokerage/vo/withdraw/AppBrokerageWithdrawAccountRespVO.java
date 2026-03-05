package com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户 App - 最近使用的提现账户 Response VO
 *
 * @author Codex
 */
@Schema(description = "用户 App - 最近使用的提现账户 Response VO")
@Data
public class AppBrokerageWithdrawAccountRespVO {

    @Schema(description = "提现方式", example = "2")
    private Integer type;

    @Schema(description = "提现账号", example = "622202*********8301")
    private String accountNo;

    @Schema(description = "收款码的图片", example = "https://www.example.com/1.png")
    private String accountQrCodeUrl;

    @Schema(description = "持卡人姓名", example = "张三")
    private String name;

    @Schema(description = "提现银行", example = "工商银行")
    private String bankName;

    @Schema(description = "开户地址", example = "海淀支行")
    private String bankAddress;

}
