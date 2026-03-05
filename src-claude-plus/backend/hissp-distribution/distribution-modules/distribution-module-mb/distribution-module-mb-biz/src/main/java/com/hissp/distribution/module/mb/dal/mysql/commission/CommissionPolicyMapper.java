package com.hissp.distribution.module.mb.dal.mysql.commission;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicyPageReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommissionPolicyMapper extends BaseMapperX<CommissionPolicyDO> {

    default PageResult<CommissionPolicyDO> selectPage(CommissionPolicyPageReqVO reqVO) {
        return selectPage(reqVO,
            new LambdaQueryWrapperX<CommissionPolicyDO>()
                .likeIfPresent(CommissionPolicyDO::getDisplayName, reqVO.getKeyword())
                .eqIfPresent(CommissionPolicyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CommissionPolicyDO::getBizType, reqVO.getBizType())
                .orderByDesc(CommissionPolicyDO::getUpdateTime));
    }

    default List<CommissionPolicyDO> selectOnlineList() {
        return selectList(new LambdaQueryWrapperX<CommissionPolicyDO>()
            .eq(CommissionPolicyDO::getStatus, "ONLINE")
            .orderByDesc(CommissionPolicyDO::getVersionNo));
    }

    default List<CommissionPolicyDO> selectOnlineListByBizType(String bizType) {
        return selectList(new LambdaQueryWrapperX<CommissionPolicyDO>()
            .eq(CommissionPolicyDO::getStatus, "ONLINE")
            .eq(CommissionPolicyDO::getBizType, bizType)
            .orderByDesc(CommissionPolicyDO::getVersionNo));
    }

    default CommissionPolicyDO selectByCode(String policyCode) {
        return selectOne(new LambdaQueryWrapperX<CommissionPolicyDO>()
            .eq(CommissionPolicyDO::getPolicyCode, policyCode)
            .last("LIMIT 1"));
    }
}
