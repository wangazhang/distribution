package com.hissp.distribution.framework.pay.core.client.dto.order;

import com.hissp.distribution.framework.pay.core.client.exception.PayException;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderDeliveryStatusRespEnum;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 渠道支付订单 Response DTO
 *
 * @author 芋道源码
 */
@Data
public class PayOrderInnerRespDTO {

    /**
     * 支付状态
     *
     * 枚举：{@link PayOrderStatusRespEnum}
     */
    private Integer status;

    /**
     * 妥投状态
     *
     * 枚举：{@link PayOrderDeliveryStatusRespEnum}
     */
    private PayOrderDeliveryStatusRespEnum deliveryStatus;

    /**
     * 外部订单号
     *
     * 对应 PayOrderExtensionDO 的 no 字段
     */
    private String outTradeNo;

    /**
     * 支付渠道编号
     */
    private String channelOrderNo;
    /**
     * 支付渠道真实交易流水号
     *
     * 例如微信 transaction_id
     */
    private String channelTransactionNo;
    /**
     * 支付渠道用户编号
     */
    private String channelUserId;
    /**
     * 渠道手续费费率
     */
    private Double channelFeeRate;
    /**
     * 渠道手续费
     */
    private Integer channelFeePrice;

    /**
     * 支付成功时间
     */
    private LocalDateTime successTime;

    /**
     * 原始的同步/异步通知结果
     */
    private Object rawData;

    // ========== 主动发起支付时，会返回的字段 ==========

    /**
     * 展示模式
     *
     * 枚举 {@link PayOrderDisplayModeEnum} 类
     */
    private String displayMode;
    /**
     * 展示内容
     */
    private String displayContent;

    /**
     * 调用渠道的错误码
     *
     * 注意：这里返回的是业务异常，而是不系统异常。
     * 如果是系统异常，则会抛出 {@link PayException}
     */
    private String channelErrorCode;
    /**
     * 调用渠道报错时，错误信息
     */
    private String channelErrorMsg;

    public PayOrderInnerRespDTO() {
    }

    /**
     * 创建【WAITING】状态的订单返回
     */
    public static PayOrderInnerRespDTO waitingOf(String displayMode, String displayContent,
                                                 String outTradeNo, Object rawData) {
        PayOrderInnerRespDTO respDTO = new PayOrderInnerRespDTO();
        respDTO.status = PayOrderStatusRespEnum.WAITING.getStatus();
        respDTO.displayMode = displayMode;
        respDTO.displayContent = displayContent;
        // 相对通用的字段
        respDTO.outTradeNo = outTradeNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【SUCCESS】状态的订单返回
     */
    public static PayOrderInnerRespDTO successOf(String channelOrderNo, String channelUserId, LocalDateTime successTime,
                                                 String outTradeNo, Object rawData) {
        PayOrderInnerRespDTO respDTO = new PayOrderInnerRespDTO();
        respDTO.status = PayOrderStatusRespEnum.SUCCESS.getStatus();
        respDTO.channelOrderNo = channelOrderNo;
        respDTO.channelTransactionNo = channelOrderNo;
        respDTO.channelUserId = channelUserId;
        respDTO.successTime = successTime;
        // 相对通用的字段
        respDTO.outTradeNo = outTradeNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建指定状态的订单返回，适合支付渠道回调时
     */
    public static PayOrderInnerRespDTO of(Integer status, String channelOrderNo, String channelUserId, LocalDateTime successTime,
                                          String outTradeNo, Object rawData) {
        PayOrderInnerRespDTO respDTO = new PayOrderInnerRespDTO();
        respDTO.status = status;
        respDTO.channelOrderNo = channelOrderNo;
        respDTO.channelTransactionNo = channelOrderNo;
        respDTO.channelUserId = channelUserId;
        respDTO.successTime = successTime;
        // 相对通用的字段
        respDTO.outTradeNo = outTradeNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

    /**
     * 创建【CLOSED】状态的订单返回，适合调用支付渠道失败时
     */
    public static PayOrderInnerRespDTO closedOf(String channelErrorCode, String channelErrorMsg,
                                                String outTradeNo, Object rawData) {
        PayOrderInnerRespDTO respDTO = new PayOrderInnerRespDTO();
        respDTO.status = PayOrderStatusRespEnum.CLOSED.getStatus();
        respDTO.channelErrorCode = channelErrorCode;
        respDTO.channelErrorMsg = channelErrorMsg;
        // 相对通用的字段
        respDTO.outTradeNo = outTradeNo;
        respDTO.rawData = rawData;
        return respDTO;
    }

}
