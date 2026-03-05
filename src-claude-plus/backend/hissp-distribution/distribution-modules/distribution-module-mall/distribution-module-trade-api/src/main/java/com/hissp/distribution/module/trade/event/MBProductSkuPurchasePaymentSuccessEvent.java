//package com.hissp.distribution.module.trade.event;
//
//import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
//import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
//import lombok.Getter;
//
//import java.util.List;
//
///**
// * MB产品购买支付成功事件
// * 用于处理普通产品购买后的业务逻辑，如减少上级的物料库存等
// *
// * @author system
// */
//@Getter
//public class MBProductSkuPurchasePaymentSuccessEvent {
//
//    /**
//     * 订单信息
//     */
//    private final TradeOrderRespDTO order;
//
//    /**
//     * 订单项列表
//     */
//    private final List<TradeOrderItemRespDTO> orderItems;
//
//    public MBProductSkuPurchasePaymentSuccessEvent(TradeOrderRespDTO order, List<TradeOrderItemRespDTO> orderItems) {
//        this.order = order;
//        this.orderItems = orderItems;
//    }
//}