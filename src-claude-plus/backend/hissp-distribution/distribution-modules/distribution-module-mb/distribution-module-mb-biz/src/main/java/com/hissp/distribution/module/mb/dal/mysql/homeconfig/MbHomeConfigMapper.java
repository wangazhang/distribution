package com.hissp.distribution.module.mb.dal.mysql.homeconfig;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.mb.dal.dataobject.homeconfig.MbHomeConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医美首页配置 Mapper
 */
@Mapper
public interface MbHomeConfigMapper extends BaseMapperX<MbHomeConfigDO> {

    default MbHomeConfigDO selectByVersion(String version) {
        return selectOne(new LambdaQueryWrapperX<MbHomeConfigDO>()
                .eq(MbHomeConfigDO::getVersion, version));
    }
    
    default MbHomeConfigDO selectOneByStatus(Integer status) {
        return selectOne(new LambdaQueryWrapperX<MbHomeConfigDO>()
                .eq(MbHomeConfigDO::getStatus, status));
    }
} 