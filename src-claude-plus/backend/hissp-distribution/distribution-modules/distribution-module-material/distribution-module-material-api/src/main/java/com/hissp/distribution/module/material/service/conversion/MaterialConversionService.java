package com.hissp.distribution.module.material.service.conversion;

import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;

import java.util.List;

/**
 * 物料转化服务
 * 负责将订单项等业务对象，转换为标准的物料动作列表
 */
public interface MaterialConversionService {

    /**
     * 将订单项转换为物料动作列表
     *
     * @param order 订单
     * @param item 订单项
     * @param direction 方向（IN:入账, OUT:回退）
     * @return 物料动作列表
     */
    List<MaterialActDTO> convert(TradeOrderRespDTO order, TradeOrderItemRespDTO item, MaterialActDirectionEnum direction);

}

