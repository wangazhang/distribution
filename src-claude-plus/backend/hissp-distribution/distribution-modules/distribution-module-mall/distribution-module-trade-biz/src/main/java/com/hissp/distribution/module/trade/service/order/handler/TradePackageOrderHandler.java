package com.hissp.distribution.module.trade.service.order.handler;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.service.packagex.PackageCommissionService;
import com.hissp.distribution.module.trade.service.packagex.PackageGrantService;
import com.hissp.distribution.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 套包发放与分佣 订单处理器
 * 支付成功后：发放物料/权益 + 二级分佣
 * 取消/退款：回滚未消耗物料 + 撤销佣金
 */
@Slf4j
@Component
public class TradePackageOrderHandler implements TradeOrderHandler {

    @Resource
    private ProductPackageApi productPackageApi;
    @Resource
    private PackageGrantService packageGrantService;
    @Resource
    private PackageCommissionService packageCommissionService;

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (order == null || CollUtil.isEmpty(orderItems)) return;
        for (TradeOrderItemDO item : orderItems) {
            try {
                ProductPackageRespDTO pack = productPackageApi.getEnabledPackageBySpuId(item.getSpuId());
                if (pack == null) continue;
                // 发放物料/权益
                packageGrantService.grantOnPaid(order, item, pack);
                // 分佣
                packageCommissionService.commissionOnPaid(order, item, pack);
            } catch (Exception e) {
                log.error("[afterPayOrder][orderId={} itemId={} 处理套包失败]", order.getId(), item.getId(), e);
            }
        }
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 未支付不处理
        if (order == null || Boolean.FALSE.equals(order.getPayStatus())) return;
        // 过滤掉已售后成功的项
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) return;
        for (TradeOrderItemDO item : orderItems) {
            afterCancelOrderItem(order, item);
        }
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        if (order == null || orderItem == null) return;
        if (TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus().equals(orderItem.getAfterSaleStatus())) {
            log.debug("[afterCancelOrderItem][orderId={} itemId={}] 售后退款场景已交由退款事件处理，跳过同步回滚套包权益/佣金",
                    order.getId(), orderItem.getId());
            return;
        }
        try {
            ProductPackageRespDTO pack = productPackageApi.getEnabledPackageBySpuId(orderItem.getSpuId());
            if (pack == null) return;
            // 回滚物料
            packageGrantService.revokeOnCancel(order, orderItem, pack);
            // 撤销佣金
            packageCommissionService.cancelCommission(order, orderItem, pack);
        } catch (Exception e) {
            log.error("[afterCancelOrderItem][orderId={} itemId={} 回滚套包失败]", order.getId(), orderItem.getId(), e);
        }
    }
}
