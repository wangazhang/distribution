package com.hissp.distribution.module.trade.api.packagex;

import com.hissp.distribution.module.trade.service.packagex.PackageGrantService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 套包权益发放 API 实现
 */
@Service
public class PackageGrantApiImpl implements PackageGrantApi {

    @Resource
    private PackageGrantService packageGrantService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantManual(Long userId, Long packageId, Long operatorId, String bizKey) {
        packageGrantService.grantManual(userId, packageId, operatorId, bizKey);
    }
}

