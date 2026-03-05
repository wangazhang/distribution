package com.hissp.distribution.module.material.convert;

import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundRespVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialInboundDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MaterialInboundConvert {

    MaterialInboundConvert INSTANCE = Mappers.getMapper(MaterialInboundConvert.class);

    MaterialInboundDO convert(MaterialInboundCreateReqVO bean);

    MaterialInboundRespVO convert(MaterialInboundDO bean);

    List<MaterialInboundRespVO> convertList(List<MaterialInboundDO> list);

}