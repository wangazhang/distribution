package com.hissp.distribution.module.trade.api.brokerage.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 分佣记录创建 Request DTO
 *
 * @author system
 */
@Data
@Accessors(chain = true)
public class BrokerageRecordCreateReqDTO {

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 业务编号
     */
    private String bizId;

    /**
     * 业务类型
     */
    private Integer bizType;

    /**
     * 业务大类
     */
    private Integer bizCategory;

    /**
     * 标题
     */
    private String title;

    /**
     * 描述
     */
    private String description;

    /**
     * 金额
     */
    private Integer price;
    ///**
    // * 总金额
    // */
    //private Integer totalPrice;

    /**
     * 冻结时间（天）
     */
    private Integer frozenDays;
    /**
     * 解冻时间
     */
    private LocalDateTime unfreezeTime;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 来源用户等级
     * <p>
     * 被推广用户和 {@link #userId} 的推广层级关系
     */
    private Integer sourceUserLevel;
    /**
     * 来源用户编号
     * <p>
     * 关联 MemberUserDO.id 字段，被推广用户的编号
     */
    private Long sourceUserId;

    /**
     * 策略编码
     */
    private String strategyCode;

    /**
     * 策略规则ID
     */
    private Long strategyRuleId;

    /**
     * 策略规则展示名称
     */
    private String strategyDisplayName;
    
} 
