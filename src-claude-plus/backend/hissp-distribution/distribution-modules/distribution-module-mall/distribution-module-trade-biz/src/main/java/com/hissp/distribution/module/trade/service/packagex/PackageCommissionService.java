package com.hissp.distribution.module.trade.service.packagex;

import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;

public interface PackageCommissionService {

    void commissionOnPaid(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack);

    void cancelCommission(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack);
}

