package com.hissp.distribution.module.trade.service.order.handler.ext;

import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.service.order.handler.event.RefundSuccessHandler;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 交易分佣退款事件处理器：统一由退款成功事件驱动撤销佣金，避免与同步流程重复。
 */
@Slf4j
@Component
public class BrokerageRefundEventHandler implements RefundSuccessHandler {

    @Resource
    private BrokerageRecordService brokerageRecordService;

    @Override
    public boolean supports(TradeOrderItemDO item) {
        return item != null;
    }

    @Override
    public void onRefund(TradeOrderDO order, TradeOrderItemDO item) {
        if (order == null || order.getBrokerageUserId() == null || item == null) {
            return;
        }
        try {
            brokerageRecordService.cancelBrokerage(BrokerageRecordBizTypeEnum.ORDER_ADD, String.valueOf(item.getId()));
        } catch (Exception ex) {
            log.error("[BrokerageRefundEventHandler][orderId={} itemId={}] 分佣撤销失败",
                    order.getId(), item.getId(), ex);
            throw ex;
        }
    }
}
