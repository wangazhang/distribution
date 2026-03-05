package com.hissp.distribution.module.trade.service.order.handler.ext;

import com.hissp.distribution.framework.test.core.ut.BaseMockitoUnitTest;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

class MbProductEventHandlerTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MbProductEventHandler mbProductEventHandler;

    @Mock
    private ProductSpuApi productSpuApi;
    @Mock
    private BrokerageUserApi brokerageUserApi;
    @Mock
    private BrokerageRecordApi brokerageRecordApi;

    @Test
    void testOnPayment_ShouldCreateCommission() {
        // Arrange
        TradeOrderDO order = new TradeOrderDO().setId(1L).setUserId(100L);
        TradeOrderItemDO item = new TradeOrderItemDO().setId(10L).setSpuId(200L);

        when(productSpuApi.isMbProduct(any())).thenReturn(true);
        when(productSpuApi.isCareerProduct(any())).thenReturn(false);
        when(brokerageUserApi.getBrokerageUser(100L)).thenReturn(new BrokerageUserRespDTO().setBrokerageEnabled(false));
        when(brokerageUserApi.getBindBrokerageUser(100L)).thenReturn(new BrokerageUserRespDTO().setId(50L));

        // Act
        mbProductEventHandler.onPayment(order, item);

        // Assert
        verify(brokerageRecordApi, times(1)).addBrokerageBatch(any());
    }

    @Test
    void testOnPayment_BuyerIsAgent_ShouldNotCreateCommission() {
        // Arrange
        TradeOrderDO order = new TradeOrderDO().setId(1L).setUserId(100L);
        TradeOrderItemDO item = new TradeOrderItemDO().setId(10L);

        when(brokerageUserApi.getBrokerageUser(100L)).thenReturn(new BrokerageUserRespDTO().setBrokerageEnabled(true));

        // Act
        mbProductEventHandler.onPayment(order, item);

        // Assert
        verify(brokerageRecordApi, never()).addBrokerageBatch(any());
    }

    @Test
    void testOnRefund_ShouldCancelCommission() {
        // Arrange
        TradeOrderDO order = new TradeOrderDO();
        TradeOrderItemDO item = new TradeOrderItemDO().setId(10L);

        // Act
        mbProductEventHandler.onRefund(order, item);

        // Assert
        verify(brokerageRecordApi, times(1)).cancelBrokerage(String.valueOf(item.getId()));
    }
}

