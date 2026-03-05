package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料转化记录响应 DTO
 */
@Data
public class MaterialConvertRecordRespDTO {

    /**
     * 转化记录ID
     */
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
     * 转化规则名称
     */
    private String ruleName;

    /**
     * 源物料ID
     */
    private Long sourceMaterialId;

    /**
     * 源物料名称
     */
    private String sourceMaterialName;

    /**
     * 源物料编码
     */
    private String sourceMaterialCode;

    /**
     * 目标物料ID
     */
    private Long targetMaterialId;

    /**
     * 目标物料名称
     */
    private String targetMaterialName;

    /**
     * 目标物料编码
     */
    private String targetMaterialCode;

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
     * 状态名称
     */
    private String statusName;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}