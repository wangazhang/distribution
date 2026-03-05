package com.hissp.distribution.module.product.api.spu;

import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import com.hissp.distribution.module.product.dal.dataobject.spu.ProductSpuDO;
import com.hissp.distribution.module.product.service.spu.ProductSpuService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 商品 SPU API 接口实现类
 *
 * @author LeeYan9
 * @since 2022-09-06
 */
@Service
@Validated
public class ProductSpuApiImpl implements ProductSpuApi {

    @Resource
    private ProductSpuService spuService;

    @Override
    public List<ProductSpuRespDTO> getSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.getSpuList(ids);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

    @Override
    public List<ProductSpuRespDTO> validateSpuList(Collection<Long> ids) {
        List<ProductSpuDO> spus = spuService.validateSpuList(ids);
        return BeanUtils.toBean(spus, ProductSpuRespDTO.class);
    }

    @Override
    public ProductSpuRespDTO getSpu(Long id) {
        ProductSpuDO spu = spuService.getSpu(id);
        return BeanUtils.toBean(spu, ProductSpuRespDTO.class);
    }

    @Override
    public Boolean isNoneMarketingProduct(List<Long> spuIds) {
        return spuService.isNoneMarketingProduct(spuIds);
    }

    @Override
    public Boolean isCareerProduct(List<Long> spuIds) {
        return spuService.isCareerProduct(spuIds);
    }

    @Override
    public Boolean isMbProduct(List<Long> spuIds) {
        return spuService.isMbProduct(spuIds);
    }
}
