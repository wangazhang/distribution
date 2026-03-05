package com.hissp.distribution.module.pay.convert.settle;

import com.hissp.distribution.module.pay.api.settle.dto.PaySettleAccountRespDTO;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * API 层提现账户对象转换。
 */
@Mapper
public interface PaySettleAccountApiConvert {

    PaySettleAccountApiConvert INSTANCE = Mappers.getMapper(PaySettleAccountApiConvert.class);

    PaySettleAccountRespDTO convert(PaySettleAccountDO bean);
}
