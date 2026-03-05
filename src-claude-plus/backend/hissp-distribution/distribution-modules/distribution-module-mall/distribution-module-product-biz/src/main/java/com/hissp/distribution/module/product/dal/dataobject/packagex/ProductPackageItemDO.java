package com.hissp.distribution.module.product.dal.dataobject.packagex;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.Map;

/** 套包物料 DO */
@TableName(value = "product_package_item", autoResultMap = true)
@KeySequence("product_package_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackageItemDO extends BaseDO {

    @TableId
    private Long id;

    private Long packageId;
    /** 1: 商品 2: 权益 3: 自定义 */
    private Integer itemType;
    /** 商品类/权益类均用数字编码。商品：SPU ID；权益：常量编码（1=OPEN_BROKERAGE, 2=SET_LEVEL） */
    private Long itemId;
    private Integer itemQuantity;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> extJson;
}

