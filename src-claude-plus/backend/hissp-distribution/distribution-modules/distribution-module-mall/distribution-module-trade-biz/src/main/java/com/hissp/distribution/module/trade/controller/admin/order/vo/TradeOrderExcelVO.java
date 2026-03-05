package com.hissp.distribution.module.trade.controller.admin.order.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台 - 交易订单 Excel 导出 VO
 */
@Data
@ExcelIgnoreUnannotated
public class TradeOrderExcelVO {

    @ExcelProperty("订单编号")
    private Long id;

    @ExcelProperty("订单流水号")
    private String no;

    @ExcelProperty("用户昵称")
    private String userNickname;

    @ExcelProperty("用户手机号")
    private String userMobile;

    @ExcelProperty("订单状态")
    private String statusName;

    @ExcelProperty("订单类型")
    private String typeName;

    @ExcelProperty("订单来源")
    private String terminalName;

    @ExcelProperty("商品数量")
    private Integer productCount;

    @ExcelProperty("商品名称")
    private String productNames;

    @ExcelProperty("商品金额(元)")
    private String totalPrice;

    @ExcelProperty("优惠金额(元)")
    private String discountPrice;

    @ExcelProperty("运费(元)")
    private String deliveryPrice;

    @ExcelProperty("应付金额(元)")
    private String payPrice;

    @ExcelProperty("支付状态")
    private String payStatusName;

    @ExcelProperty("支付渠道")
    private String payChannelName;

    @ExcelProperty("支付时间")
    private LocalDateTime payTime;

    @ExcelProperty("配送方式")
    private String deliveryTypeName;

    @ExcelProperty("物流单号")
    private String logisticsNo;

    @ExcelProperty("收件人")
    private String receiverName;

    @ExcelProperty("收件人手机")
    private String receiverMobile;

    @ExcelProperty("收件地区")
    private String receiverAreaName;

    @ExcelProperty("收件详细地址")
    private String receiverDetailAddress;

    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @ExcelProperty("发货时间")
    private LocalDateTime deliveryTime;

    @ExcelProperty("收货时间")
    private LocalDateTime receiveTime;

    @ExcelProperty("完成时间")
    private LocalDateTime finishTime;

    @ExcelProperty("退款金额(元)")
    private String refundPrice;

}
