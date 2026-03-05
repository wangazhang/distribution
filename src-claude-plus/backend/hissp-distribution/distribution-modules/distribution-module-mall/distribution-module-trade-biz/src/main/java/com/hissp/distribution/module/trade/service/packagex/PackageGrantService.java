package com.hissp.distribution.module.trade.service.packagex;

import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;

public interface PackageGrantService {

    void grantOnPaid(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack);

    void revokeOnCancel(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack);

    /**
     * 手动发放套包权益
     *
     * @param userId 用户ID
     * @param packageId 套包ID
     * @param operatorId 操作人ID
     * @param bizKey 业务标识（用于日志）
     */
    void grantManual(Long userId, Long packageId, Long operatorId, String bizKey);
}
