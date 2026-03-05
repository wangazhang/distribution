package com.hissp.distribution.module.mb.api.commission.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 分佣层级信息
 */
@Data
public class CommissionHierarchyDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String hierarchy;

    private Long userId;

    private Integer level;

    private Boolean brokerageEnabled;
}
