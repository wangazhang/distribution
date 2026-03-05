package com.hissp.distribution.module.fulfillmentchannel.api.dto;

import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingChannelEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * 渠道发货状态批量查询请求
 *
 * <p>目前仅支持微信小程序渠道，通过时间范围、订单状态或 openId 分页拉取发货状态。</p>
 */
@Data
public class ChannelShippingQueryListRequestDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 渠道类型，默认微信小程序
     */
    private Integer channel = ChannelShippingChannelEnum.WECHAT_MINIAPP.getCode();

    /**
     * 支付成功时间范围开始（毫秒时间戳，可为空）
     */
    private Long payTimeBegin;

    /**
     * 支付成功时间范围结束（毫秒时间戳，可为空）
     */
    private Long payTimeEnd;

    /**
     * 渠道订单状态（参考微信文档定义，可为空）
     */
    private Integer orderState;

    /**
     * 微信 openId，可选，用于按用户过滤
     */
    private String openId;

    /**
     * 翻页游标，对应微信返回的 last_index
     */
    private String lastIndex;

    /**
     * 单次拉取数量，微信范围 1~20
     */
    private Long pageSize;
}
