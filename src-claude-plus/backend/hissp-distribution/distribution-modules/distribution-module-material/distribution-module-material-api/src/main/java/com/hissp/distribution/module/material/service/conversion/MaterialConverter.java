package com.hissp.distribution.module.material.service.conversion;

import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;

import java.util.List;

/**
 * 物料转化器 SPI
 * 不同的业务场景（套包、MB、普通商品）可实现此接口，定义自己的转化规则
 */
public interface MaterialConverter {

    /**
     * 是否支持处理该订单项
     *
     * @param item 订单项
     * @return true 如果支持
     */
    boolean supports(TradeOrderItemRespDTO item);

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

