package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.material.dal.dataobject.MaterialOutboundItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 物料出库明细 Mapper
 */
@Mapper
public interface MaterialOutboundItemMapper extends BaseMapperX<MaterialOutboundItemDO> {

    default List<MaterialOutboundItemDO> selectListByOutboundId(Long outboundId) {
        return selectList(MaterialOutboundItemDO::getOutboundId, outboundId);
    }

    default void deleteByOutboundId(Long outboundId) {
        delete(MaterialOutboundItemDO::getOutboundId, outboundId);
    }

}