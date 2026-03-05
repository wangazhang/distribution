package com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.hissp.distribution.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import java.util.List;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - mb订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MbOrderPageReqVO extends PageParam {

    @Schema(description = "商品ID", example = "7561")
    private Long productId;

    @Schema(description = "订单号", example = "MR123456789")
    private String orderNo;

    @Schema(description = "业务类型", example = "restock")
    private String bizType;

    @Schema(description = "订单状态", example = "COMPLETED")
    private String status;

    @Schema(description = "代理用户ID", example = "30083")
    private Long agentUserId;

    @Schema(description = "代理用户昵称")
    private String agentUserName;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "单价（单位：分）", example = "17617")
    private Integer unitPrice;

    @Schema(description = "总价（单位：分）", example = "6935")
    private Integer totalPrice;

    @Schema(description = "支付单号", example = "14834")
    private String paymentId;

    @Schema(description = "物料/商品名称", example = "胶原蛋白")
    private String productName;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    // ====== 内部使用的筛选字段 ======
    @Schema(hidden = true)
    private Long orderId;

    @Schema(hidden = true)
    private List<Long> agentUserIds;

    @Schema(hidden = true)
    private List<Long> productIds;

}
