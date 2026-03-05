package com.hissp.distribution.module.member.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 会员等级变更事件
 * 当会员等级发生变化时触发
 *
 * @author azhanga
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubCompanyUpgradeEvent {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 变更前的等级
     * 可能为null，表示之前没有等级
     */
    private Integer beforeLevel;

    private Long operatorId;

    private LocalDateTime operateTime;


} 