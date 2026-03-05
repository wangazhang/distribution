package com.hissp.distribution.module.trade.api.order.dto;

import lombok.Data;

/**
 * 交易订单项 Response DTO
 *
 * @author system
 */
@Data
public class TradeOrderItemRespDTO {

    /**
     * 编号，主键自增
     */
    private Long id;
    
    /**
     * 用户编号
     */
    private Long userId;
    
    /**
     * 订单编号
     */
    private Long orderId;
    
    /**
     * 商品SPU编号
     */
    private Long spuId;
    
    /**
     * 商品SPU名称
     */
    private String spuName;
    
    /**
     * 商品SKU编号
     */
    private Long skuId;
    
    /**
     * 商品SKU名称
     */
    private String skuName;
    
    /**
     * 商品图片
     */
    private String picUrl;
    
    /**
     * 购买数量
     */
    private Integer count;
    
    /**
     * 商品原价（单），单位：分
     */
    private Integer price;
    
    /**
     * 商品优惠（总），单位：分
     */
    private Integer discountPrice;
    
    /**
     * 最终价格（总），单位：分
     */
    private Integer payPrice;

    /**
     * 商品属性数组，JSON 格式
     */
    private String properties;

}