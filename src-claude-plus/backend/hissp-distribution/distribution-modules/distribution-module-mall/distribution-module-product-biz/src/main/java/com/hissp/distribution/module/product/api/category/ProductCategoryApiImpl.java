package com.hissp.distribution.module.product.api.category;

import com.hissp.distribution.module.product.service.category.ProductCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * 商品分类 API 接口实现类
 *
 * @author owen
 */
@Service
@Validated
public class ProductCategoryApiImpl implements ProductCategoryApi {

    @Resource
    private ProductCategoryService productCategoryService;

    @Override
    public void validateCategoryList(Collection<Long> ids) {
        productCategoryService.validateCategoryList(ids);
    }

    @Override
    public List<Long> getProductCategoryParentPathById(List<Long> ids) {
        return productCategoryService.getProductCategoryParentPathById(ids);
    }

}
