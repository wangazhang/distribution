package com.hissp.distribution.module.mb.adapter.controller.app.trade.vo;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.hissp.distribution.framework.common.pojo.PageParam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "App - mb订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppMbOrderPageReqVO extends PageParam {

    @Schema(description = "商品ID", example = "1024")
    private Long productId;

    @Schema(description = "业务类型", example = "restock")
    private String bizType;

    @Schema(description = "订单状态", example = "COMPLETED")
    private String status;

    @Schema(description = "代理用户ID，由系统自动设置", hidden = true)
    private Long agentUserId;

    @Schema(description = "支付单ID", example = "pay_2023052115124332")
    private String paymentId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}