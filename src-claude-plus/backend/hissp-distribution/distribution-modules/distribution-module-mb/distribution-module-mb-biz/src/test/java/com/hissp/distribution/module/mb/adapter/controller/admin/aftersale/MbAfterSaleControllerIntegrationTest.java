package com.hissp.distribution.module.mb.adapter.controller.admin.aftersale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hissp.distribution.framework.test.core.ut.BaseDbUnitTest;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.MbAfterSaleController;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import com.hissp.distribution.module.mb.domain.service.trade.refund.MbOrderRefundReverseOperationService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link MbAfterSaleController} 的集成测试类
 *
 * @author system
 */
@Import(MbAfterSaleController.class)
@AutoConfigureWebMvc
public class MbAfterSaleControllerIntegrationTest extends BaseDbUnitTest {

    @Resource
    private MockMvc mockMvc;

    @MockBean
    private MbOrderRefundReverseOperationService mbOrderRefundReverseOperationService;

    @Resource
    private ObjectMapper objectMapper;

    @Test
    public void testUpdateOrderRefunded_Success() throws Exception {
        // 准备参数
        PayRefundNotifyReqDTO notifyReqDTO = new PayRefundNotifyReqDTO();
        notifyReqDTO.setMerchantOrderId("MR12345");
        notifyReqDTO.setPayRefundId(1L);

        // Mock 服务调用
        when(mbOrderRefundReverseOperationService.processRefundNotification(any()))
            .thenReturn(true);

        // 执行请求
        mockMvc.perform(post("/mb/after-sale/update-refunded")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notifyReqDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(true));

        // 验证服务调用
        verify(mbOrderRefundReverseOperationService).processRefundNotification(notifyReqDTO);
    }

    @Test
    public void testUpdateOrderRefunded_Failure() throws Exception {
        // 准备参数
        PayRefundNotifyReqDTO notifyReqDTO = new PayRefundNotifyReqDTO();
        notifyReqDTO.setMerchantOrderId("UNKNOWN12345");
        notifyReqDTO.setPayRefundId(1L);

        // Mock 服务调用失败
        when(mbOrderRefundReverseOperationService.processRefundNotification(any()))
            .thenReturn(false);

        // 执行请求
        mockMvc.perform(post("/mb/after-sale/update-refunded")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notifyReqDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").value(false));

        // 验证服务调用
        verify(mbOrderRefundReverseOperationService).processRefundNotification(notifyReqDTO);
    }

    @Test
    public void testUpdateOrderRefunded_ServiceException() throws Exception {
        // 准备参数
        PayRefundNotifyReqDTO notifyReqDTO = new PayRefundNotifyReqDTO();
        notifyReqDTO.setMerchantOrderId("MR12345");
        notifyReqDTO.setPayRefundId(1L);

        // Mock 服务调用抛出异常
        when(mbOrderRefundReverseOperationService.processRefundNotification(any()))
            .thenThrow(new RuntimeException("退款处理异常"));

        // 执行请求，期望抛出异常
        mockMvc.perform(post("/mb/after-sale/update-refunded")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notifyReqDTO)))
                .andExpect(status().isInternalServerError());

        // 验证服务调用
        verify(mbOrderRefundReverseOperationService).processRefundNotification(notifyReqDTO);
    }
}
