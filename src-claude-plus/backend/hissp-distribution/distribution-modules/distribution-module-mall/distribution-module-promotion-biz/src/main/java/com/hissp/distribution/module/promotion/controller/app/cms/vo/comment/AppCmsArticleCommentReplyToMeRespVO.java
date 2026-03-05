package com.hissp.distribution.module.promotion.controller.app.cms.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "App - CMS收到的评论和回复 Response VO")
@Data
public class AppCmsArticleCommentReplyToMeRespVO {

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

    @Schema(description = "回复者用户ID", example = "1")
    private Long replierId;

    @Schema(description = "回复者昵称", example = "张三")
    private String replierName;

    @Schema(description = "回复者头像", example = "https://xxx.jpg")
    private String replierAvatar;

    @Schema(description = "回复者是否官方", example = "false")
    private Boolean replierIsOfficial;

    @Schema(description = "回复内容", example = "写得真不错!")
    private String content;

    @Schema(description = "回复图片数组", example = "[\"https://xxx.jpg\"]")
    private List<String> picUrls;

    @Schema(description = "原评论内容", example = "我也这么认为")
    private String originalContent;

    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createTime;

    @Schema(description = "是否已读", example = "false")
    private Boolean readStatus;

}
