package com.hissp.distribution.module.pay.api.order.dto;

import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付单信息 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PayOrderRespDTO {

    /**
     * 订单编号，数据库自增
     */
    private Long id;
    /**
     * 渠道编码
     *
     * 枚举 PayChannelEnum
     */
    private String channelCode;
    /**
     * 渠道订单号
     */
    private String channelOrderNo;
    /**
     * 渠道真实交易流水号
     *
     * 例如微信 transaction_id
     */
    private String channelTransactionNo;
    /**
     * 渠道用户编号，例如微信 openid
     */
    private String channelUserId;

    // ========== 商户相关字段 ==========
    /**
     * 商户订单编号
     * 例如说，内部系统 A 的订单号。需要保证每个 PayMerchantDO 唯一
     */
    private String merchantOrderId;

    // ========== 订单相关字段 ==========
    /**
     * 支付金额，单位：分
     */
    private Integer price;
    /**
     * 渠道手续费金额，单位：分
     */
    private Integer channelFeePrice;
    /**
     * 支付状态
     *
     * 枚举 {@link PayOrderStatusEnum}
     */
    private Integer status;

    /**
     * 订单支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 渠道妥投状态
     */
    private String deliveryStatus;

    // ========== 渠道相关字段 ==========

}
