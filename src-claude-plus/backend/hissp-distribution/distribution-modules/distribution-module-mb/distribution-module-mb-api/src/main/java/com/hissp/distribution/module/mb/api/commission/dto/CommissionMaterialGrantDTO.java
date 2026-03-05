package com.hissp.distribution.module.mb.api.commission.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 分佣规则触发时产生的物料奖励 DTO
 */
@Data
public class CommissionMaterialGrantDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 关联策略 ID
     */
    private Long policyId;

    /**
     * 关联策略编码
     */
    private String policyCode;

    /**
     * 规则 ID
     */
    private Long ruleId;

    /**
     * 规则展示名称
     */
    private String ruleDisplayName;

    /**
     * 命中层级
     */
    private String targetHierarchy;

    /**
     * 奖励接收人
     */
    private Long userId;

    /**
     * 物料 ID
     */
    private Long materialId;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 物料编码
     */
    private String materialCode;

    /**
     * 物料图片
     */
    private String materialImage;

    /**
     * 物料单位
     */
    private String materialUnit;

    /**
     * 发放数量
     */
    private Integer quantity;

    /**
     * 幂等键
     */
    private String bizKey;

    /**
     * 物料流水业务类型
     */
    private String bizType;

    /**
     * 原因描述
     */
    private String reason;
}
