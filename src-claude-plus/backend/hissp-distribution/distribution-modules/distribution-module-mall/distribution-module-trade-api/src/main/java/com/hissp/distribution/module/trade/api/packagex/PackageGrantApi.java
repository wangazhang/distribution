package com.hissp.distribution.module.trade.api.packagex;

/**
 * 套包权益发放对外 API
 */
public interface PackageGrantApi {

    /**
     * 手动发放套包权益
     *
     * @param userId 用户ID
     * @param packageId 套包ID
     * @param operatorId 操作人ID（可能为空）
     * @param bizKey 业务标识
     */
    void grantManual(Long userId, Long packageId, Long operatorId, String bizKey);
}

