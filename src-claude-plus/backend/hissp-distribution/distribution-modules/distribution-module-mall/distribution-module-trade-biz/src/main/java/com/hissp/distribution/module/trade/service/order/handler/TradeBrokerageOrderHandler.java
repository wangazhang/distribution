package com.hissp.distribution.module.trade.service.order.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.product.api.sku.ProductSkuApi;
import com.hissp.distribution.module.product.api.sku.dto.ProductSkuRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import com.hissp.distribution.module.trade.convert.order.TradeOrderConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageUserService;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;

import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertList;

/**
 * 订单分销的 {@link TradeOrderHandler} 实现类
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class TradeBrokerageOrderHandler implements TradeOrderHandler {

    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private BrokerageUserService brokerageUserService;

    @Override
    public void beforeOrderCreate(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 设置订单推广人
        BrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(order.getUserId());
        if (brokerageUser != null && brokerageUser.getBindUserId() != null) {
            order.setBrokerageUserId(brokerageUser.getBindUserId());
        }
    }

    @Override
    public void afterPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        if (order.getBrokerageUserId() == null) {
            return;
        }
        //addBrokerage4Orders(order.getUserId(), orderItems);
    }

    @Override
    public void afterCancelOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 如果是未支付的订单，不会产生分销结果，所以直接 return
        if (!order.getPayStatus()) {
            return;
        }
        if (order.getBrokerageUserId() == null) {
            return;
        }

        // 售后的订单项，已经在 afterCancelOrderItem 回滚库存，所以这里不需要重复回滚
        orderItems = filterOrderItemListByNoneAfterSale(orderItems);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        orderItems.forEach(orderItem -> afterCancelOrderItem(order, orderItem));
    }

    @Override
    public void afterCancelOrderItem(TradeOrderDO order, TradeOrderItemDO orderItem) {
        if (order.getBrokerageUserId() == null) {
            return;
        }
        if (TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus().equals(orderItem.getAfterSaleStatus())) {
            log.debug("[afterCancelOrderItem][orderId={} itemId={}] 已为售后退款成功，等待统一退款事件处理，跳过同步撤销佣金",
                    order.getId(), orderItem.getId());
            return;
        }
        brokerageRecordService.cancelBrokerage(BrokerageRecordBizTypeEnum.ORDER_ADD, String.valueOf(orderItem.getId()));
    }

    /**
     * 创建分销记录
     * <p>
     * 目前是支付成功后，就会创建分销记录。
     * <p>
     * 业内还有两种做法，可以根据自己的业务调整：
     * 1. 确认收货后，才创建分销记录
     * 2. 支付 or 下单成功时，创建分销记录（冻结），确认收货解冻或者 n 天后解冻
     *
     * @param userId  用户编号
     * @param orderItems 订单项
     */
    protected void addBrokerage4Orders(Long userId, List<TradeOrderItemDO> orderItems) {
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        Assert.notNull(user);
        ProductSpuRespDTO spu = productSpuApi.getSpu(orderItems.get(0).getSpuId());
        Assert.notNull(spu);
        ProductSkuRespDTO sku = productSkuApi.getSku(orderItems.get(0).getSkuId());

        // 每一个订单项，都会去生成分销记录
        List<BrokerageAddReqBO> addList = convertList(orderItems,
                item -> TradeOrderConvert.INSTANCE.convert(user, item, spu, sku));
        brokerageRecordService.addBrokerage4Orders(userId, BrokerageRecordBizTypeEnum.ORDER_ADD, addList);
    }

}
