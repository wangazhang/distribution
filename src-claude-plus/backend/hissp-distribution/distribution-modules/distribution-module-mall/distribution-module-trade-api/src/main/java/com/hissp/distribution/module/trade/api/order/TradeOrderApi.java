package com.hissp.distribution.module.trade.api.order;

import com.hissp.distribution.module.trade.api.order.dto.TradeOrderChannelReceiveReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderDeliveryReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemCommentCreateReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;

import java.util.Collection;
import java.util.List;

/**
 * 订单 API 接口
 *
 * @author HUIHUI
 */
public interface TradeOrderApi {

    /**
     * 获得订单列表
     *
     * @param ids 订单编号数组
     * @return 订单列表
     */
    List<TradeOrderRespDTO> getOrderList(Collection<Long> ids);

    /**
     * 获得订单
     *
     * @param id 订单编号
     * @return 订单
     */
    TradeOrderRespDTO getOrder(Long id);

    /**
     * 取消支付订单
     *
     * @param userId 用户编号
     * @param orderId 订单编号
     * @param cancelType 取消类型
     */
    void cancelPaidOrder(Long userId, Long orderId, Integer cancelType);

    /**
     * 发货订单
     * 用于系统自动发货或管理员发货操作
     *
     * @param deliveryReqDTO 发货请求参数
     */
    void deliveryOrder(TradeOrderDeliveryReqDTO deliveryReqDTO);

    /**
     * 会员收货订单
     * 用于用户确认收货或系统代客收货
     *
     * @param userId 用户编号
     * @param orderId 订单编号
     */
    void receiveOrderByMember(Long userId, Long orderId);

    /**
     * 创建订单项评论
     * 用于用户评价商品或系统代客评价
     *
     * @param commentReqDTO 评论创建请求参数
     * @return 评论ID
     */
    Long createOrderItemComment(TradeOrderItemCommentCreateReqDTO commentReqDTO);

    /**
     * 渠道回传的订单收货消息
     *
     * @param reqDTO 渠道结算/收货消息
     */
    void handleChannelOrderReceive(TradeOrderChannelReceiveReqDTO reqDTO);

}
