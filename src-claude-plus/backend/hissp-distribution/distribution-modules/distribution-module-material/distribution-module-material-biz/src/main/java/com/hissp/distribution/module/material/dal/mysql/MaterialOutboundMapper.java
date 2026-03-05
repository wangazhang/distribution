package com.hissp.distribution.module.material.dal.mysql;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.MaterialOutboundPageReqVO;
import com.hissp.distribution.module.material.controller.app.outbound.vo.AppMaterialOutboundPageReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialOutboundDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料出库主 Mapper
 */
@Mapper
public interface MaterialOutboundMapper extends BaseMapperX<MaterialOutboundDO> {

    default PageResult<MaterialOutboundDO> selectPage(MaterialOutboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialOutboundDO>()
                .eqIfPresent(MaterialOutboundDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MaterialOutboundDO::getOutboundNo, reqVO.getOutboundNo())
                .likeIfPresent(MaterialOutboundDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(MaterialOutboundDO::getReceiverMobile, reqVO.getReceiverMobile())
                .eqIfPresent(MaterialOutboundDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MaterialOutboundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MaterialOutboundDO::getId));
    }

    default PageResult<MaterialOutboundDO> selectAppPage(AppMaterialOutboundPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MaterialOutboundDO>()
                .eqIfPresent(MaterialOutboundDO::getUserId, reqVO.getUserId())
                .likeIfPresent(MaterialOutboundDO::getOutboundNo, reqVO.getOutboundNo())
                .likeIfPresent(MaterialOutboundDO::getReceiverName, reqVO.getReceiverName())
                .likeIfPresent(MaterialOutboundDO::getReceiverMobile, reqVO.getReceiverMobile())
                .eqIfPresent(MaterialOutboundDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(MaterialOutboundDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(MaterialOutboundDO::getId));
    }

}