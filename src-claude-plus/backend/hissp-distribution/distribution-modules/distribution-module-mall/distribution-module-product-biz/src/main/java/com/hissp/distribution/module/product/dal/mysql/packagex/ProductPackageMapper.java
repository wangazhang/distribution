package com.hissp.distribution.module.product.dal.mysql.packagex;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackagePageReqVO;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductPackageMapper extends BaseMapperX<ProductPackageDO> {

    default ProductPackageDO selectEnabledBySpuId(Long spuId) {
        LambdaQueryWrapper<ProductPackageDO> qw = new LambdaQueryWrapper<ProductPackageDO>()
                .eq(ProductPackageDO::getSpuId, spuId)
                .eq(ProductPackageDO::getStatus, 1)
                .eq(ProductPackageDO::getDeleted, false)
                .last("limit 1");
        return selectOne(qw);
    }

    default Long selectEnabledCountBySpuIdExcludeId(Long spuId, Long excludeId) {
        LambdaQueryWrapper<ProductPackageDO> qw = new LambdaQueryWrapper<ProductPackageDO>()
                .eq(ProductPackageDO::getSpuId, spuId)
                .eq(ProductPackageDO::getStatus, 1)
                .eq(ProductPackageDO::getDeleted, false)
                .ne(excludeId != null, ProductPackageDO::getId, excludeId);
        return selectCount(qw);
    }

    default PageResult<ProductPackageDO> selectPage(ProductPackagePageReqVO reqVO) {
        LambdaQueryWrapperX<ProductPackageDO> qw = new LambdaQueryWrapperX<ProductPackageDO>()
                .likeIfPresent(ProductPackageDO::getName, reqVO.getName())
                .eqIfPresent(ProductPackageDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(ProductPackageDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(ProductPackageDO::getCreateTime, reqVO.getCreateTime())
                .eq(ProductPackageDO::getDeleted, false)
                .orderByDesc(ProductPackageDO::getId);
        return selectPage(reqVO, qw);
    }
}

