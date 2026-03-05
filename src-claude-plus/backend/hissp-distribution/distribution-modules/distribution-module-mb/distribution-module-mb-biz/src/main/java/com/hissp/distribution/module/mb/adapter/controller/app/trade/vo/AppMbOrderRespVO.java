package com.hissp.distribution.module.mb.adapter.controller.app.trade.vo;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "App - mb订单响应 VO")
@Data
@ExcelIgnoreUnannotated
public class AppMbOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    @ExcelProperty("商品ID")
    private Long productId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "restock")
    @ExcelProperty("业务类型")
    private String bizType;

    @Schema(description = "商品名称", example = "ECM人物料产品")
    private String productName;

    @Schema(description = "商品图片", example = "https://example.com/image.jpg")
    private String productImage;

    @Schema(description = "代理用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "4096")
    @ExcelProperty("代理用户ID")
    private Long agentUserId;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "5")
    @ExcelProperty("数量")
    private Integer quantity;

    @Schema(description = "单价（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "30000")
    @ExcelProperty("单价（单位：分）")
    private Integer unitPrice;

    @Schema(description = "总价（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "150000")
    @ExcelProperty("总价（单位：分）")
    private Integer totalPrice;

    @Schema(description = "支付单ID", example = "pay_2023052115124332")
    @ExcelProperty("支付单ID")
    private String paymentId;
    
    @Schema(description = "订单状态", example = "1")
    private Integer status;
    
    @Schema(description = "订单状态名称", example = "已支付")
    private String statusName;

    @Schema(description = "是否可以确认收货", example = "true")
    private Boolean canReceive;

    @Schema(description = "订单号", example = "MR195548602408803123")
    private String orderNo;

    @Schema(description = "总价显示（元）", example = "300.00")
    private String totalPriceDisplay;

    @Schema(description = "单价显示（元）", example = "300.00")
    private String unitPriceDisplay;

    @Schema(description = "是否可以退款", example = "true")
    private Boolean canRefund;

    @Schema(description = "创建时间显示", example = "08-13 12:26")
    private String createTimeDisplay;

    @Schema(description = "状态颜色", example = "#34c759")
    private String statusColor;

    @Schema(description = "状态图标", example = "success")
    private String statusIcon;

    @Schema(description = "业务类型显示", example = "物料补货")
    private String bizTypeDisplay;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    private LocalDateTime receiveTime;

    @Schema(description = "微信确认收货额外参数")
    @JsonProperty("wechat_extra_data")
    private AppMbOrderWeixinExtraRespVO wechatExtraData;

}
