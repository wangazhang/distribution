package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.math.BigDecimal;

/**
 * 物料转化规则 DO
 */
@TableName("material_convert_rule")
@KeySequence("material_convert_rule_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialConvertRuleDO extends BaseDO {

    /**
     * 规则ID
     */
    @TableId
    private Long id;
    
    /**
     * 转化规则名称
     */
    private String ruleName;
    
    /**
     * 源物料ID
     */
    private Long sourceMaterialId;
    
    /**
     * 目标物料ID
     */
    private Long targetMaterialId;
    
    /**
     * 转化比例
     */
    private BigDecimal convertRatio;
    
    /**
     * 转化费用
     */
    private BigDecimal convertPrice;
    
    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 扩展属性
     */
    private String attrs;

}