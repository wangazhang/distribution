package com.hissp.distribution.module.mb.api.commission.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 分佣订单快照
 */
@Data
public class CommissionOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;

    private String bizOrderNo;

    private Long productId;

    private Long packageId;

    private Integer quantity;

    private Integer unitPrice;

    private Integer totalPrice;
}
