package com.hissp.distribution.module.trade.api.order;

import com.hissp.distribution.module.trade.api.order.dto.TradeOrderChannelReceiveReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderDeliveryReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemCommentCreateReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.convert.order.TradeOrderConvert;
import com.hissp.distribution.module.trade.service.order.TradeOrderQueryService;
import com.hissp.distribution.module.trade.service.order.TradeOrderUpdateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

/**
 * 订单 API 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class TradeOrderApiImpl implements TradeOrderApi {

    @Resource
    private TradeOrderUpdateService tradeOrderUpdateService;
    @Resource
    private TradeOrderQueryService tradeOrderQueryService;

    @Override
    public List<TradeOrderRespDTO> getOrderList(Collection<Long> ids) {
        return TradeOrderConvert.INSTANCE.convertList04(tradeOrderQueryService.getOrderList(ids));
    }

    @Override
    public TradeOrderRespDTO getOrder(Long id) {
        return TradeOrderConvert.INSTANCE.convert(tradeOrderQueryService.getOrder(id));
    }

    @Override
    public void cancelPaidOrder(Long userId, Long orderId, Integer cancelType) {
        tradeOrderUpdateService.cancelPaidOrder(userId, orderId, cancelType);
    }

    @Override
    public void deliveryOrder(TradeOrderDeliveryReqDTO deliveryReqDTO) {
        // 转换API DTO为内部VO
        var deliveryReqVO = TradeOrderConvert.INSTANCE.convertToDeliveryVO(deliveryReqDTO);
        tradeOrderUpdateService.deliveryOrder(deliveryReqVO);
    }

    @Override
    public void receiveOrderByMember(Long userId, Long orderId) {
        tradeOrderUpdateService.receiveOrderByMember(userId, orderId);
    }

    @Override
    public Long createOrderItemComment(TradeOrderItemCommentCreateReqDTO commentReqDTO) {
        // 转换API DTO为内部VO
        var commentReqVO = TradeOrderConvert.INSTANCE.convertToCommentVO(commentReqDTO);
        return tradeOrderUpdateService.createOrderItemCommentByMember(commentReqDTO.getUserId(), commentReqVO);
    }

    @Override
    public void handleChannelOrderReceive(TradeOrderChannelReceiveReqDTO reqDTO) {
        tradeOrderUpdateService.receiveOrderByChannel(reqDTO);
    }

}
