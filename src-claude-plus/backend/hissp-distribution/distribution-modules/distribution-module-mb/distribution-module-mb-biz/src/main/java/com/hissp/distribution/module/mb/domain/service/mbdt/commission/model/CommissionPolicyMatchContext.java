package com.hissp.distribution.module.mb.domain.service.mbdt.commission.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 策略匹配上下文
 */
@Data
@Accessors(chain = true)
public class CommissionPolicyMatchContext {

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 触发用户等级
     */
    private Integer triggerLevel;

    /**
     * 商品 ID
     */
    private Long productId;

    /**
     * 套包 ID
     */
    private Long packageId;
}
