package com.hissp.distribution.module.trade.api.order;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.service.order.TradeOrderQueryService;

import jakarta.annotation.Resource;

/**
 * 订单项 API 接口实现类
 *
 * @author system
 */
@Service
@Validated
public class TradeOrderItemApiImpl implements TradeOrderItemApi {

    @Resource
    private TradeOrderQueryService tradeOrderItemService;

    @Override
    public List<TradeOrderItemRespDTO> getOrderItemListByOrderId(Long orderId) {
        List<TradeOrderItemDO> items = tradeOrderItemService.getOrderItemListByOrderId(orderId);
        return convertList(items);
    }

    @Override
    public List<TradeOrderItemRespDTO> getOrderItemList(Collection<Long> ids) {
        List<TradeOrderItemDO> items = tradeOrderItemService.getOrderItemListByOrderId(ids);
        return convertList(items);
    }
    
    /**
     * 将 DO 列表转换为 DTO 列表
     */
    private List<TradeOrderItemRespDTO> convertList(List<TradeOrderItemDO> items) {
        if (items == null) {
            return null;
        }
        return items.stream().map(this::convert).collect(Collectors.toList());
    }
    
    /**
     * 将 DO 转换为 DTO
     */
    private TradeOrderItemRespDTO convert(TradeOrderItemDO item) {
        if (item == null) {
            return null;
        }
        TradeOrderItemRespDTO dto = new TradeOrderItemRespDTO();
        BeanUtils.copyProperties(item, dto);
        return dto;
    }
} 