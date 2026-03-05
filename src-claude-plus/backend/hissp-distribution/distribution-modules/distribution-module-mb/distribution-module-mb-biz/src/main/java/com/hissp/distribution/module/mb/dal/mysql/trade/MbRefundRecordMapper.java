package com.hissp.distribution.module.mb.dal.mysql.trade;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbRefundRecordDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * MB业务退款记录 Mapper
 *
 * @author system
 */
@Mapper
public interface MbRefundRecordMapper extends BaseMapperX<MbRefundRecordDO> {

    /**
     * 根据商户订单号查询退款记录
     *
     * @param merchantOrderId 商户订单号
     * @return 退款记录
     */
    default MbRefundRecordDO selectByMerchantOrderId(String merchantOrderId) {
        return selectOne(MbRefundRecordDO::getMerchantOrderId, merchantOrderId);
    }

    /**
     * 根据支付退款ID查询退款记录
     *
     * @param payRefundId 支付退款ID
     * @return 退款记录
     */
    default MbRefundRecordDO selectByPayRefundId(Long payRefundId) {
        return selectOne(MbRefundRecordDO::getPayRefundId, payRefundId);
    }
}
