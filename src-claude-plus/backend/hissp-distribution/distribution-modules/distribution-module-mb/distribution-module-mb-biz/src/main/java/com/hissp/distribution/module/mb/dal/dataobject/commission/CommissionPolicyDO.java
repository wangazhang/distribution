package com.hissp.distribution.module.mb.dal.dataobject.commission;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分佣策略头表 DO
 */
@TableName("mb_commission_policy")
@KeySequence("mb_commission_policy_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionPolicyDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 策略英文编码
     */
    private String policyCode;

    /**
     * 策略展示名称
     */
    private String displayName;

    /**
     * 状态：DRAFT/ONLINE/OFFLINE
     */
    private String status;

    /**
     * 版本号
     */
    private Integer versionNo;

    /**
     * 业务类型，例如 RESTOCK_MATERIAL
     */
    private String bizType;

    /**
     * 适用用户等级
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String applicableLevel;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private List<Long> applicableProductIds;

    @TableField(exist = false)
    private List<Long> applicablePackageIds;
}
