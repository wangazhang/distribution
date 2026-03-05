package com.hissp.distribution.module.pay.dal.mysql.request;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.pay.dal.dataobject.request.PayChannelRequestDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 通用渠道请求 Mapper
 *
 * @author Cascade
 */
@Mapper
public interface PayChannelRequestMapper extends BaseMapperX<PayChannelRequestDO> {

    default PayChannelRequestDO selectByReqSeqId(String reqSeqId) {
        return selectOne(PayChannelRequestDO::getReqSeqId, reqSeqId);
    }

    default PayChannelRequestDO selectByOrderId(@Param("taskNo") String taskNo){
        return selectOne(PayChannelRequestDO::getOrderId, taskNo);
    }
}

