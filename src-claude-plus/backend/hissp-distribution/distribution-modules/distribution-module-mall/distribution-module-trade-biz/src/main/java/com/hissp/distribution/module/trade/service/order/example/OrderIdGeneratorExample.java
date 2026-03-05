package com.hissp.distribution.module.trade.service.order.example;

import com.hissp.distribution.framework.common.util.id.IdGeneratorUtil;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import org.springframework.stereotype.Component;

/**
 * 订单ID生成器示例
 * 展示如何使用雪花算法生成器生成订单号
 *
 * @author hissp
 */
@Component
public class OrderIdGeneratorExample {

    /**
     * 交易订单前缀
     */
    public static final String PREFIX_TRADE = "TO";
    
    /**
     * 售后订单前缀
     */
    public static final String PREFIX_AFTER_SALE = "AS";
    
    /**
     * 生成交易订单ID
     */
    public String generateTradeOrderNo() {
        return IdGeneratorUtil.nextIdStr(PREFIX_TRADE);
    }
    
    /**
     * 生成售后订单ID
     */
    public String generateAfterSaleOrderNo() {
        return IdGeneratorUtil.nextIdStr(PREFIX_AFTER_SALE);
    }
    
    /**
     * 补货单前缀使用工具类中定义的前缀
     */
    public String generateReplenishmentOrderNo() {
        return IdGeneratorUtil.nextReplenishmentId();
    }
    
    /**
     * 商品单前缀使用工具类中定义的前缀
     */
    public String generateMerchandiseOrderNo() {
        return IdGeneratorUtil.nextMerchandiseId();
    }
    
    /**
     * 创建订单示例
     */
    public TradeOrderDO createOrder() {
        TradeOrderDO order = new TradeOrderDO();
        // 使用ID生成器生成订单号
        order.setNo(generateTradeOrderNo());
        
        // 设置其他订单属性
        // ...
        
        return order;
    }
} 