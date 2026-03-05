package com.hissp.distribution.module.material.convert;

import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialTxnDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MaterialConvert {

    MaterialConvert INSTANCE = Mappers.getMapper(MaterialConvert.class);

    @Mapping(target = "direction", expression = "java(act.getDirection().getValue())")
    MaterialTxnDO convert(MaterialActDTO act);

}

