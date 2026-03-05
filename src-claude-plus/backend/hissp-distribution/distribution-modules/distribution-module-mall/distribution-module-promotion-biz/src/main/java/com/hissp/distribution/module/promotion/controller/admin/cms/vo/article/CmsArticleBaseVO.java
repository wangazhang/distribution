package com.hissp.distribution.module.promotion.controller.admin.cms.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * CMS文章 Base VO
 *
 * @author 芋道源码
 */
@Data
public class CmsArticleBaseVO {

    @Schema(description = "所属板块ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属板块ID不能为空")
    private Long sectionId;

    @Schema(description = "所属分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属分类ID不能为空")
    private Long categoryId;

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "深入理解Spring Boot自动配置原理")
    @NotBlank(message = "文章标题不能为空")
    private String title;

    @Schema(description = "副标题", example = "从源码角度分析")
    private String subtitle;

    @Schema(description = "封面图片(JSON数组)", example = "[\"https://example.com/cover1.jpg\"]")
    private List<String> coverImages;

    @Schema(description = "文章内容(富文本/Markdown)", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文章内容不能为空")
    private String content;

    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "richtext")
    @NotBlank(message = "内容类型不能为空")
    private String contentType;

    @Schema(description = "作者ID", example = "100")
    private Long authorId;

    @Schema(description = "作者类型", example = "admin")
    private String authorType;

    @Schema(description = "作者名称", example = "张三")
    private String authorName;

    @Schema(description = "作者头像", example = "https://example.com/avatar.jpg")
    private String authorAvatar;

    @Schema(description = "是否热门(0-否 1-是)", example = "0")
    private Integer isHot;

    @Schema(description = "是否官方(0-否 1-是)", example = "0")
    private Integer isOfficial;

    @Schema(description = "是否支持点赞(0-否 1-是)", example = "1")
    private Integer enableLike;

    @Schema(description = "初始点赞数", example = "0")
    private Integer initialLikeCount;

    @Schema(description = "是否显示点赞数(0-否 1-是)", example = "1")
    private Integer showLikeCount;

    @Schema(description = "是否支持收藏(0-否 1-是)", example = "1")
    private Integer enableCollect;

    @Schema(description = "初始收藏数", example = "0")
    private Integer initialCollectCount;

    @Schema(description = "是否显示收藏数(0-否 1-是)", example = "1")
    private Integer showCollectCount;

    @Schema(description = "是否支持分享(0-否 1-是)", example = "1")
    private Integer enableShare;

    @Schema(description = "初始分享数", example = "0")
    private Integer initialShareCount;

    @Schema(description = "是否显示分享数(0-否 1-是)", example = "1")
    private Integer showShareCount;

    @Schema(description = "是否支持下载(0-否 1-是)", example = "1")
    private Integer enableDownload;

    @Schema(description = "是否支持报名(0-否 1-是)", example = "0")
    private Integer enableRegister;

    @Schema(description = "是否允许移动端发布(0-否 1-是)", example = "0")
    private Integer enableMobilePublish;

    @Schema(description = "关联商品ID列表", example = "[1, 2, 3]")
    private List<Long> productIds;

}
