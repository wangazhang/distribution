package com.hissp.distribution.module.trade.service.order.handler.event;

import com.hissp.distribution.framework.idempotency.core.IdempotencyService;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeOrderEventProcessService {

    private final List<PaymentSuccessHandler> paymentHandlers;
    private final List<RefundSuccessHandler> refundHandlers;
    private final IdempotencyService idempotencyService;

    public void processPaymentItem(TradeOrderDO order, TradeOrderItemDO item, String msgId) {
        String consumerGroup = "trade-payment-success-dispatcher";
        String businessKey = String.format("payment:%d", item.getId());

        if (!idempotencyService.checkAndRecord(msgId, consumerGroup, businessKey)) {
            log.info("[processPaymentItem][msgId({})] 订单项({})已处理，无需重复消费", msgId, item.getId());
            return;
        }

        for (PaymentSuccessHandler handler : paymentHandlers) {
            if (handler.supports(item)) {
                handler.onPayment(order, item);
            }
        }
        idempotencyService.markSuccessById(msgId, consumerGroup);
    }

    public void processRefundItem(TradeOrderDO order, TradeOrderItemDO item, String msgId) {
        String consumerGroup = "trade-refund-success-dispatcher";
        String businessKey = String.format("refund:%d", item.getId());

        if (!idempotencyService.checkAndRecord(msgId, consumerGroup, businessKey)) {
            log.info("[processRefundItem][msgId({})] 订单项({})已处理，无需重复消费", msgId, item.getId());
            return;
        }

        for (RefundSuccessHandler handler : refundHandlers) {
            if (handler.supports(item)) {
                handler.onRefund(order, item);
            }
        }
        idempotencyService.markSuccessById(msgId, consumerGroup);
    }
}

