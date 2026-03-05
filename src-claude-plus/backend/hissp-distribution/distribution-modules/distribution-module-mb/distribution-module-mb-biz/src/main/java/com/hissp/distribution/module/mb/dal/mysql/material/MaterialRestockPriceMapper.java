package com.hissp.distribution.module.mb.dal.mysql.material;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPricePageReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.material.MaterialRestockPriceDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 补货价格表：记录不同用户等级对应的商品补货价格 Mapper
 *
 * @author azhanga
 */
@Mapper
public interface MaterialRestockPriceMapper extends BaseMapperX<MaterialRestockPriceDO> {

    default PageResult<MaterialRestockPriceDO> selectPage(MaterialRestockPricePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialRestockPriceDO>()
                .eqIfPresent(MaterialRestockPriceDO::getProductId, reqVO.getProductId())
                .eqIfPresent(MaterialRestockPriceDO::getLevelId, reqVO.getLevelId())
                .eqIfPresent(MaterialRestockPriceDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MaterialRestockPriceDO::getCreateTime, reqVO.getCreateTime())
                .orderByAsc(MaterialRestockPriceDO::getProductId)
                .orderByAsc(MaterialRestockPriceDO::getLevelId)
                .orderByDesc(MaterialRestockPriceDO::getId));
    }

    default MaterialRestockPriceDO selectByLevelIdAndProductId(Long levelId, Long productId) {
        return selectOne(new LambdaQueryWrapperX<MaterialRestockPriceDO>()
                .eq(MaterialRestockPriceDO::getLevelId, levelId)
                .eq(MaterialRestockPriceDO::getProductId, productId)
                .eq(MaterialRestockPriceDO::getStatus, 1)); // 只查询启用状态的记录
    }

}
