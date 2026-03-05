package com.hissp.distribution.module.mb.convert.homeconfig;

import com.hissp.distribution.module.mb.adapter.controller.app.homeconfig.vo.MbHomeConfigRespVO;
import com.hissp.distribution.module.mb.dal.dataobject.homeconfig.MbHomeConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 首页配置 Convert
 */
@Mapper
public interface MbHomeConfigConvert {

    MbHomeConfigConvert INSTANCE = Mappers.getMapper(MbHomeConfigConvert.class);

    MbHomeConfigRespVO convert(MbHomeConfigDO bean);
} 