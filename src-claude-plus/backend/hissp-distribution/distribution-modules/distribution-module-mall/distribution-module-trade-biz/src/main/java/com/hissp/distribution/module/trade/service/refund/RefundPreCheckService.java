package com.hissp.distribution.module.trade.service.refund;

import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;

import java.util.List;

/**
 * 退款前资源充足性校验服务
 * 后续将校验：物料余额充足、佣金结算状态、权益回收策略等
 */
public interface RefundPreCheckService {

    /**
     * 校验订单在当前退款场景下是否满足资源回退条件
     * 不满足应抛出业务异常
     */
    void validate(TradeOrderDO order, List<TradeOrderItemDO> items);
}

