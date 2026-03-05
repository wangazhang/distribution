package com.hissp.distribution.module.product.dal.dataobject.sku;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import com.hissp.distribution.module.product.dal.dataobject.property.ProductPropertyDO;
import com.hissp.distribution.module.product.dal.dataobject.property.ProductPropertyValueDO;
import com.hissp.distribution.module.product.dal.dataobject.spu.ProductSpuDO;
import lombok.*;

import java.util.List;

/**
 * 商品 SKU DO
 *
 * @author 芋道源码
 */
@TableName(value = "product_sku", autoResultMap = true)
@KeySequence("product_sku_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSkuDO extends BaseDO {

    /**
     * 商品 SKU 编号，自增
     */
    @TableId
    private Long id;
    /**
     * SPU 编号
     *
     * 关联 {@link ProductSpuDO#getId()}
     */
    private Long spuId;
    /**
     * 属性数组，JSON 格式
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Property> properties;
    /**
     * 商品价格，单位：分
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
     * 商品条码
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
     * 会员价配置，JSON 数组
     *
     * 设计说明：
     * 1. 每个 SKU 可针对不同会员等级配置一个“锁定售价”，避免重复计算折扣；
     * 2. 该字段仅在需要会员价时解析，默认保持 null，避免单规格商品冗余；
     * 3. 结构体见 {@link MemberPrice}，由等级+价格两部分组成。
     */
    @TableField(value = "member_price_json", typeHandler = JacksonTypeHandler.class)
    private List<MemberPrice> memberPrices;

    // ========== 营销相关字段 =========

    // ========== 统计相关字段 =========
    /**
     * 商品销量
     */
    private Integer salesCount;

    /**
     * 商品属性
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Property {

        /**
         * 属性编号
         * 关联 {@link ProductPropertyDO#getId()}
         */
        private Long propertyId;
        /**
         * 属性名字
         * 冗余 {@link ProductPropertyDO#getName()}
         *
         * 注意：每次属性名字发生变化时，需要更新该冗余
         */
        private String propertyName;

        /**
         * 属性值编号
         * 关联 {@link ProductPropertyValueDO#getId()}
         */
        private Long valueId;
        /**
         * 属性值名字
         * 冗余 {@link ProductPropertyValueDO#getName()}
         *
         * 注意：每次属性值名字发生变化时，需要更新该冗余
         */
        private String valueName;

    }

    /**
     * SKU 会员价配置
     *
     * 只冗余等级编号与对应的会员价（单位：分），其余属性由会员等级表维护
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
