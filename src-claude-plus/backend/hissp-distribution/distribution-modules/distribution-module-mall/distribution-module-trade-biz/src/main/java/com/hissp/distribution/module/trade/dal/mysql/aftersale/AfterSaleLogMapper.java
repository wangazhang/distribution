package com.hissp.distribution.module.trade.dal.mysql.aftersale;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AfterSaleLogMapper extends BaseMapperX<AfterSaleLogDO> {

    default List<AfterSaleLogDO> selectListByAfterSaleId(Long afterSaleId) {
        return selectList(new LambdaQueryWrapper<AfterSaleLogDO>()
                .eq(AfterSaleLogDO::getAfterSaleId, afterSaleId)
                .orderByDesc(AfterSaleLogDO::getCreateTime));
    }

}
