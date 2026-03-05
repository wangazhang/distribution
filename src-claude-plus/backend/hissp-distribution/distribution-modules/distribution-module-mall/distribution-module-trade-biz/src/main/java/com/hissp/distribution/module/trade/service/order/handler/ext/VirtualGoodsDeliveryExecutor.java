package com.hissp.distribution.module.trade.service.order.handler.ext;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.module.fulfillmentchannel.api.ChannelShippingApi;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingLogisticsTypeEnum;
import com.hissp.distribution.module.pay.api.order.PayOrderApi;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import com.hissp.distribution.module.product.enums.spu.ProductSpuTypeEnum;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderItemMapper;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderMapper;
import com.hissp.distribution.module.trade.service.order.TradeOrderUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 虚拟商品发货执行器
 *
 * <p>负责构造渠道发货请求并调用微信发货接口，发货成功后同步交易订单状态。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VirtualGoodsDeliveryExecutor {

    private final TradeOrderMapper tradeOrderMapper;
    private final TradeOrderItemMapper tradeOrderItemMapper;
    private final ChannelShippingApi channelShippingApi;
    private final TradeOrderUpdateService tradeOrderUpdateService;
    private final PayOrderApi payOrderApi;

    public void execute(Long orderId) {
        execute(orderId, null);
    }

    public void execute(Long orderId, PayOrderRespDTO payOrder) {
        TradeOrderDO order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            log.warn("[execute][orderId={}] 订单不存在，终止虚拟发货", orderId);
            return;
        }
        if (order.getPayOrderId() == null) {
            log.warn("[execute][orderId={}] 缺少支付单编号，终止虚拟发货", orderId);
            return;
        }
        if (StrUtil.isBlank(order.getPayChannelCode())
                || !StrUtil.containsIgnoreCase(order.getPayChannelCode(), "wx")) {
            log.info("[execute][orderId={}] 非微信渠道订单，无需走微信虚拟发货", orderId);
            return;
        }

        PayOrderRespDTO targetPayOrder = payOrder;
        if (targetPayOrder == null) {
            targetPayOrder = payOrderApi.getOrder(order.getPayOrderId());
        }
        if (targetPayOrder == null) {
            log.error("[execute][orderId={}] 未查询到支付单({})，终止虚拟发货", orderId, order.getPayOrderId());
            return;
        }

        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(orderItems)) {
            log.warn("[execute][orderId={}] 订单项为空，终止虚拟发货", orderId);
            return;
        }

        List<TradeOrderItemDO> virtualItems = orderItems.stream()
                .filter(item -> ProductSpuTypeEnum.isVirtual(item.getProductType()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(virtualItems)) {
            log.info("[execute][orderId={}] 不包含虚拟商品，跳过虚拟发货", orderId);
            return;
        }

        ChannelShippingRequestDTO reqDTO = new ChannelShippingRequestDTO();
        reqDTO.setOrderId(orderId);
        reqDTO.setOrderNo(order.getNo());
        reqDTO.setPayOrderId(order.getPayOrderId());
        reqDTO.setUserId(order.getUserId());
        reqDTO.setLogisticsType(ChannelShippingLogisticsTypeEnum.VIRTUAL_GOODS.getType());
        reqDTO.setAllDelivered(isAllVirtual(orderItems));
        reqDTO.setItems(virtualItems.stream().map(this::convertShippingItem).collect(Collectors.toList()));
        reqDTO.setItemDesc(buildItemDesc(reqDTO.getItems()));
        reqDTO.setRemark("虚拟商品支付自动发货");

        channelShippingApi.ship(reqDTO);
        tradeOrderUpdateService.deliveryVirtualOrder(orderId);
    }

    private boolean isAllVirtual(List<TradeOrderItemDO> orderItems) {
        return orderItems.stream().allMatch(item -> ProductSpuTypeEnum.isVirtual(item.getProductType()));
    }

    private String buildItemDesc(List<ChannelShippingRequestDTO.Item> items) {
        String desc = items.stream()
                .map(ChannelShippingRequestDTO.Item::getSpuName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、"));
        if (StrUtil.length(desc) > 120) {
            desc = StrUtil.sub(desc, 0, 120);
        }
        return desc;
    }

    private ChannelShippingRequestDTO.Item convertShippingItem(TradeOrderItemDO item) {
        ChannelShippingRequestDTO.Item dto = new ChannelShippingRequestDTO.Item();
        dto.setOrderItemId(item.getId());
        dto.setSpuId(item.getSpuId());
        dto.setSpuName(item.getSpuName());
        dto.setProductType(item.getProductType());
        dto.setCount(item.getCount());
        return dto;
    }
}
