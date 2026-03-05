package com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import com.alibaba.excel.annotation.*;

@Schema(description = "管理后台 - mb订单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MbOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15504")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "订单号", example = "MR123456789")
    @ExcelProperty("订单号")
    private String orderNo;

    @Schema(description = "商品ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "7561")
    @ExcelProperty("商品ID")
    private Long productId;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "restock")
    @ExcelIgnore
    private String bizType;

    @Schema(description = "业务类型名称", example = "物料补货")
    @ExcelProperty("业务类型")
    private String bizTypeName;

    @Schema(description = "代理用户ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "30083")
    @ExcelProperty("代理用户ID")
    private Long agentUserId;

    @Schema(description = "代理用户昵称", example = "小明")
    @ExcelProperty("代理用户昵称")
    private String agentUserNickname;

    @Schema(description = "代理用户手机号", example = "13800000000")
    @ExcelIgnore
    private String agentUserMobile;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数量")
    private Integer quantity;

    @Schema(description = "单价（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "17617")
    @ExcelIgnore
    private Integer unitPrice;

    @Schema(description = "单价（元）展示", example = "123.45")
    @ExcelProperty("单价（元）")
    private String unitPriceDisplay;

    @Schema(description = "总价（单位：分）", requiredMode = Schema.RequiredMode.REQUIRED, example = "6935")
    @ExcelIgnore
    private Integer totalPrice;

    @Schema(description = "总价（元）展示", example = "123.45")
    @ExcelProperty("总价（元）")
    private String totalPriceDisplay;

    @Schema(description = "商品名称", example = "补货套装")
    @ExcelProperty("商品名称")
    private String productName;

    @Schema(description = "商品图片", example = "https://xxx")
    @ExcelIgnore
    private String productImage;

    @Schema(description = "支付单ID", example = "14834")
    @ExcelProperty("支付单ID")
    private String paymentId;

    @Schema(description = "订单状态", example = "COMPLETED")
    @ExcelIgnore
    private String status;

    @Schema(description = "订单状态名称", example = "已完成")
    @ExcelProperty("订单状态")
    private String statusName;

    @Schema(description = "是否可退款")
    @ExcelIgnore
    private Boolean refundable;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "发货时间")
    @ExcelProperty("发货时间")
    private LocalDateTime deliveryTime;

    @Schema(description = "收货时间")
    @ExcelProperty("收货时间")
    private LocalDateTime receiveTime;

}
