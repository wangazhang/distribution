package com.hissp.distribution.module.trade.api.brokerage.dto;

import lombok.Data;

/**
 * 分销用户 Response DTO
 *
 * @author system
 */
@Data
public class BrokerageUserRespDTO {

    /**
     * 用户编号
     */
    private Long id;
    
    /**
     * 推广员编号
     */
    private String code;
    
    /**
     * 推广资格
     */
    private Boolean brokerageEnabled;
    
    /**
     * 可提现佣金
     */
    private Integer brokeragePrice;
    
    /**
     * 冻结佣金
     */
    private Integer frozenPrice;
    
    /**
     * 累计佣金
     */
    private Integer totalBrokeragePrice;
    
    /**
     * 绑定推广员编号
     */
    private Long bindUserId;
    
    /**
     * 绑定推广员时间
     */
    private String bindUserTime;
    
    /**
     * 是否团队leader
     */
    private Boolean leader;
    
} 