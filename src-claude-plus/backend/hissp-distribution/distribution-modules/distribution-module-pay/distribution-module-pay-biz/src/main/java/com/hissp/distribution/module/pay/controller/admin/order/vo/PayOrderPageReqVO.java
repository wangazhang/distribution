package com.hissp.distribution.module.pay.controller.admin.order.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 支付订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PayOrderPageReqVO extends PageParam {

    // 支付 ID 精准查询，避免与支付 NO 概念混淆
    @Schema(description = "支付 ID", example = "1024")
    private Long id;

    @Schema(description = "应用编号", example = "1024")
    private Long appId;

    @Schema(description = "渠道编码", example = "wx_app")
    private String channelCode;

    @Schema(description = "商户订单编号", example = "4096")
    private String merchantOrderId;

    @Schema(description = "渠道编号", example = "1888")
    private String channelOrderNo;

    @Schema(description = "支付单号", example = "2014888")
    private String no;

    @Schema(description = "支付状态", example = "0")
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
