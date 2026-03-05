package com.hissp.distribution.module.fulfillmentchannel.api;

import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;

/**
 * 渠道发货统一入口
 *
 * <p>交易域调用该接口，由履约模块匹配具体渠道实现并调用上游。</p>
 */
public interface ChannelShippingApi {

    /**
     * 推送发货信息到渠道
     *
     * @param request 发货请求
     */
    void ship(ChannelShippingRequestDTO request);

    /**
     * 查询渠道发货状态及所需额外参数
     *
     * @param request 查询请求
     * @return 渠道返回的查询结果
     */
    ChannelShippingQueryRespDTO query(ChannelShippingQueryRequestDTO request);
}
