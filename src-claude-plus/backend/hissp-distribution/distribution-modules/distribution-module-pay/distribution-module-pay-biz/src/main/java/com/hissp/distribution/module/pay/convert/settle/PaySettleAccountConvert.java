package com.hissp.distribution.module.pay.convert.settle;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountRespVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountRespVO;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PaySettleAccountConvert {

    PaySettleAccountConvert INSTANCE = Mappers.getMapper(PaySettleAccountConvert.class);

    @Mapping(source = "idCardFrontLocalUrl", target = "idCardFrontUrl")
    @Mapping(source = "idCardBackLocalUrl", target = "idCardBackUrl")
    @Mapping(source = "bankCardFrontLocalUrl", target = "bankCardFrontUrl")
    AppSettleAccountRespVO convertApp(PaySettleAccountDO bean);

    SettleAccountRespVO convert(PaySettleAccountDO bean);

    PageResult<SettleAccountRespVO> convertPage(PageResult<PaySettleAccountDO> pageResult);
}
