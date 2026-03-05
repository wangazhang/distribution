package com.hissp.distribution.module.infra.dal.mysql.third;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.infra.dal.dataobject.third.ThirdCodeAddressDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ThirdCodeAddressMapper extends BaseMapperX<ThirdCodeAddressDO> {

    default List<ThirdCodeAddressDO> selectByLevel(String provider, Integer level) {
        return selectList(new LambdaQueryWrapperX<ThirdCodeAddressDO>()
                .eq(ThirdCodeAddressDO::getProvider, provider)
                .eq(ThirdCodeAddressDO::getLevel, level)
                .eq(ThirdCodeAddressDO::getStatus, 0)
                .orderByAsc(ThirdCodeAddressDO::getSort, ThirdCodeAddressDO::getCode));
    }

    default List<ThirdCodeAddressDO> selectByParent(String provider, String parentCode) {
        return selectList(new LambdaQueryWrapperX<ThirdCodeAddressDO>()
                .eq(ThirdCodeAddressDO::getProvider, provider)
                .eq(ThirdCodeAddressDO::getParentCode, parentCode)
                .eq(ThirdCodeAddressDO::getStatus, 0)
                .orderByAsc(ThirdCodeAddressDO::getSort, ThirdCodeAddressDO::getCode));
    }
}

