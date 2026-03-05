package com.hissp.distribution.module.fulfillmentchannel.framework.client;

import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;

/**
 * 渠道发货客户端
 */
public interface ChannelShippingClient {

    /**
     * 是否支持指定渠道
     *
     * @param channel 渠道编码
     * @return 支持返回 true
     */
    boolean supports(Integer channel);

    /**
     * 推送发货信息
     *
     * @param request 发货请求
     * @param payOrder 渠道支付信息（可能为空）
     * @throws Exception 调用渠道异常
     */
    void ship(ChannelShippingRequestDTO request, PayOrderRespDTO payOrder) throws Exception;

    /**
     * 查询渠道发货状态
     *
     * @param request 查询请求
     * @param payOrder 渠道支付信息
     * @return 查询结果
     * @throws Exception 调用渠道异常
     */
    ChannelShippingQueryRespDTO query(ChannelShippingQueryRequestDTO request, PayOrderRespDTO payOrder) throws Exception;
}
