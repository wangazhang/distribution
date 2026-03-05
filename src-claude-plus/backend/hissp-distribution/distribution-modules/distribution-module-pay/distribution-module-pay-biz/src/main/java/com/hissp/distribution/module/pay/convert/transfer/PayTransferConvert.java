package com.hissp.distribution.module.pay.convert.transfer;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.hissp.distribution.module.pay.controller.admin.demo.vo.transfer.PayDemoTransferCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferPageItemRespVO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferRespVO;
import com.hissp.distribution.module.pay.dal.dataobject.transfer.PayTransferDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayTransferConvert {

    PayTransferConvert INSTANCE = Mappers.getMapper(PayTransferConvert.class);

    PayTransferDO convert(PayTransferCreateReqDTO dto);

    PayTransferUnifiedInnerReqDTO convert2(PayTransferDO dto);

    PayTransferCreateReqDTO convert(PayTransferCreateReqVO vo);

    PayTransferCreateReqDTO convert(PayDemoTransferCreateReqVO vo);

    PayTransferRespVO convert(PayTransferDO bean);

    PageResult<PayTransferPageItemRespVO> convertPage(PageResult<PayTransferDO> pageResult);

}
