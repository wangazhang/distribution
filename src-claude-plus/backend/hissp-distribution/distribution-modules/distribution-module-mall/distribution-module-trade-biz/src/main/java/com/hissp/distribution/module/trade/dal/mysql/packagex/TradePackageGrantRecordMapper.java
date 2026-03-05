package com.hissp.distribution.module.trade.dal.mysql.packagex;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.trade.dal.dataobject.packagex.TradePackageGrantRecordDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TradePackageGrantRecordMapper extends BaseMapperX<TradePackageGrantRecordDO> {

    default TradePackageGrantRecordDO selectByOrderItemId(Long orderItemId) {
        LambdaQueryWrapper<TradePackageGrantRecordDO> qw = new LambdaQueryWrapper<TradePackageGrantRecordDO>()
                .eq(TradePackageGrantRecordDO::getOrderItemId, orderItemId)
                .eq(TradePackageGrantRecordDO::getDeleted, false)
                .last("limit 1");
        return selectOne(qw);
    }
}

