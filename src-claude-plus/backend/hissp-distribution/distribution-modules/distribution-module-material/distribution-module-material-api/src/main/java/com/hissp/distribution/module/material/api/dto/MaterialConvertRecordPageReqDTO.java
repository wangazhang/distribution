package com.hissp.distribution.module.material.api.dto;

import com.hissp.distribution.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 物料转化记录分页查询请求 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialConvertRecordPageReqDTO extends PageParam {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 转化规则ID
     */
    private Long ruleId;

    /**
     * 源物料ID
     */
    private Long sourceMaterialId;

    /**
     * 目标物料ID
     */
    private Long targetMaterialId;

    /**
     * 转化状态：1成功 2失败
     */
    private Integer status;

    /**
     * 创建时间
     */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}