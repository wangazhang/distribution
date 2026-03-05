package com.hissp.distribution.module.trade.controller.admin.aftersale;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;
import com.hissp.distribution.module.trade.controller.admin.aftersale.vo.*;
import com.hissp.distribution.module.trade.convert.aftersale.AfterSaleConvert;
import com.hissp.distribution.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import com.hissp.distribution.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.message.TradeOrderRefundSuccessMessage;
import com.hissp.distribution.module.trade.mq.producer.TradeOrderEventProducer;
import com.hissp.distribution.module.trade.service.aftersale.AfterSaleLogService;
import com.hissp.distribution.module.trade.service.aftersale.AfterSaleService;
import com.hissp.distribution.module.trade.service.order.TradeOrderQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hissp.distribution.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 售后订单")
@RestController
@RequestMapping("/trade/after-sale")
@Validated
@Slf4j
public class AfterSaleController {

    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;
    @Resource
    private AfterSaleLogService afterSaleLogService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private TradeOrderEventProducer tradeOrderEventProducer;

    @GetMapping("/page")
    @Operation(summary = "获得售后订单分页")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:query')")
    public CommonResult<PageResult<AfterSaleRespPageItemVO>> getAfterSalePage(@Valid AfterSalePageReqVO pageVO) {
        // 查询售后
        PageResult<AfterSaleDO> pageResult = afterSaleService.getAfterSalePage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty());
        }

        // 查询会员
        Map<Long, MemberUserRespDTO> memberUsers = memberUserApi.getUserMap(
                convertSet(pageResult.getList(), AfterSaleDO::getUserId));
        return success(AfterSaleConvert.INSTANCE.convertPage(pageResult, memberUsers));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得售后订单详情")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:query')")
    public CommonResult<AfterSaleDetailRespVO> getOrderDetail(@RequestParam("id") Long id) {
        // 查询订单
        AfterSaleDO afterSale = afterSaleService.getAfterSale(id);
        if (afterSale == null) {
            return success(null);
        }

        // 查询订单
        TradeOrderDO order = tradeOrderQueryService.getOrder(afterSale.getOrderId());
        // 查询订单项
        TradeOrderItemDO orderItem = tradeOrderQueryService.getOrderItem(afterSale.getOrderItemId());
        // 拼接数据
        MemberUserRespDTO user = memberUserApi.getUser(afterSale.getUserId());
        List<AfterSaleLogDO> logs = afterSaleLogService.getAfterSaleLogList(afterSale.getId());
        return success(AfterSaleConvert.INSTANCE.convert(afterSale, order, orderItem, user, logs));
    }

    @PutMapping("/agree")
    @Operation(summary = "同意售后")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:agree')")
    public CommonResult<Boolean> agreeAfterSale(@RequestParam("id") Long id) {
        afterSaleService.agreeAfterSale(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/disagree")
    @Operation(summary = "拒绝售后")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:disagree')")
    public CommonResult<Boolean> disagreeAfterSale(@RequestBody AfterSaleDisagreeReqVO confirmReqVO) {
        afterSaleService.disagreeAfterSale(getLoginUserId(), confirmReqVO);
        return success(true);
    }

    @PutMapping("/receive")
    @Operation(summary = "确认收货")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:receive')")
    public CommonResult<Boolean> receiveAfterSale(@RequestParam("id") Long id) {
        afterSaleService.receiveAfterSale(getLoginUserId(), id);
        return success(true);
    }

    @PutMapping("/refuse")
    @Operation(summary = "拒绝收货")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:receive')")
    public CommonResult<Boolean> refuseAfterSale(AfterSaleRefuseReqVO refuseReqVO) {
        afterSaleService.refuseAfterSale(getLoginUserId(), refuseReqVO);
        return success(true);
    }

    @PutMapping("/refund")
    @Operation(summary = "确认退款")
    @Parameter(name = "id", description = "售后编号", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('trade:after-sale:refund')")
    public CommonResult<Boolean> refundAfterSale(@RequestParam("id") Long id) {
        afterSaleService.refundAfterSale(getLoginUserId(), getClientIP(), id);
        return success(true);
    }

    @PostMapping("/update-refunded")
    @Operation(summary = "更新售后订单为已退款") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录，安全由 PayDemoOrderService 内部校验实现
    public CommonResult<Boolean> updateAfterRefund(@RequestBody PayRefundNotifyReqDTO notifyReqDTO) {
        log.info("[updateAfterRefund][收到退款通知: {}]", notifyReqDTO);

        try {
            // 1. 基本参数校验
            String merchantOrderId = notifyReqDTO.getMerchantOrderId();
            if (merchantOrderId == null || merchantOrderId.trim().isEmpty()) {
                log.warn("[updateAfterRefund][商户订单号为空，跳过处理]");
                return success(false);
            }

            // 2. 查询订单信息
            Long orderId = Long.parseLong(merchantOrderId);
            TradeOrderDO order = tradeOrderQueryService.getOrder(orderId);
            if (order == null) {
                log.warn("[updateAfterRefund][订单不存在: {}]", orderId);
                return success(false);
            }

            // 3. 发布订单退款成功消息，让各个业务模块自行处理
            publishOrderRefundSuccessMessage(order, notifyReqDTO);

            log.info("[updateAfterRefund][订单退款通知处理完成: orderId={}]", orderId);
            return success(true);

        } catch (NumberFormatException e) {
            log.error("[updateAfterRefund][解析订单ID失败: {}]", notifyReqDTO.getMerchantOrderId(), e);
            return success(false);
        } catch (Exception e) {
            log.error("[updateAfterRefund][处理退款通知异常: {}]", notifyReqDTO, e);
            throw new RuntimeException("退款通知处理失败: " + e.getMessage(), e);
        }
    }

    /**
     * 发布订单退款成功消息
     * 让各个业务模块（如MB模块）自行订阅处理
     */
    private void publishOrderRefundSuccessMessage(TradeOrderDO order, PayRefundNotifyReqDTO notifyReqDTO) {
            // 构建退款成功消息
            TradeOrderRefundSuccessMessage message = TradeOrderRefundSuccessMessage.builder()
                .orderId(order.getId())
                .orderNo(order.getNo())
                .userId(order.getUserId())
                .payRefundId(notifyReqDTO.getPayRefundId())
                .merchantOrderId(notifyReqDTO.getMerchantOrderId())
                .refundTime(LocalDateTime.now())
                .build();

            // 发布消息到MQ
            tradeOrderEventProducer.sendRefundSuccessMessage(message);

            log.info("[publishOrderRefundSuccessMessage][发布订单退款成功消息: orderId={}, payRefundId={}]",
                order.getId(), notifyReqDTO.getPayRefundId());

    }

}
