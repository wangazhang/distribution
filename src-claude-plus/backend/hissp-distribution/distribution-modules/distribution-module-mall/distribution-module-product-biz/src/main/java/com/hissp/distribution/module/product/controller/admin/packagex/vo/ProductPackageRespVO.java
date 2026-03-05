package com.hissp.distribution.module.product.controller.admin.packagex.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ProductPackageRespVO {
    private Long id;
    private String name;
    private Long spuId;
    private Integer status; // 0:草稿 1:启用 2:禁用
    private String remark;

    private List<Item> items;
    private List<Commission> commissions;

    @Data
    public static class Item {
        private Long id;
        private Long packageId;
        private Integer itemType; // 1:商品 2:权益 3:自定义
        private Long itemId;      // 商品SPU 或 权益编码（1=OPEN_BROKERAGE 2=SET_LEVEL）
        private Integer itemQuantity;
        private Map<String, Object> extJson;
    }

    @Data
    public static class Commission {
        private Long id;
        private Long packageId;
        private Integer level;           // 1/2
        private Integer commissionType;  // 1固定金额(分) 2比例(%)
        private BigDecimal commissionValue;
        private Integer baseType;        // 1订单项实付 2自定义基准
        private Integer baseAmount;      // 分
    }
}

