package com.hissp.distribution.module.pay.dal.mysql.account;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountPageReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaySettleAccountMapper extends BaseMapperX<PaySettleAccountDO> {

    default PaySettleAccountDO selectByUserId(Long userId) {
        return selectOne(PaySettleAccountDO::getUserId, userId);
    }

    default PaySettleAccountDO selectByRequestId(String requestId) {
        return selectOne(PaySettleAccountDO::getRequestId, requestId);
    }

    default PaySettleAccountDO selectBySubMerchantId(String subMerchantId) {
        return selectOne(PaySettleAccountDO::getSubMerchantId, subMerchantId);
    }

    default com.hissp.distribution.framework.common.pojo.PageResult<PaySettleAccountDO> selectPage(
            SettleAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PaySettleAccountDO>()
                .likeIfPresent(PaySettleAccountDO::getSignedName, reqVO.getSignedName())
                .eqIfPresent(PaySettleAccountDO::getMobile, reqVO.getMobile())
                .eqIfPresent(PaySettleAccountDO::getStatus, reqVO.getStatus()));
    }
}
