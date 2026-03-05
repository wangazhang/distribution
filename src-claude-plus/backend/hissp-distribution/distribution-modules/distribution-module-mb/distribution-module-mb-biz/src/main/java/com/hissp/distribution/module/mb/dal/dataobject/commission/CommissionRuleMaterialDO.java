package com.hissp.distribution.module.mb.dal.dataobject.commission;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 分佣规则 - 物料发放配置 DO
 *
 * 为了便于策略执行阶段直接取到快照信息，这里冗余了物料名称、编码与单位。
 */
@TableName("mb_commission_rule_material")
@KeySequence("mb_commission_rule_material_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommissionRuleMaterialDO extends BaseDO {

    @TableId
    private Long id;

    /**
     * 关联的分佣规则 ID
     */
    private Long ruleId;

    /**
     * 物料 ID
     */
    private Long materialId;

    /**
     * 物料名称快照
     */
    private String materialName;

    /**
     * 物料编码快照
     */
    private String materialCode;

    /**
     * 物料主图快照
     */
    private String materialImage;

    /**
     * 物料单位快照
     */
    private String materialUnit;

    /**
     * 发放数量
     */
    private Integer quantity;

    /**
     * 冗余策略 ID 便于排查
     */
    private Long policyId;

    /**
     * 冗余规则展示名称，方便查询
     */
    @TableField(exist = false)
    private String ruleDisplayName;
}
