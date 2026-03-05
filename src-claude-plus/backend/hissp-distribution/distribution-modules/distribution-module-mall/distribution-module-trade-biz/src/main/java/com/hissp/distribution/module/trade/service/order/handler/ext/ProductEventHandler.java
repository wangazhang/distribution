package com.hissp.distribution.module.trade.service.order.handler.ext;

import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.service.order.handler.event.PaymentSuccessHandler;
import com.hissp.distribution.module.trade.service.order.handler.event.RefundSuccessHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 普通商品处理器（占位）
 * 当前仅作为占位，后续可在此处理非套包商品的支付/退款后逻辑
 */
@Slf4j
@Component
public class ProductEventHandler implements PaymentSuccessHandler, RefundSuccessHandler {

    @Resource
    private ProductPackageApi productPackageApi;

    @Override
    public boolean supports(TradeOrderItemDO item) {
        // 非套包商品
        return productPackageApi.getEnabledPackageBySpuId(item.getSpuId()) == null;
    }

    @Override
    public void onPayment(TradeOrderDO order, TradeOrderItemDO item) {
        // TODO: 普通商品支付后逻辑（如需要）
        log.debug("[onPayment][普通商品处理占位 orderId={} itemId={}]", order.getId(), item.getId());
    }

    @Override
    public void onRefund(TradeOrderDO order, TradeOrderItemDO item) {
        // TODO: 普通商品退款后逻辑（如需要）
        log.debug("[onRefund][普通商品处理占位 orderId={} itemId={}]", order.getId(), item.getId());
    }
}

