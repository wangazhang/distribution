package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物料余额响应 DTO
 */
@Data
public class MaterialBalanceRespDTO implements Serializable {

    /** 记录ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String nickname;

    /** 物料ID */
    private Long materialId;

    /** 物料名称 */
    private String materialName;

    /** 可用余额 */
    private Integer availableBalance;

    /** 冻结余额 */
    private Integer frozenBalance;

    /** 最后更新时间 */
    private LocalDateTime updateTime;

}

