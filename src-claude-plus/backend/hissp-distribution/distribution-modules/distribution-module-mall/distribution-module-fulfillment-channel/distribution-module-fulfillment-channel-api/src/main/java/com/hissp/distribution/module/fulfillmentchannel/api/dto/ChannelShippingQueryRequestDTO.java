package com.hissp.distribution.module.fulfillmentchannel.api.dto;

import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingChannelEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 渠道发货状态查询请求
 *
 * @author Codex
 */
@Data
public class ChannelShippingQueryRequestDTO implements Serializable {

    /**
     * 渠道类型，默认微信小程序
     */
    private Integer channel = ChannelShippingChannelEnum.WECHAT_MINIAPP.getCode();

    /**
     * 交易订单编号
     */
    private Long orderId;

    /**
     * 交易订单号
     */
    private String orderNo;

    /**
     * 支付订单编号
     */
    private Long payOrderId;

    /**
     * 是否已发货（含已完成），供部分渠道构造额外参数时快速判断
     */
    private Boolean delivered;
}
