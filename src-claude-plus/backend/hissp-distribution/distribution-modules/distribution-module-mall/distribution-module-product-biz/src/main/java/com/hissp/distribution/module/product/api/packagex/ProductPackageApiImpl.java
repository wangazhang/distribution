package com.hissp.distribution.module.product.api.packagex;

import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.product.service.packagex.ProductPackageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class ProductPackageApiImpl implements ProductPackageApi {

    @Resource
    private ProductPackageService productPackageService;

    @Override
    public ProductPackageRespDTO getEnabledPackageBySpuId(Long spuId) {
        return productPackageService.getEnabledBySpuId(spuId);
    }

    @Override
    public ProductPackageRespDTO getPackageById(Long packageId) {
        return productPackageService.getById(packageId);
    }
}
