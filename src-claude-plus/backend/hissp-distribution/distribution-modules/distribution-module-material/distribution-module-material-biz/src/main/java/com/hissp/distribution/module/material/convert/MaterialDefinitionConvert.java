package com.hissp.distribution.module.material.convert;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionRespVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionUpdateReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MaterialDefinitionConvert {

    MaterialDefinitionConvert INSTANCE = Mappers.getMapper(MaterialDefinitionConvert.class);

    MaterialDefinitionDO convert(MaterialDefinitionCreateReqVO bean);

    MaterialDefinitionDO convert(MaterialDefinitionUpdateReqVO bean);

    MaterialDefinitionRespVO convert(MaterialDefinitionDO bean);

    PageResult<MaterialDefinitionRespVO> convertPage(PageResult<MaterialDefinitionDO> page);

}

