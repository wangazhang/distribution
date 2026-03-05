package com.hissp.distribution.module.product.dal.mysql.packagex;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageCommissionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductPackageCommissionMapper extends BaseMapperX<ProductPackageCommissionDO> {

    default List<ProductPackageCommissionDO> selectByPackageId(Long packageId) {
        return selectList(ProductPackageCommissionDO::getPackageId, packageId);
    }

    @Delete("DELETE FROM product_package_commission WHERE package_id = #{packageId}")
    void physicalDeleteByPackageId(@Param("packageId") Long packageId);
}

