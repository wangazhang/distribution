package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端 - CMS文章列表项 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS文章列表项 Response VO")
@Data
public class AppCmsArticleRespVO {

    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "所属板块ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sectionId;

    @Schema(description = "所属分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long categoryId;

    @Schema(description = "文章标题", requiredMode = Schema.RequiredMode.REQUIRED, example = "深入理解Spring Boot自动配置原理")
    private String title;

    @Schema(description = "副标题", example = "从源码角度分析")
    private String subtitle;

    @Schema(description = "封面图片(JSON数组)", example = "[\"https://example.com/cover1.jpg\"]")
    private List<String> coverImages;

    @Schema(description = "作者ID", example = "100")
    private Long authorId;

    @Schema(description = "作者类型", example = "admin")
    private String authorType;

    @Schema(description = "作者名称", example = "张三")
    private String authorName;

    @Schema(description = "作者头像", example = "https://example.com/avatar.jpg")
    private String authorAvatar;

    @Schema(description = "浏览数", example = "1000")
    private Integer viewCount;

    @Schema(description = "点赞数", example = "100")
    private Integer likeCount;

    @Schema(description = "收藏数", example = "50")
    private Integer collectCount;

    @Schema(description = "分享数", example = "20")
    private Integer shareCount;

    @Schema(description = "是否热门(0-否 1-是)", example = "0")
    private Integer isHot;

    @Schema(description = "是否官方(0-否 1-是)", example = "0")
    private Integer isOfficial;

    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    @Schema(description = "是否支持点赞", example = "1")
    private Integer enableLike;

    @Schema(description = "是否显示点赞数", example = "1")
    private Integer showLikeCount;

    @Schema(description = "初始点赞数(假数据,运营设置)", example = "100")
    private Integer initialLikeCount;

    @Schema(description = "是否支持收藏", example = "1")
    private Integer enableCollect;

    @Schema(description = "是否显示收藏数", example = "1")
    private Integer showCollectCount;

    @Schema(description = "初始收藏数(假数据,运营设置)", example = "50")
    private Integer initialCollectCount;

    @Schema(description = "是否支持分享", example = "1")
    private Integer enableShare;

    @Schema(description = "是否显示分享数", example = "1")
    private Integer showShareCount;

    @Schema(description = "初始分享数(假数据,运营设置)", example = "20")
    private Integer initialShareCount;

    @Schema(description = "是否支持下载", example = "1")
    private Integer enableDownload;

    @Schema(description = "是否支持报名(课程类型)", example = "0")
    private Integer enableRegister;

    @Schema(description = "标签列表")
    private List<AppCmsTagRespVO> tags;

    @Schema(description = "板块类型", example = "article")
    private String sectionType;

    @Schema(description = "当前用户是否已点赞", example = "false")
    private Boolean userLiked;

    @Schema(description = "当前用户是否已收藏", example = "false")
    private Boolean userCollected;

    @Schema(description = "关联商品ID列表", example = "[1, 2, 3]")
    private List<Long> productIds;

    @Schema(description = "审核状态", example = "approved")
    private String auditStatus;

    @Schema(description = "发布状态", example = "1")
    private Integer publishStatus;

}
