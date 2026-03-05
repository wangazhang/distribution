package com.hissp.distribution.module.mb.api.order.dto;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

/**
 * MB 订单简要响应 DTO
 *
 * @author codex
 */
@Data
public class MbOrderSimpleRespDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -6612849992921630570L;

    private Long id;
    private String orderNo;

    private Long productId;
    private String productName;
    private String productImage;

    private String bizType;
    private String bizTypeName;

    private String status;
    private String statusName;

    private Integer quantity;
    private Integer unitPrice;
    private String unitPriceDisplay;
    private Integer totalPrice;
    private String totalPriceDisplay;

    private String paymentId;
    private String payChannelCode;
    private Integer payStatus;
    private String payStatusName;
    private Integer payPrice;
    private String payPriceDisplay;
    private LocalDateTime paySuccessTime;

    private Long agentUserId;
    private String agentUserNickname;
    private String agentUserMobile;

    private Boolean canRetryVirtualDelivery;
    private Boolean canReceive;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime receiveTime;

}
