package com.hissp.distribution.module.mb.dal.dataobject.material;

import com.baomidou.mybatisplus.annotation.*;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 补货价格表：记录不同用户等级对应的商品补货价格 DO
 * 
 * @author azhanga
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("mb_material_restock_price")
public class MaterialRestockPriceDO extends BaseDO {

    /**
     * 价格配置ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户等级ID
     */
    private Long levelId;

    /**
     * 产品SPU ID
     */
    private Long productId;

    /**
     * 补货价格（单位：分）
     */
    private Integer restockPrice;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

}