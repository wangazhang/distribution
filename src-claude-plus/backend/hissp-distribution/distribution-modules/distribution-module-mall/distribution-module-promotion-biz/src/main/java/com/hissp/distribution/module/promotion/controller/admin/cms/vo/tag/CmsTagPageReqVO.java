package com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * CMS标签分页查询 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS标签分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsTagPageReqVO extends PageParam {

    @Schema(description = "标签名称(模糊查询)", example = "Spring Boot")
    private String name;

    @Schema(description = "状态(0-禁用 1-启用)", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
