package com.hissp.distribution.module.product.service.packagex;

import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;

public interface ProductPackageService {

    /**
     * 查询某 SPU 是否存在启用中的套包，并返回完整配置
     */
    ProductPackageRespDTO getEnabledBySpuId(Long spuId);

    /**
     * 根据套包ID查询完整配置
     */
    ProductPackageRespDTO getById(Long packageId);
}
