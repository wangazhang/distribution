package com.hissp.distribution.module.mb.adapter.controller.app.trade;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.mb.domain.service.trade.refund.MbOrderRefundReverseOperationService;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * MB售后退款回调Controller
 * 处理MB业务的退款成功回调，包括补货订单和物料转化订单的退款逆操作
 *
 * @author system
 */
@Tag(name = "用户 App - MB售后退款回调")
@RestController
@RequestMapping("/mb/after-sale")
@Validated
@Slf4j
public class AppMbAfterSaleController {

    @Resource
    private MbOrderRefundReverseOperationService mbOrderRefundReverseOperationService;

    @PostMapping("/update-refunded")
    @Operation(summary = "更新MB订单为已退款") // 由 pay-module 支付服务，进行回调
    @PermitAll // 无需登录，安全由内部业务逻辑校验实现
    public CommonResult<Boolean> updateOrderRefunded(@RequestBody PayRefundNotifyReqDTO notifyReqDTO) {
        String merchantOrderId = notifyReqDTO.getMerchantOrderId();
        
        log.info("[updateOrderRefunded][收到MB订单退款成功回调: merchantOrderId={}, payRefundId={}]", 
            merchantOrderId, notifyReqDTO.getPayRefundId());

        try {
            // 注意：退款回调能到达这里，说明退款已经成功
            log.info("[updateOrderRefunded][处理MB订单退款成功回调: merchantOrderId={}, payRefundId={}]",
                merchantOrderId, notifyReqDTO.getPayRefundId());

            // 调用退款逆操作服务处理
            boolean result = mbOrderRefundReverseOperationService.processRefundNotification(notifyReqDTO);

            if (result) {
                log.info("[updateOrderRefunded][MB订单退款成功回调处理完成: merchantOrderId={}]", merchantOrderId);
                return success(true);
            } else {
                log.warn("[updateOrderRefunded][MB订单退款成功回调处理失败或跳过: merchantOrderId={}]", merchantOrderId);
                return success(false);
            }
            
        } catch (Exception e) {
            log.error("[updateOrderRefunded][MB订单退款成功回调处理失败: merchantOrderId={}]", 
                merchantOrderId, e);
            // 抛出异常，让支付系统进行重试
            throw new RuntimeException("MB订单退款回调处理失败: " + e.getMessage(), e);
        }
    }
}
