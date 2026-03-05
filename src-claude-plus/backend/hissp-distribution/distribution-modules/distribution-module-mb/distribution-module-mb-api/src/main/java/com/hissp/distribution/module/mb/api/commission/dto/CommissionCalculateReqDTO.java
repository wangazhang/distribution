package com.hissp.distribution.module.mb.api.commission.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import lombok.Data;

/**
 * 分佣策略计算请求 DTO
 */
@Data
public class CommissionCalculateReqDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bizType;

    private Long policyId;

    private String policyCode;

    private Long productId;

    private Long packageId;

    private CommissionOrderDTO order;

    private List<CommissionHierarchyDTO> hierarchies;

    private Map<String, Object> attributes;
}
