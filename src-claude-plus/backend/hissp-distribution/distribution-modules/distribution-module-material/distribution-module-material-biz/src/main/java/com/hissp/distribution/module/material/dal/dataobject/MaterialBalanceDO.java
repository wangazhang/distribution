package com.hissp.distribution.module.material.dal.dataobject;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 用户物料余额 DO
 */
@TableName("material_balance")
@KeySequence("material_balance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialBalanceDO extends BaseDO {

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
     * 可用余额
     */
    private Integer availableBalance;
    /**
     * 冻结余额
     */
    private Integer frozenBalance;
    /**
     * 版本号（乐观锁）
     */
    @Version
    private Integer version;

}

