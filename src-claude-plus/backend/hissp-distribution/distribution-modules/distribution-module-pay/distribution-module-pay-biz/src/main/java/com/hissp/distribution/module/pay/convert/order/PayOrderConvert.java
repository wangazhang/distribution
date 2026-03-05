package com.hissp.distribution.module.pay.convert.order;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.framework.common.util.collection.MapUtils;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import com.hissp.distribution.module.pay.controller.admin.order.vo.*;
import com.hissp.distribution.module.pay.controller.app.order.vo.AppPayOrderSubmitRespVO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.order.PayOrderDO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 支付订单 Convert
 *
 * @author aquan
 */
@Mapper
public interface PayOrderConvert {

    PayOrderConvert INSTANCE = Mappers.getMapper(PayOrderConvert.class);

    PayOrderRespVO convert(PayOrderDO bean);

    PayOrderRespDTO convert2(PayOrderDO order);

    default PayOrderDetailsRespVO convert(PayOrderDO order, PayAppDO app) {
        PayOrderDetailsRespVO respVO = convertDetail(order);
        if (app != null) {
            respVO.setAppName(app.getName());
        }
        return respVO;
    }
    PayOrderDetailsRespVO convertDetail(PayOrderDO bean);

    default PageResult<PayOrderPageItemRespVO> convertPage(PageResult<PayOrderDO> page, Map<Long, PayAppDO> appMap) {
        PageResult<PayOrderPageItemRespVO> result = convertPage(page);
        result.getList().forEach(order -> MapUtils.findAndThen(appMap, order.getAppId(), app -> order.setAppName(app.getName())));
        return result;
    }
    PageResult<PayOrderPageItemRespVO> convertPage(PageResult<PayOrderDO> page);

    default List<PayOrderExcelVO> convertList(List<PayOrderDO> list, Map<Long, PayAppDO> appMap) {
        return CollectionUtils.convertList(list, order -> {
            PayOrderExcelVO excelVO = convertExcel(order);
            MapUtils.findAndThen(appMap, order.getAppId(), app -> excelVO.setAppName(app.getName()));
            return excelVO;
        });
    }
    PayOrderExcelVO convertExcel(PayOrderDO bean);

    PayOrderDO convert(PayOrderCreateReqDTO bean);

    default PayOrderUnifiedInnerReqDTO convert2(PayOrderSubmitReqVO bean, String userIp) {
        PayOrderUnifiedInnerReqDTO reqDTO = new PayOrderUnifiedInnerReqDTO();
        reqDTO.setDisplayMode(bean.getDisplayMode());
        reqDTO.setChannelExtras(bean.getChannelExtras());
        reqDTO.setUserIp(userIp);
        return reqDTO;
    }

    @Mapping(source = "order.status", target = "status")
    PayOrderSubmitRespVO convert(PayOrderDO order, PayOrderInnerRespDTO respDTO);

    AppPayOrderSubmitRespVO convert3(PayOrderSubmitRespVO bean);

}
