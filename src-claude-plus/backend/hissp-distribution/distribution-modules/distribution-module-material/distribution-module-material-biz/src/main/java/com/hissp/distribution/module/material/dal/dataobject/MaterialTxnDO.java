package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 物料流水 DO
 */
@TableName("material_txn")
@KeySequence("material_txn_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialTxnDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 物料ID
     */
    private Long materialId;
    /**
     * 方向（1:增加 -1:减少）
     */
    private Integer direction;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 流水后余额
     */
    private Integer balanceAfter;
    /**
     * 业务唯一标识（幂等键）
     */
    private String bizKey;
    /**
     * 业务类型
     */
    private String bizType;
    /**
     * 备注/原因
     */
    private String reason;
    /**
     * 扩展信息
     */
    private String extJson; // TODO: typeHandler for JSON

}

