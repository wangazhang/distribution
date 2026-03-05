package com.hissp.distribution.module.product.api.sku.dto;

import com.hissp.distribution.module.product.api.property.dto.ProductPropertyValueDetailRespDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 商品 SKU 信息 Response DTO
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Data
public class ProductSkuRespDTO {

    /**
     * 商品 SKU 编号，自增
     */
    private Long id;
    /**
     * SPU 编号
     */
    private Long spuId;

    /**
     * 属性数组
     */
    private List<ProductPropertyValueDetailRespDTO> properties;
    /**
     * 销售价格，单位：分
     */
    private Integer price;
    /**
     * 市场价，单位：分
     */
    private Integer marketPrice;
    /**
     * 成本价，单位：分
     */
    private Integer costPrice;
    /**
     * SKU 的条形码
     */
    private String barCode;
    /**
     * 图片地址
     */
    private String picUrl;
    /**
     * 库存
     */
    private Integer stock;
    /**
     * 商品重量，单位：kg 千克
     */
    private Double weight;
    /**
     * 商品体积，单位：m^3 平米
     */
    private Double volume;
    /**
     * 一级分销的佣金，单位：分
     */
    private Integer firstBrokeragePrice;
    /**
     * 二级分销的佣金，单位：分
     */
    private Integer secondBrokeragePrice;
    /**
     * 会员价配置
     */
    private List<MemberPrice> memberPrices;

    /**
     * 会员价配置项
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemberPrice {

        /**
         * 会员等级编号
         */
        private Long levelId;
        /**
         * 会员价，单位：分
         */
        private Integer price;

    }

}
