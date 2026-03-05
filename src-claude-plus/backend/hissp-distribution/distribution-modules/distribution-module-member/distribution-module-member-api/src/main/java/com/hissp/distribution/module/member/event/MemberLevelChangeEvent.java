package com.hissp.distribution.module.member.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberLevelChangeEvent {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 变更前的等级 可能为null，表示之前没有等级
     */
    private Long beforeLevelId;
    private Integer beforeLevel;
    /**
     * 变更后的等级
     */
    private Long afterLevelId;
    private Integer afterLevel;
    /**
     * 手动分佣
     */
    private Boolean manualEnableBrokerage=false;

    /**
     * 附赠套包编号
     */
    private Long packageId;

    private Long operatorId;

    private LocalDateTime operateTime;

}
