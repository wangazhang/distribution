package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 物料出库明细 DO
 */
@TableName("material_outbound_item")
@KeySequence("material_outbound_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialOutboundItemDO extends BaseDO {

    /**
     * 明细ID
     */
    @TableId
    private Long id;
    
    /**
     * 出库单ID
     */
    private Long outboundId;
    
    /**
     * 物料ID
     */
    private Long materialId;
    
    /**
     * 出库数量
     */
    private Integer quantity;

    /**
     * 单位
     */
    private String unit;

    /**
     * 物料名称（关联查询获取，非表字段）
     */
    @TableField(exist = false)
    private String materialName;

    /**
     * 物料编码（关联查询获取，非表字段）
     */
    @TableField(exist = false)
    private String materialCode;

    /**
     * 基础单位（关联查询获取，非表字段）
     */
    @TableField(exist = false)
    private String baseUnit;

}