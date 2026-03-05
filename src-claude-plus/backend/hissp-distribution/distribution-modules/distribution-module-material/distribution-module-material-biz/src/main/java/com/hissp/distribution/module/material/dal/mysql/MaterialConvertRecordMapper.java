package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRecordPageReqDTO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料转化记录 Mapper
 */
@Mapper
public interface MaterialConvertRecordMapper extends BaseMapperX<MaterialConvertRecordDO> {

    default PageResult<MaterialConvertRecordDO> selectPage(MaterialConvertRecordPageReqDTO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialConvertRecordDO>()
                .eqIfPresent(MaterialConvertRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MaterialConvertRecordDO::getRuleId, reqVO.getRuleId())
                .eqIfPresent(MaterialConvertRecordDO::getSourceMaterialId, reqVO.getSourceMaterialId())
                .eqIfPresent(MaterialConvertRecordDO::getTargetMaterialId, reqVO.getTargetMaterialId())
                .eqIfPresent(MaterialConvertRecordDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MaterialConvertRecordDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MaterialConvertRecordDO::getId));
    }

    default long selectCountByRuleId(Long ruleId) {
        return selectCount(new LambdaQueryWrapperX<MaterialConvertRecordDO>()
                .eq(MaterialConvertRecordDO::getRuleId, ruleId));
    }

}