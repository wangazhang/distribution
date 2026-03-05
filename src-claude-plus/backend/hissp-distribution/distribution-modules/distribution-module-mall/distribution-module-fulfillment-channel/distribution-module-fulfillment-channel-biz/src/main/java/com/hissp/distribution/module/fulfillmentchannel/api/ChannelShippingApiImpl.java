package com.hissp.distribution.module.fulfillmentchannel.api;

import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.framework.client.ChannelShippingClient;
import com.hissp.distribution.module.pay.api.order.PayOrderApi;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 渠道发货入口实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChannelShippingApiImpl implements ChannelShippingApi {

    private final PayOrderApi payOrderApi;
    private final List<ChannelShippingClient> shippingClients;

    @Override
    public void ship(ChannelShippingRequestDTO request) {
        ChannelShippingClient client = findClient(request.getChannel());
        PayOrderRespDTO payOrder = payOrderApi.getOrder(request.getPayOrderId());
        try {
            client.ship(request, payOrder);
        } catch (Exception ex) {
            log.error("[ship][orderId={}] 渠道发货失败", request.getOrderId(), ex);
            throw new RuntimeException("渠道发货失败：" + ex.getMessage(), ex);
        }
        log.info("[ship][orderId={}] 渠道发货成功", request.getOrderId());
    }

    @Override
    public ChannelShippingQueryRespDTO query(ChannelShippingQueryRequestDTO request) {
        ChannelShippingClient client = findClient(request.getChannel());
        PayOrderRespDTO payOrder = request.getPayOrderId() != null ? payOrderApi.getOrder(request.getPayOrderId()) : null;
        if (payOrder == null) {
            log.warn("[query][orderId={}] 未查询到支付单({})，返回默认发货状态", request.getOrderId(), request.getPayOrderId());
            return new ChannelShippingQueryRespDTO().setShipped(false);
        }
        try {
            ChannelShippingQueryRespDTO resp = client.query(request, payOrder);
            return resp != null ? resp : new ChannelShippingQueryRespDTO().setShipped(false);
        } catch (Exception ex) {
            log.warn("[query][orderId={}] 渠道发货状态查询失败", request.getOrderId(), ex);
            return new ChannelShippingQueryRespDTO().setShipped(false);
        }
    }

    private ChannelShippingClient findClient(Integer channel) {
        return shippingClients.stream()
                .filter(item -> item.supports(channel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("暂不支持的渠道：" + channel));
    }
}
