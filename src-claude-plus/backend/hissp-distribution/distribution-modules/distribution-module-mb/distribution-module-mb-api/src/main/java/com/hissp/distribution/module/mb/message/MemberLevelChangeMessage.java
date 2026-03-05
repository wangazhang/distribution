package com.hissp.distribution.module.mb.message;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 会员等级变更消息
 * 当会员等级发生变化时触发
 *
 * @author azhanga
 */
@Data
public class MemberLevelChangeMessage {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 变更前的等级ID
     * 可能为null，表示之前没有等级
     */
    private Long beforeLevelId;
    
    /**
     * 变更前的等级
     */
    private Integer beforeLevel;
    
    /**
     * 变更后的等级ID
     */
    @NotNull(message = "变更后的等级ID不能为空")
    private Long afterLevelId;
    
    /**
     * 变更后的等级
     */
    @NotNull(message = "变更后的等级不能为空")
    private Integer afterLevel;
    
    /**
     * 手动分佣
     */
    private Boolean manualEnableBrokerage = false;

    /**
     * 附赠套包编号
     */
    private Long packageId;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作时间
     */
    private LocalDateTime operateTime;

}
