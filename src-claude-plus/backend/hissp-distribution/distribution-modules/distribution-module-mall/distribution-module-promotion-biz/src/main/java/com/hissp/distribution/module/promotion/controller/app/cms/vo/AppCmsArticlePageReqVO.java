package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 用户端 - CMS文章分页查询 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS文章分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCmsArticlePageReqVO extends PageParam {

    @Schema(description = "所属板块ID", example = "1")
    private Long sectionId;

    @Schema(description = "所属分类ID", example = "1")
    private Long categoryId;

    @Schema(description = "标签ID", example = "1")
    private Long tagId;

    @Schema(description = "文章标题(模糊查询)", example = "AI训练师")
    private String title;

    @Schema(description = "是否热门(0-否 1-是)", example = "1")
    private Integer isHot;

    @Schema(description = "是否官方(0-否 1-是)", example = "1")
    private Integer isOfficial;

    @Schema(description = "排序字段(publishTime-发布时间 viewCount-浏览数 likeCount-点赞数)", example = "publishTime")
    private String sortField;

    @Schema(description = "排序方式(asc-升序 desc-降序)", example = "desc")
    private String sortOrder;

    @Schema(description = "作者ID(用于查询指定用户的文章)", example = "1")
    private Long authorId;

    @Schema(description = "作者类型(admin-管理员 user-用户)", example = "user")
    private String authorType;

    @Schema(description = "审核状态(pending-待审核 approved-已通过审核 rejected-已拒绝)", example = "approved")
    private String auditStatus;

    @Schema(description = "发布状态(0-未发布 1-已发布)", example = "1")
    private Integer publishStatus;

}
