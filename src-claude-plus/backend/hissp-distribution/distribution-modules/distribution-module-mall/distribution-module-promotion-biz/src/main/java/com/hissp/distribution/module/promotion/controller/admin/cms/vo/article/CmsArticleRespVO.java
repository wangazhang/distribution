package com.hissp.distribution.module.promotion.controller.admin.cms.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CMS文章 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS文章 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticleRespVO extends CmsArticleBaseVO {

    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @Schema(description = "浏览数", example = "1000")
    private Integer viewCount;

    @Schema(description = "点赞数", example = "100")
    private Integer likeCount;

    @Schema(description = "收藏数", example = "50")
    private Integer collectCount;

    @Schema(description = "分享数", example = "20")
    private Integer shareCount;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;

    @Schema(description = "发布状态(0-未发布 1-已发布)", example = "1")
    private Integer publishStatus;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
