package com.hissp.distribution.module.material.convert;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.MaterialBalancePageReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalancePageReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceRespVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialBalanceDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MaterialBalanceConvert {

    MaterialBalanceConvert INSTANCE = Mappers.getMapper(MaterialBalanceConvert.class);

    MaterialBalanceRespVO convert(MaterialBalanceDO bean);

    PageResult<MaterialBalanceRespVO> convertPage(PageResult<MaterialBalanceDO> page);

    MaterialBalancePageReqVO convert(MaterialBalancePageReqDTO pageReqDTO);

    MaterialBalanceRespDTO convertToDto(MaterialBalanceRespVO bean);

    PageResult<MaterialBalanceRespDTO> convertPageToDto(PageResult<MaterialBalanceRespVO> page);

}

