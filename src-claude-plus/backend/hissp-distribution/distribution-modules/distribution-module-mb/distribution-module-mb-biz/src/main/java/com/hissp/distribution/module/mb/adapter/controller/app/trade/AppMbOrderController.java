package com.hissp.distribution.module.mb.adapter.controller.app.trade;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderChannelConfirmReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderCreateReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderRespVO;
import com.hissp.distribution.module.mb.domain.service.mbdt.MbBizOperationService;
import com.hissp.distribution.module.mb.domain.service.trade.MbOrderService;
import com.hissp.distribution.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * MB订单支付回调Controller
 * 处理MB业务的支付成功回调，包括补货订单和物料转化订单
 *
 * @author system
 */
@Tag(name = "用户 App - MB订单")
@RestController
@RequestMapping("/mb/order")
@Validated
@Slf4j
public class AppMbOrderController {

    @Resource
    private MbBizOperationService mbBizOperationService;

    @Resource
    private MbOrderService mbOrderService;


    @PostMapping("/create-order")
    @Operation(summary = "创建订单")
    public CommonResult<Long> createMaterialRestockOrder(@Valid @RequestBody AppMbOrderCreateReqVO createReqVO) {
        // 设置当前登录用户ID
        createReqVO.setAgentUserId(getLoginUserId());
        // 创建订单
        Long orderId = mbOrderService.createUserMaterialRestockOrder(createReqVO);
        return success(orderId);
    }

    @GetMapping("/order-page")
    @Operation(summary = "获取用户订单分页列表")
    public CommonResult<PageResult<AppMbOrderRespVO>> getUserMaterialOrderPage(@Valid AppMbOrderPageReqVO pageReqVO) {
        // 设置当前登录用户ID
        pageReqVO.setAgentUserId(getLoginUserId());
        // 查询订单分页列表
        PageResult<AppMbOrderRespVO> pageResult = mbOrderService.getUserMaterialOrderPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/get")
    @Operation(summary = "获取订单详情")
    public CommonResult<AppMbOrderRespVO> getOrderDetail(@RequestParam("id") Long orderId) {
        // 获取订单详情
        AppMbOrderRespVO orderDetail = mbOrderService.getUserMaterialOrderDetail(orderId, getLoginUserId());
        return success(orderDetail);
    }

    @PutMapping("/receive")
    @Operation(summary = "确认收货")
    public CommonResult<Boolean> receiveOrder(@RequestParam("id") Long orderId) {
        mbOrderService.receiveOrderByMember(getLoginUserId(), orderId);
        return success(Boolean.TRUE);
    }

    @PostMapping("/check-channel-confirm")
    @Operation(summary = "补偿校验渠道确认收货结果")
    public CommonResult<Boolean> checkChannelConfirm(@Valid @RequestBody AppMbOrderChannelConfirmReqVO reqVO) {
        return success(mbOrderService.checkOrderReceiveStatus(reqVO.getOrderId()));
    }

    @GetMapping("/status-list")
    @Operation(summary = "获取订单状态列表")
    public CommonResult<List<Map<String, Object>>> getOrderStatusList() {
        List<Map<String, Object>> statusList = mbOrderService.getOrderStatusList();
        return success(statusList);
    }


    @PostMapping("/update-paid")
    @Operation(summary = "更新MB订单为已支付") // 由 pay-module 支付服务，进行回调
    @PermitAll // 无需登录，安全由内部业务逻辑校验实现
    public CommonResult<Boolean> updateOrderPaid(@RequestBody PayOrderNotifyReqDTO notifyReqDTO) {
        String merchantOrderId = notifyReqDTO.getMerchantOrderId();
        
        log.info("[updateOrderPaid][收到MB订单支付成功回调: merchantOrderId={}, payOrderId={}]", 
            merchantOrderId, notifyReqDTO.getPayOrderId());

        try {
            // 注意：支付回调能到达这里，说明支付已经成功
            // PayOrderNotifyReqDTO的status字段可能为null，不需要验证
            log.info("[updateOrderPaid][处理MB订单支付成功回调: merchantOrderId={}, status={}]",
                merchantOrderId, notifyReqDTO.getStatus());

            // 根据订单号前缀处理不同类型的订单
            if (merchantOrderId != null && merchantOrderId.startsWith("MR")) {
                // 处理物料补货订单
                log.info("[updateOrderPaid][处理物料补货订单: {}]", merchantOrderId);
                mbBizOperationService.handleRestockPaymentSuccess(notifyReqDTO);
                
            } else if (merchantOrderId != null && merchantOrderId.startsWith("MC")) {
                // 处理物料转化订单
                log.info("[updateOrderPaid][处理物料转化订单: {}]", merchantOrderId);
                mbBizOperationService.handleMaterialConvertPaymentSuccess(notifyReqDTO);
                
            } else {
                log.warn("[updateOrderPaid][未知的MB订单类型: {}]", merchantOrderId);
                return success(false);
            }

            log.info("[updateOrderPaid][MB订单支付成功回调处理完成: {}]", merchantOrderId);
            return success(true);
            
        } catch (Exception e) {
            log.error("[updateOrderPaid][MB订单支付成功回调处理失败: merchantOrderId={}]", 
                merchantOrderId, e);
            // 抛出异常，让支付系统进行重试
            throw new RuntimeException("MB订单支付回调处理失败: " + e.getMessage(), e);
        }
    }
}
