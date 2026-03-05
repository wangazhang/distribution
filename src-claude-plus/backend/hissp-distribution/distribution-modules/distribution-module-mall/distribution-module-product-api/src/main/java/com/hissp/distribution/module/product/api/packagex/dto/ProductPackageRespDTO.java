package com.hissp.distribution.module.product.api.packagex.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductPackageRespDTO implements Serializable {
    private Long id;
    private String name;
    private Long spuId;
    private Integer status; // 0: 草稿, 1: 启用, 2: 禁用
    private String remark;

    private List<Item> items;
    private List<Commission> commissions;

    @Data
    public static class Item implements Serializable {
        private Long id;
        private Long packageId;
        private Integer itemType; // 1: 商品 2: 权益 3: 自定义
        private String itemId;    // 统一转为字符串以兼容 OPEN_BROKERAGE / SET_LEVEL 等标识
        private Integer itemQuantity;
        private Map<String, Object> extJson;
    }

    @Data
    public static class Commission implements Serializable {
        private Long id;
        private Long packageId;
        private Integer level;           // 1: 上级, 2: 上上级
        private Integer commissionType;  // 1: 固定金额(分), 2: 固定比例(%)
        private BigDecimal commissionValue; // 金额(分)或百分比
        private Integer baseType;        // 1: 订单项实付, 2: 自定义基准
        private Integer baseAmount;      // 自定义基准价(分)
    }
}

