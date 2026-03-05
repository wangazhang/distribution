package com.hissp.distribution.module.product.controller.admin.packagex.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ProductPackageItemVO {

    /** 1: 商品 2: 权益 3: 自定义 */
    @NotNull(message = "itemType 不能为空")
    private Integer itemType;

    /** 商品：SPU ID；权益：数值编码（1=OPEN_BROKERAGE, 2=SET_LEVEL） */
    @NotNull(message = "itemId 不能为空")
    private Long itemId;

    @Min(value = 1, message = "itemQuantity 必须大于等于 1")
    private Integer itemQuantity;

    /** 权益扩展参数，如 SET_LEVEL: { levelId: 3 } */
    private Map<String, Object> extJson;
}

