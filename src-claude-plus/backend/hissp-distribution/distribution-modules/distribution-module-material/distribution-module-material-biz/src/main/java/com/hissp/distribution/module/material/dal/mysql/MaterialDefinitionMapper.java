package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionPageReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MaterialDefinitionMapper extends BaseMapperX<MaterialDefinitionDO> {

    default PageResult<MaterialDefinitionDO> selectPage(MaterialDefinitionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialDefinitionDO>()
                .likeIfPresent(MaterialDefinitionDO::getName, reqVO.getName())
                .likeIfPresent(MaterialDefinitionDO::getCode, reqVO.getCode())
                .eqIfPresent(MaterialDefinitionDO::getSpuId, reqVO.getSpuId())
                .eqIfPresent(MaterialDefinitionDO::getType, reqVO.getType())
                .eqIfPresent(MaterialDefinitionDO::getStatus, reqVO.getStatus())
                .orderByDesc(MaterialDefinitionDO::getId));
    }

    default MaterialDefinitionDO selectBySpuId(Long spuId) {
        return selectOne(MaterialDefinitionDO::getSpuId, spuId);
    }

    default MaterialDefinitionDO selectByCode(String code) {
        return selectOne(MaterialDefinitionDO::getCode, code);
    }

    default List<MaterialDefinitionDO> selectListByNameLike(String name) {
        return selectList(new LambdaQueryWrapperX<MaterialDefinitionDO>()
                .likeIfPresent(MaterialDefinitionDO::getName, name)
                .orderByDesc(MaterialDefinitionDO::getId));
    }

    /**
     * 根据物料类型和转化状态查询物料列表
     *
     * @param type 物料类型：1-半成品 2-成品
     * @param convertStatus 转化状态：0-不可转化 1-可转化 2-已转化
     * @return 物料列表
     */
    default List<MaterialDefinitionDO> selectByTypeAndConvertStatus(Integer type, Integer convertStatus) {
        return selectList(new LambdaQueryWrapperX<MaterialDefinitionDO>()
                .eqIfPresent(MaterialDefinitionDO::getType, type)
                .eqIfPresent(MaterialDefinitionDO::getConvertStatus, convertStatus)
                .orderByDesc(MaterialDefinitionDO::getId));
    }

    /**
     * 查询最大物料编码(MER格式)
     *
     * @return 最大编码，如 MER0000005
     */
    @Select("SELECT code FROM material_definition WHERE code LIKE 'MER%' ORDER BY code DESC LIMIT 1")
    String selectMaxCode();
}
