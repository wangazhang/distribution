package com.hissp.distribution.module.product.service.packagex;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackagePageReqVO;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackageRespVO;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackageSaveReqVO;

public interface AdminProductPackageService {

    Long create(ProductPackageSaveReqVO reqVO);

    void update(ProductPackageSaveReqVO reqVO);

    void updateStatus(Long id, Integer status);

    void delete(Long id);

    ProductPackageRespVO get(Long id);

    PageResult<ProductPackageRespVO> page(ProductPackagePageReqVO reqVO);

    ProductPackageRespVO getBySpu(Long spuId);
}

