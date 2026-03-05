package com.hissp.distribution.module.mb.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * MB 订单虚拟发货消息
 */
@Data
@Accessors(chain = true)
public class MbOrderVirtualDeliveryMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    private Long orderId;
}
