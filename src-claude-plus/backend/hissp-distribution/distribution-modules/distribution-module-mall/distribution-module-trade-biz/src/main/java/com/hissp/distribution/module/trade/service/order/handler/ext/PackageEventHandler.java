package com.hissp.distribution.module.trade.service.order.handler.ext;

import java.util.List;

import org.springframework.stereotype.Component;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.service.conversion.MaterialConversionService;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.service.order.handler.event.PaymentSuccessHandler;
import com.hissp.distribution.module.trade.service.order.handler.event.RefundSuccessHandler;
import com.hissp.distribution.module.trade.service.packagex.PackageCommissionService;
import com.hissp.distribution.module.trade.service.packagex.PackageGrantService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 套包领域处理器：
 * 1. 支付成功后，发放物料/权益、计算分佣
 * 2. 退款成功后，回滚物料、撤销分佣
 */
@Slf4j
@Component
public class PackageEventHandler implements PaymentSuccessHandler, RefundSuccessHandler {

    @Resource
    private ProductPackageApi productPackageApi;
    @Resource
    private PackageGrantService packageGrantService; // For entitlements
    @Resource
    private PackageCommissionService packageCommissionService;
    @Resource
    private MaterialConversionService materialConversionService;
    @Resource
    private MaterialApi materialApi;

    @Override
    public boolean supports(TradeOrderItemDO item) {
        return productPackageApi.getEnabledPackageBySpuId(item.getSpuId()) != null;
    }

    private TradeOrderRespDTO adaptOrder(TradeOrderDO order) {
        TradeOrderRespDTO dto = new TradeOrderRespDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setNo(order.getNo());
        return dto;
    }

    private TradeOrderItemRespDTO adaptItem(TradeOrderItemDO item) {
        TradeOrderItemRespDTO dto = new TradeOrderItemRespDTO();
        dto.setId(item.getId());
        dto.setUserId(item.getUserId());
        dto.setOrderId(item.getOrderId());
        dto.setSpuId(item.getSpuId());
        dto.setCount(item.getCount());
        return dto;
    }

    @Override
    public void onPayment(TradeOrderDO order, TradeOrderItemDO item) {
        ProductPackageRespDTO pack = productPackageApi.getEnabledPackageBySpuId(item.getSpuId());
        if (pack == null) {
            return;
        }
        log.info("[onPayment][套包处理 orderId={} itemId={}]", order.getId(), item.getId());

        // 1. 发放物料 (via new material domain)
        log.info("[onPayment][开始发放物料 orderId={} itemId={}]", order.getId(), item.getId());
        List<MaterialActDTO> acts = materialConversionService.convert(adaptOrder(order), adaptItem(item), MaterialActDirectionEnum.IN);
        materialApi.applyActs(acts);
        log.info("[onPayment][完成发放物料 orderId={} itemId={}]", order.getId(), item.getId());

        // 2. 发放权益 (still via PackageGrantService)
        log.info("[onPayment][开始发放权益 orderId={} itemId={}]", order.getId(), item.getId());
        packageGrantService.grantOnPaid(order, item, pack);
        log.info("[onPayment][完成发放权益 orderId={} itemId={}]", order.getId(), item.getId());

        // 3. 分佣
        log.info("[onPayment][开始计算分佣 orderId={} itemId={}]", order.getId(), item.getId());
        packageCommissionService.commissionOnPaid(order, item, pack);
        log.info("[onPayment][完成计算分佣 orderId={} itemId={}]", order.getId(), item.getId());
    }

    @Override
    public void onRefund(TradeOrderDO order, TradeOrderItemDO item) {
        ProductPackageRespDTO pack = productPackageApi.getEnabledPackageBySpuId(item.getSpuId());
        if (pack == null) {
            return;
        }
        log.info("[onRefund][套包处理 orderId={} itemId={}]", order.getId(), item.getId());

        // 1. 回滚物料 (via new material domain)
        log.info("[onRefund][开始回滚物料 orderId={} itemId={}]", order.getId(), item.getId());
        List<MaterialActDTO> acts = materialConversionService.convert(adaptOrder(order), adaptItem(item), MaterialActDirectionEnum.OUT);
        materialApi.applyActs(acts);
        log.info("[onRefund][完成回滚物料 orderId={} itemId={}]", order.getId(), item.getId());

        // 2. 回滚权益 (still via PackageGrantService)
        log.info("[onRefund][开始回滚权益 orderId={} itemId={}]", order.getId(), item.getId());
        packageGrantService.revokeOnCancel(order, item, pack);
        log.info("[onRefund][完成回滚权益 orderId={} itemId={}]", order.getId(), item.getId());

        // 3. 撤销佣金
        log.info("[onRefund][开始撤销佣金 orderId={} itemId={}]", order.getId(), item.getId());
        packageCommissionService.cancelCommission(order, item, pack);
        log.info("[onRefund][完成撤销佣金 orderId={} itemId={}]", order.getId(), item.getId());
    }
}

