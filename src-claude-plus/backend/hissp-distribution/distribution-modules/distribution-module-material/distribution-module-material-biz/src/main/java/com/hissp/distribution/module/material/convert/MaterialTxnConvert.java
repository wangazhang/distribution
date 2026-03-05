package com.hissp.distribution.module.material.convert;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.MaterialTxnPageReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialTxnRespDTO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnPageReqVO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnRespVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialTxnDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MaterialTxnConvert {

    MaterialTxnConvert INSTANCE = Mappers.getMapper(MaterialTxnConvert.class);

    MaterialTxnRespVO convert(MaterialTxnDO bean);

    MaterialTxnRespDTO convertToDto(MaterialTxnDO bean);

    PageResult<MaterialTxnRespVO> convertPage(PageResult<MaterialTxnDO> page);

    MaterialTxnPageReqVO convert(MaterialTxnPageReqDTO pageReqDTO);

    MaterialTxnRespDTO convertToDto(MaterialTxnRespVO bean);

    PageResult<MaterialTxnRespDTO> convertPageToDto(PageResult<MaterialTxnRespVO> page);

}

