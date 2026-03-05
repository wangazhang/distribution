package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.math.BigDecimal;

/**
 * 物料转化记录 DO
 */
@TableName("material_convert_record")
@KeySequence("material_convert_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialConvertRecordDO extends BaseDO {

    /**
     * 记录ID
     */
    @TableId
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 转化规则ID
     */
    private Long ruleId;
    
    /**
     * 源物料ID
     */
    private Long sourceMaterialId;
    
    /**
     * 目标物料ID
     */
    private Long targetMaterialId;
    
    /**
     * 源物料数量
     */
    private Integer sourceQuantity;
    
    /**
     * 目标物料数量
     */
    private Integer targetQuantity;
    
    /**
     * 转化费用
     */
    private BigDecimal convertPrice;
    
    /**
     * 转化状态：1成功 2失败
     */
    private Integer status;
    
    /**
     * 关联订单ID
     */
    private Long orderId;
    
    /**
     * 转化原因/备注
     */
    private String reason;
    
    /**
     * 失败原因
     */
    private String failureReason;

}