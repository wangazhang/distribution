package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnPageReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialTxnDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialTxnMapper extends BaseMapperX<MaterialTxnDO> {

    default PageResult<MaterialTxnDO> selectPage(MaterialTxnPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialTxnDO>()
                .eqIfPresent(MaterialTxnDO::getUserId, reqVO.getUserId())
                .eqIfPresent(MaterialTxnDO::getMaterialId, reqVO.getMaterialId())
                .eqIfPresent(MaterialTxnDO::getDirection, reqVO.getDirection())
                .likeIfPresent(MaterialTxnDO::getBizKey, reqVO.getBizKey())
                .orderByDesc(MaterialTxnDO::getId));
    }
}

