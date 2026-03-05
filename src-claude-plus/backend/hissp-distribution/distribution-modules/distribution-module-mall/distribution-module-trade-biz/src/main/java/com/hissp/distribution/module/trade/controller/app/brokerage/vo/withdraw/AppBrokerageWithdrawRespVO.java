package com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 分销提现 Response VO")
@Data
public class AppBrokerageWithdrawRespVO {

    @Schema(description = "提现编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Long id;

    @Schema(description = "提现状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer status;

    @Schema(description = "提现状态名", requiredMode = Schema.RequiredMode.REQUIRED, example = "审核中")
    private String statusName;

    @Schema(description = "提现金额，单位：分", requiredMode = Schema.RequiredMode.REQUIRED, example = "1000")
    private Integer price;

    @Schema(description = "手续费，单位：分", example = "60")
    private Integer feePrice;

    @Schema(description = "当前总佣金，单位：分", example = "10000")
    private Integer totalPrice;

    @Schema(description = "提现类型", example = "5")
    private Integer type;

    @Schema(description = "提现类型名称", example = "微信零钱")
    private String typeName;

    @Schema(description = "真实姓名", example = "张三")
    private String name;

    @Schema(description = "到账账号", example = "622202******8301")
    private String accountNo;

    @Schema(description = "银行名称", example = "工商银行")
    private String bankName;

    @Schema(description = "开户地址", example = "北京市东城区某支行")
    private String bankAddress;

    @Schema(description = "收款码地址", example = "https://xxx.png")
    private String accountQrCodeUrl;

    @Schema(description = "提现时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "审核/失败原因", example = "账户信息不正确")
    private String auditReason;

}
