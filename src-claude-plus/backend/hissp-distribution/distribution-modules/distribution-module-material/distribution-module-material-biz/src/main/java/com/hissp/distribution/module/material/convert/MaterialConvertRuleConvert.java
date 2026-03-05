package com.hissp.distribution.module.material.convert;

import com.hissp.distribution.module.material.controller.admin.convert.vo.MaterialConvertRuleRespVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRuleDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface MaterialConvertRuleConvert {

    MaterialConvertRuleConvert INSTANCE = Mappers.getMapper(MaterialConvertRuleConvert.class);

    MaterialConvertRuleRespVO convert(MaterialConvertRuleDO bean);

    List<MaterialConvertRuleRespVO> convertList(List<MaterialConvertRuleDO> list);

}