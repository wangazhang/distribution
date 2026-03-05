package com.hissp.distribution.module.promotion.controller.admin.cms.vo.article;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * CMS文章分页查询 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS文章分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticlePageReqVO extends PageParam {

    @Schema(description = "所属板块ID", example = "1")
    private Long sectionId;

    @Schema(description = "所属分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @Schema(description = "文章标题(模糊查询)", example = "Spring Boot")
    private String title;

    @Schema(description = "作者类型", example = "admin")
    private String authorType;

    @Schema(description = "作者ID", example = "100")
    private Long authorId;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "发布状态(0-未发布 1-已发布)", example = "1")
    private Integer publishStatus;

    @Schema(description = "是否热门(0-否 1-是)", example = "1")
    private Integer isHot;

    @Schema(description = "是否官方(0-否 1-是)", example = "1")
    private Integer isOfficial;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
