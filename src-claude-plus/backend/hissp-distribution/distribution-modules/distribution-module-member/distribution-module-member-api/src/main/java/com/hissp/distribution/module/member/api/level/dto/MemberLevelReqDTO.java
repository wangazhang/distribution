package com.hissp.distribution.module.member.api.level.dto;

import com.hissp.distribution.framework.common.enums.CommonStatusEnum;
import lombok.Data;

/**
 * 会员等级 Resp DTO
 *
 * @author 芋道源码
 */
@Data
public class MemberLevelReqDTO {

    /**
     * 编号
     */
    private Long userId;

    /**
     * 等级
     */
    private Long levelId;
    /**
     * 原因
     */
    private String reason;

    /**
     * 分佣标志
     * true: 需要分佣 false: 不需要分佣
     */
    private Boolean enableBrokerage = false;

    /**
     * 管理员操作标志
     */
    private Boolean adminOp = false;

    /**
     * bizId
     */
    private Long bizId ;

    /**
     * bizType
     */
    private Integer bizType ;


}
