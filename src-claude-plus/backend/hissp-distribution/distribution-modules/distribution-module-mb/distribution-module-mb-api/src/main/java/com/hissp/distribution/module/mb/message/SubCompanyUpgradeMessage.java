package com.hissp.distribution.module.mb.message;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 子公司升级消息
 * 当子公司升级时触发
 *
 * @author azhanga
 */
@Data
public class SubCompanyUpgradeMessage {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 变更前的等级
     * 可能为null，表示之前没有等级
     */
    private Integer beforeLevel;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

}
