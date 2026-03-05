package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRulePageReqDTO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 物料转化规则 Mapper
 */
@Mapper
public interface MaterialConvertRuleMapper extends BaseMapperX<MaterialConvertRuleDO> {

    default PageResult<MaterialConvertRuleDO> selectPage(MaterialConvertRulePageReqDTO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialConvertRuleDO>()
                .likeIfPresent(MaterialConvertRuleDO::getRuleName, reqVO.getRuleName())
                .eqIfPresent(MaterialConvertRuleDO::getSourceMaterialId, reqVO.getSourceMaterialId())
                .eqIfPresent(MaterialConvertRuleDO::getTargetMaterialId, reqVO.getTargetMaterialId())
                .eqIfPresent(MaterialConvertRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MaterialConvertRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MaterialConvertRuleDO::getId));
    }

    default MaterialConvertRuleDO selectBySourceAndTarget(Long sourceMaterialId, Long targetMaterialId) {
        return selectOne(new LambdaQueryWrapperX<MaterialConvertRuleDO>()
                .eq(MaterialConvertRuleDO::getSourceMaterialId, sourceMaterialId)
                .eq(MaterialConvertRuleDO::getTargetMaterialId, targetMaterialId));
    }

    default List<MaterialConvertRuleDO> selectActiveBySourceMaterialId(Long sourceMaterialId) {
        return selectList(new LambdaQueryWrapperX<MaterialConvertRuleDO>()
                .eq(MaterialConvertRuleDO::getSourceMaterialId, sourceMaterialId)
                .eq(MaterialConvertRuleDO::getStatus, 1) // 只查询启用的规则
                .orderByAsc(MaterialConvertRuleDO::getId));
    }

}