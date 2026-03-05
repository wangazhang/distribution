package com.hissp.distribution.module.material.api.dto;

import com.hissp.distribution.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 物料转化规则分页请求 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialConvertRulePageReqDTO extends PageParam {

    /**
     * 转化规则名称
     */
    private String ruleName;

    /**
     * 源物料ID
     */
    private Long sourceMaterialId;

    /**
     * 目标物料ID
     */
    private Long targetMaterialId;

    /**
     * 状态（0:禁用 1:启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}