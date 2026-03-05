package com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 佣金记录关联业务摘要响应
 *
 * @author codex
 */
@Schema(description = "管理后台 - 佣金记录业务详情 Response VO")
@Data
public class BrokerageRecordBizDetailRespVO {

    @Schema(description = "佣金记录编号", example = "10086")
    private Long recordId;

    @Schema(description = "佣金用户编号", example = "30001")
    private Long userId;

    @Schema(description = "业务类型", example = "3001")
    private Integer bizType;

    @Schema(description = "业务大类", example = "1")
    private Integer bizCategory;

    @Schema(description = "业务编号", example = "501122")
    private String bizId;

    @Schema(description = "记录标题")
    private String title;

    @Schema(description = "记录说明")
    private String description;

    @Schema(description = "金额（分）")
    private Integer price;

    @Schema(description = "结算后总金额（分）")
    private Integer totalPrice;

    @Schema(description = "记录状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "商城订单详情")
    private MallOrderBizDetail mallOrder;

    @Schema(description = "MB 订单详情")
    private MbOrderBizDetail mbOrder;

    @Data
    public static class MallOrderBizDetail {

        @Schema(description = "商城订单编号")
        private Long orderId;

        @Schema(description = "商城订单号")
        private String orderNo;

        @Schema(description = "订单状态")
        private Integer orderStatus;

        @Schema(description = "订单状态名称")
        private String orderStatusName;

        @Schema(description = "售后整体状态")
        private Integer refundStatus;

        @Schema(description = "售后整体状态名称")
        private String refundStatusName;

        @Schema(description = "订单实付金额（分）")
        private Integer orderPayPrice;

        @Schema(description = "支付是否完成")
        private Boolean payFinished;

        @Schema(description = "支付渠道编码")
        private String payChannelCode;

        @Schema(description = "支付时间")
        private LocalDateTime payTime;

        @Schema(description = "配送方式")
        private Integer deliveryType;

        @Schema(description = "配送方式名称")
        private String deliveryTypeName;

        @Schema(description = "收件信息")
        private MallOrderReceiver receiver;

        @Schema(description = "订单项信息")
        private MallOrderItem item;
    }

    @Data
    public static class MallOrderReceiver {

        @Schema(description = "收件人姓名")
        private String name;

        @Schema(description = "收件人手机号")
        private String mobile;

        @Schema(description = "地区全称")
        private String areaName;

        @Schema(description = "详细地址")
        private String detailAddress;
    }

    @Data
    public static class MallOrderItem {

        @Schema(description = "订单项编号")
        private Long itemId;

        @Schema(description = "商品 SPU 编号")
        private Long spuId;

        @Schema(description = "商品 SKU 编号")
        private Long skuId;

        @Schema(description = "商品名称")
        private String spuName;

        @Schema(description = "SKU 规格")
        private String propertiesText;

        @Schema(description = "商品图片")
        private String picUrl;

        @Schema(description = "购买数量")
        private Integer count;

        @Schema(description = "单价（分）")
        private Integer unitPrice;

        @Schema(description = "实际支付金额（分）")
        private Integer payPrice;

        @Schema(description = "售后状态")
        private Integer afterSaleStatus;

        @Schema(description = "售后状态名称")
        private String afterSaleStatusName;
    }

    @Data
    public static class MbOrderBizDetail {

        @Schema(description = "MB 订单编号")
        private Long orderId;

        @Schema(description = "MB 订单号")
        private String orderNo;

        @Schema(description = "状态编码")
        private String status;

        @Schema(description = "状态名称")
        private String statusName;

        @Schema(description = "业务类型")
        private String bizType;

        @Schema(description = "业务类型名称")
        private String bizTypeName;

        @Schema(description = "商品编号")
        private Long productId;

        @Schema(description = "商品名称")
        private String productName;

        @Schema(description = "商品图片")
        private String productImage;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "单价（分）")
        private Integer unitPrice;

        @Schema(description = "总价（分）")
        private Integer totalPrice;

        @Schema(description = "支付金额（分）")
        private Integer payPrice;

        @Schema(description = "支付渠道编码")
        private String payChannelCode;

        @Schema(description = "支付状态")
        private Integer payStatus;

        @Schema(description = "支付状态名称")
        private String payStatusName;

        @Schema(description = "支付单编号")
        private String paymentId;

        @Schema(description = "支付成功时间")
        private LocalDateTime paySuccessTime;

        @Schema(description = "代理用户编号")
        private Long agentUserId;

        @Schema(description = "代理用户昵称")
        private String agentUserNickname;

        @Schema(description = "代理用户手机号")
        private String agentUserMobile;

        @Schema(description = "是否可重推虚拟发货")
        private Boolean canRetryVirtualDelivery;

        @Schema(description = "是否可确认收货")
        private Boolean canReceive;

        @Schema(description = "创建时间")
        private LocalDateTime createTime;

        @Schema(description = "更新时间")
        private LocalDateTime updateTime;

        @Schema(description = "发货时间")
        private LocalDateTime deliveryTime;

        @Schema(description = "收货时间")
        private LocalDateTime receiveTime;
    }
}
