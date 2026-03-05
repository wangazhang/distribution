package com.hissp.distribution.module.product.api.packagex;

import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;

/**
 * 套包商品查询对外 API（供 trade-biz 等模块调用）
 */
public interface ProductPackageApi {

    /**
     * 按 SPU 查询已启用的套包（若无返回 null）
     * @param spuId 商品 SPU ID
     * @return 已启用的套包配置，或 null
     */
    ProductPackageRespDTO getEnabledPackageBySpuId(Long spuId);

    /**
     * 根据套包ID查询配置（无状态限制）
     *
     * @param packageId 套包ID
     * @return 套包配置，或 null
     */
    ProductPackageRespDTO getPackageById(Long packageId);
}
