package com.hissp.distribution.module.trade.service.order.handler.ext;

import com.hissp.distribution.module.product.enums.spu.ProductSpuTypeEnum;
import com.hissp.distribution.module.fulfillmentchannel.api.ChannelShippingApi;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingLogisticsTypeEnum;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelShippingVirtualGoodsPaymentSuccessHandlerTest {

    @Mock
    private ChannelShippingApi channelShippingApi;
    @Mock
    private TradeOrderItemMapper tradeOrderItemMapper;

    @InjectMocks
    private ChannelShippingVirtualGoodsPaymentSuccessHandler handler;

    @Test
    void onPayment_shouldPushVirtualShipping() {
        TradeOrderDO order = new TradeOrderDO();
        order.setId(1001L);
        order.setPayOrderId(2002L);
        order.setUserId(3003L);
        order.setNo("T202410270001");
        order.setPayChannelCode("wx_lite");

        TradeOrderItemDO virtualItem = buildOrderItem(11L, order.getId(), ProductSpuTypeEnum.VIRTUAL.getType(), "会员年卡");
        TradeOrderItemDO physicalItem = buildOrderItem(12L, order.getId(), ProductSpuTypeEnum.PHYSICAL.getType(), "线下体验券");
        when(tradeOrderItemMapper.selectListByOrderId(order.getId()))
                .thenReturn(List.of(virtualItem, physicalItem));

        handler.onPayment(order, virtualItem);

        ArgumentCaptor<ChannelShippingRequestDTO> captor = ArgumentCaptor.forClass(ChannelShippingRequestDTO.class);
        verify(channelShippingApi).ship(captor.capture());
        ChannelShippingRequestDTO reqDTO = captor.getValue();

        assertThat(reqDTO.getOrderId()).isEqualTo(order.getId());
        assertThat(reqDTO.getPayOrderId()).isEqualTo(order.getPayOrderId());
        assertThat(reqDTO.getLogisticsType()).isEqualTo(ChannelShippingLogisticsTypeEnum.VIRTUAL_GOODS.getType());
        assertThat(reqDTO.getAllDelivered()).isFalse();
        assertThat(reqDTO.getItems()).hasSize(1);
        ChannelShippingRequestDTO.Item itemDTO = reqDTO.getItems().get(0);
        assertThat(itemDTO.getOrderItemId()).isEqualTo(virtualItem.getId());
        assertThat(itemDTO.getSpuName()).isEqualTo("会员年卡");
        assertThat(reqDTO.getItemDesc()).contains("会员年卡");
    }

    @Test
    void onPayment_shouldSkipWhenMissingPayOrderId() {
        TradeOrderDO order = new TradeOrderDO();
        order.setId(1001L);
        order.setPayOrderId(null);
        order.setPayChannelCode("wx_lite");

        TradeOrderItemDO virtualItem = buildOrderItem(11L, order.getId(), ProductSpuTypeEnum.VIRTUAL.getType(), "会员年卡");

        handler.onPayment(order, virtualItem);

        verifyNoInteractions(channelShippingApi);
    }

    @Test
    void onPayment_shouldSkipWhenNotWechatChannel() {
        TradeOrderDO order = new TradeOrderDO();
        order.setId(1001L);
        order.setPayOrderId(2002L);
        order.setPayChannelCode("alipay_wap");

        TradeOrderItemDO virtualItem = buildOrderItem(11L, order.getId(), ProductSpuTypeEnum.VIRTUAL.getType(), "会员年卡");

        handler.onPayment(order, virtualItem);

        verifyNoInteractions(channelShippingApi);
    }

    @Test
    void supports_shouldMatchVirtualOnly() {
        TradeOrderItemDO virtualItem = new TradeOrderItemDO().setProductType(ProductSpuTypeEnum.VIRTUAL.getType());
        TradeOrderItemDO physicalItem = new TradeOrderItemDO().setProductType(ProductSpuTypeEnum.PHYSICAL.getType());

        assertThat(handler.supports(virtualItem)).isTrue();
        assertThat(handler.supports(physicalItem)).isFalse();
    }

    private TradeOrderItemDO buildOrderItem(Long id, Long orderId, Integer productType, String spuName) {
        TradeOrderItemDO item = new TradeOrderItemDO();
        item.setId(id);
        item.setOrderId(orderId);
        item.setSpuId(id * 10);
        item.setSpuName(spuName);
        item.setProductType(productType);
        item.setCount(1);
        return item;
    }
}
