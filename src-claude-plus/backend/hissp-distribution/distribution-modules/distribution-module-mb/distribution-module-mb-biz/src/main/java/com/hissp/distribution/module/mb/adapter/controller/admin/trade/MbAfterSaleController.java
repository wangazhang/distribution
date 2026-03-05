package com.hissp.distribution.module.mb.adapter.controller.admin.trade;

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
 * 处理MB业务的退款成功回调
 *
 * @author system
 */
@Tag(name = "管理后台 - MB售后退款回调")
@RestController
@RequestMapping("/mb/after-sale")
@Validated
@Slf4j
public class MbAfterSaleController {

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
            // 注意：PayRefundNotifyReqDTO 没有status字段，退款回调默认表示退款成功
            // 如果需要验证退款状态，需要通过payRefundId查询退款单详情
            log.info("[updateOrderRefunded][处理退款成功回调: merchantOrderId={}, payRefundId={}]",
                merchantOrderId, notifyReqDTO.getPayRefundId());

            // 检查订单号是否为空
            if (merchantOrderId == null || merchantOrderId.trim().isEmpty()) {
                log.warn("[updateOrderRefunded][商户订单号为空，跳过处理]");
                return success(false);
            }

            // 直接调用集成的退款处理服务
            boolean result = mbOrderRefundReverseOperationService.processRefundNotification(notifyReqDTO);

            if (!result) {
                log.warn("[updateOrderRefunded][MB订单退款处理失败或跳过: {}]", merchantOrderId);
                return success(false);
            }

            log.info("[updateOrderRefunded][MB订单退款成功回调处理完成: {}]", merchantOrderId);
            return success(true);
            
        } catch (Exception e) {
            log.error("[updateOrderRefunded][MB订单退款成功回调处理失败: merchantOrderId={}]", 
                merchantOrderId, e);
            // 抛出异常，让支付系统进行重试
            throw new RuntimeException("MB订单退款回调处理失败: " + e.getMessage(), e);
        }
    }


}
