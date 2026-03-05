package com.hissp.distribution.module.pay.convert.demo;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.pay.controller.admin.demo.vo.order.PayDemoOrderCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.demo.vo.order.PayDemoOrderRespVO;
import com.hissp.distribution.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 示例订单 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface PayDemoOrderConvert {

    PayDemoOrderConvert INSTANCE = Mappers.getMapper(PayDemoOrderConvert.class);

    PayDemoOrderDO convert(PayDemoOrderCreateReqVO bean);

    PayDemoOrderRespVO convert(PayDemoOrderDO bean);

    PageResult<PayDemoOrderRespVO> convertPage(PageResult<PayDemoOrderDO> page);

}
