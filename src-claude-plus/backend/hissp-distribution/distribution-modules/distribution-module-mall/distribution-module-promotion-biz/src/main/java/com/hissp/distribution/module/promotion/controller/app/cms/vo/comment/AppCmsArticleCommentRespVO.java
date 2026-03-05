package com.hissp.distribution.module.promotion.controller.app.cms.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "App - CMS文章评论 Response VO")
@Data
public class AppCmsArticleCommentRespVO {

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "文章ID", example = "1")
    private Long articleId;

    @Schema(description = "文章标题", example = "如何学习Vue3")
    private String articleTitle;

    @Schema(description = "文章副标题", example = "从零到精通")
    private String articleSubtitle;

    @Schema(description = "文章封面图", example = "https://xxx.jpg")
    private String articleCover;

    @Schema(description = "评论者用户ID", example = "1")
    private Long userId;

    @Schema(description = "评论者昵称", example = "张三")
    private String userNickname;

    @Schema(description = "评论者头像", example = "https://xxx.jpg")
    private String userAvatar;

    @Schema(description = "是否匿名", example = "false")
    private Boolean anonymous;

    @Schema(description = "父评论ID,0表示一级评论", example = "0")
    private Long parentId;

    @Schema(description = "评论内容", example = "写得真不错!")
    private String content;

    @Schema(description = "评论图片数组", example = "[\"https://xxx.jpg\"]")
    private List<String> picUrls;

    @Schema(description = "是否可见", example = "true")
    private Boolean visible;

    @Schema(description = "回复数量", example = "5")
    private Integer replyCount;

    @Schema(description = "点赞数", example = "10")
    private Integer likeCount;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

}
