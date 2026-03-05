package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalancePageReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialBalanceDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface MaterialBalanceMapper extends BaseMapperX<MaterialBalanceDO> {

    default MaterialBalanceDO selectByUserIdAndMaterialId(Long userId, Long materialId) {
        return selectOne(new LambdaQueryWrapperX<MaterialBalanceDO>()
                .eq(MaterialBalanceDO::getUserId, userId)
                .eq(MaterialBalanceDO::getMaterialId, materialId));
    }

    default List<MaterialBalanceDO> selectListByUserIdAndMaterialIds(Long userId, Collection<Long> materialIds) {
        return selectList(new LambdaQueryWrapperX<MaterialBalanceDO>()
                .eq(MaterialBalanceDO::getUserId, userId)
                .in(MaterialBalanceDO::getMaterialId, materialIds));
    }

    default PageResult<MaterialBalanceDO> selectPage(MaterialBalancePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialBalanceDO>()
                .eqIfPresent(MaterialBalanceDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MaterialBalanceDO::getMaterialId, reqVO.getMaterialId())
                .orderByDesc(MaterialBalanceDO::getId));
    }

    @Select("SELECT COUNT(DISTINCT user_id) FROM material_balance WHERE available_balance > 0")
    Long selectCountWithBalance();

    @Select("SELECT COUNT(DISTINCT material_id) FROM material_balance")
    Long selectDistinctMaterialCount();

    @Select("SELECT IFNULL(SUM(available_balance), 0) FROM material_balance")
    Long selectTotalBalance();

}

