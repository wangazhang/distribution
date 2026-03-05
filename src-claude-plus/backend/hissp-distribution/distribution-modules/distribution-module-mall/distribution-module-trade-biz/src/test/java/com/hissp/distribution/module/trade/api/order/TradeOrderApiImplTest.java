package com.hissp.distribution.module.trade.api.order;

import com.hissp.distribution.module.trade.api.order.dto.TradeOrderDeliveryReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemCommentCreateReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.service.order.TradeOrderQueryService;
import com.hissp.distribution.module.trade.service.order.TradeOrderUpdateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * TradeOrderApiImpl 单元测试
 * 验证新增的API方法是否正确调用内部服务
 * 
 * @author system
 */
@ExtendWith(MockitoExtension.class)
class TradeOrderApiImplTest {

    @Mock
    private TradeOrderUpdateService tradeOrderUpdateService;

    @Mock
    private TradeOrderQueryService tradeOrderQueryService;

    @InjectMocks
    private TradeOrderApiImpl tradeOrderApi;

    @BeforeEach
    void setUp() {
        // 重置所有mock
        reset(tradeOrderUpdateService, tradeOrderQueryService);
    }

    @Test
    void testDeliveryOrder_Success() {
        // Given
        TradeOrderDeliveryReqDTO deliveryReqDTO = new TradeOrderDeliveryReqDTO();
        deliveryReqDTO.setOrderId(12345L);
        deliveryReqDTO.setLogisticsId(1L);
        deliveryReqDTO.setLogisticsNo("SF123456789");
        deliveryReqDTO.setOperatorId(100L);
        deliveryReqDTO.setRemark("测试发货");

        doNothing().when(tradeOrderUpdateService).deliveryOrder(any());

        // When
        tradeOrderApi.deliveryOrder(deliveryReqDTO);

        // Then
        verify(tradeOrderUpdateService, times(1)).deliveryOrder(any());
    }

    @Test
    void testDeliveryOrder_VirtualProduct() {
        // Given - 虚拟商品发货（无需物流信息）
        TradeOrderDeliveryReqDTO deliveryReqDTO = new TradeOrderDeliveryReqDTO();
        deliveryReqDTO.setOrderId(12345L);
        deliveryReqDTO.setLogisticsId(null);
        deliveryReqDTO.setLogisticsNo("");
        deliveryReqDTO.setRemark("生涯产品自动发货");

        doNothing().when(tradeOrderUpdateService).deliveryOrder(any());

        // When
        tradeOrderApi.deliveryOrder(deliveryReqDTO);

        // Then
        verify(tradeOrderUpdateService, times(1)).deliveryOrder(any());
    }

    @Test
    void testReceiveOrderByMember_Success() {
        // Given
        Long userId = 67890L;
        Long orderId = 12345L;

        doNothing().when(tradeOrderUpdateService).receiveOrderByMember(userId, orderId);

        // When
        tradeOrderApi.receiveOrderByMember(userId, orderId);

        // Then
        verify(tradeOrderUpdateService, times(1)).receiveOrderByMember(userId, orderId);
    }

    @Test
    void testCreateOrderItemComment_Success() {
        // Given
        TradeOrderItemCommentCreateReqDTO commentReqDTO = new TradeOrderItemCommentCreateReqDTO();
        commentReqDTO.setAnonymous(true);
        commentReqDTO.setOrderItemId(11111L);
        commentReqDTO.setDescriptionScores(5);
        commentReqDTO.setBenefitScores(5);
        commentReqDTO.setContent("系统代客评价");
        commentReqDTO.setPicUrls(List.of());
        commentReqDTO.setUserId(67890L);

        Long expectedCommentId = 22222L;
        when(tradeOrderUpdateService.createOrderItemCommentByMember(eq(67890L), any()))
            .thenReturn(expectedCommentId);

        // When
        Long commentId = tradeOrderApi.createOrderItemComment(commentReqDTO);

        // Then
        assertEquals(expectedCommentId, commentId);
        verify(tradeOrderUpdateService, times(1))
            .createOrderItemCommentByMember(eq(67890L), any());
    }

    @Test
    void testCreateOrderItemComment_WithPictures() {
        // Given
        TradeOrderItemCommentCreateReqDTO commentReqDTO = new TradeOrderItemCommentCreateReqDTO();
        commentReqDTO.setAnonymous(false);
        commentReqDTO.setOrderItemId(11111L);
        commentReqDTO.setDescriptionScores(4);
        commentReqDTO.setBenefitScores(5);
        commentReqDTO.setContent("很好的商品，推荐购买！");
        commentReqDTO.setPicUrls(List.of(
            "https://example.com/pic1.jpg",
            "https://example.com/pic2.jpg"
        ));
        commentReqDTO.setUserId(67890L);

        Long expectedCommentId = 33333L;
        when(tradeOrderUpdateService.createOrderItemCommentByMember(eq(67890L), any()))
            .thenReturn(expectedCommentId);

        // When
        Long commentId = tradeOrderApi.createOrderItemComment(commentReqDTO);

        // Then
        assertEquals(expectedCommentId, commentId);
        verify(tradeOrderUpdateService, times(1))
            .createOrderItemCommentByMember(eq(67890L), any());
    }

    @Test
    void testCancelPaidOrder_Success() {
        // Given
        Long userId = 67890L;
        Long orderId = 12345L;
        Integer cancelType = 1;

        doNothing().when(tradeOrderUpdateService).cancelPaidOrder(userId, orderId, cancelType);

        // When
        tradeOrderApi.cancelPaidOrder(userId, orderId, cancelType);

        // Then
        verify(tradeOrderUpdateService, times(1)).cancelPaidOrder(userId, orderId, cancelType);
    }

    @Test
    void testGetOrder_Success() {
        // Given
        Long orderId = 12345L;
        TradeOrderRespDTO expectedOrder = new TradeOrderRespDTO();
        expectedOrder.setId(orderId);
        expectedOrder.setUserId(67890L);

        when(tradeOrderQueryService.getOrder(orderId)).thenReturn(null);

        // When
        TradeOrderRespDTO order = tradeOrderApi.getOrder(orderId);

        // Then
        verify(tradeOrderQueryService, times(1)).getOrder(orderId);
    }

    @Test
    void testGetOrderList_Success() {
        // Given
        List<Long> orderIds = List.of(12345L, 12346L, 12347L);

        when(tradeOrderQueryService.getOrderList(orderIds)).thenReturn(List.of());

        // When
        List<TradeOrderRespDTO> orders = tradeOrderApi.getOrderList(orderIds);

        // Then
        assertNotNull(orders);
        verify(tradeOrderQueryService, times(1)).getOrderList(orderIds);
    }
}
