package com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - MB 订单详情 Response VO
 */
@Data
public class MbOrderDetailRespVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "15504")
    private Long id;

    @Schema(description = "订单号", example = "MR1234567890123")
    private String orderNo;

    @Schema(description = "商品ID", example = "7561")
    private Long productId;

    @Schema(description = "商品名称", example = "物料补货套餐")
    private String productName;

    @Schema(description = "商品图片")
    private String productImage;

    @Schema(description = "业务类型", example = "restock")
    private String bizType;

    @Schema(description = "业务类型名称", example = "物料补货")
    private String bizTypeName;

    @Schema(description = "订单状态", example = "COMPLETED")
    private String status;

    @Schema(description = "订单状态名称", example = "已完成")
    private String statusName;

    @Schema(description = "数量", example = "3")
    private Integer quantity;

    @Schema(description = "单价（分）", example = "16800")
    private Integer unitPrice;

    @Schema(description = "单价展示（元）", example = "168.00")
    private String unitPriceDisplay;

    @Schema(description = "总价（分）", example = "50400")
    private Integer totalPrice;

    @Schema(description = "总价展示（元）", example = "504.00")
    private String totalPriceDisplay;

    @Schema(description = "支付单ID", example = "9527001")
    private String paymentId;

    @Schema(description = "支付渠道编码", example = "wx_pub")
    private String payChannelCode;

    @Schema(description = "支付状态", example = "10")
    private Integer payStatus;

    @Schema(description = "支付状态名称", example = "支付成功")
    private String payStatusName;

    @Schema(description = "支付金额（分）", example = "50400")
    private Integer payPrice;

    @Schema(description = "支付金额展示（元）", example = "504.00")
    private String payPriceDisplay;

    @Schema(description = "支付成功时间")
    private LocalDateTime paySuccessTime;

    @Schema(description = "代理用户ID", example = "30083")
    private Long agentUserId;

    @Schema(description = "代理用户昵称", example = "张三")
    private String agentUserNickname;

    @Schema(description = "代理用户手机号", example = "13800000000")
    private String agentUserMobile;

    @Schema(description = "是否可以重推虚拟发货", example = "true")
    private Boolean canRetryVirtualDelivery;

    @Schema(description = "是否可以退款", example = "true")
    private Boolean refundable;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "是否可以确认收货", example = "true")
    private Boolean canReceive;
}
