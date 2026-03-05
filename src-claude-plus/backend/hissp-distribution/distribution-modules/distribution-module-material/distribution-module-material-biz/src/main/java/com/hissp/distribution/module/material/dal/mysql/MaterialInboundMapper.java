package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundPageReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialInboundDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MaterialInboundMapper extends BaseMapperX<MaterialInboundDO> {

    default PageResult<MaterialInboundDO> selectPage(MaterialInboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialInboundDO>()
                .likeIfPresent(MaterialInboundDO::getInboundNo, reqVO.getInboundNo())
                .eqIfPresent(MaterialInboundDO::getApplicantId, reqVO.getApplicantId())
                .eqIfPresent(MaterialInboundDO::getMaterialId, reqVO.getMaterialId())
                .eqIfPresent(MaterialInboundDO::getStatus, reqVO.getStatus())
                .orderByDesc(MaterialInboundDO::getId));
    }

}