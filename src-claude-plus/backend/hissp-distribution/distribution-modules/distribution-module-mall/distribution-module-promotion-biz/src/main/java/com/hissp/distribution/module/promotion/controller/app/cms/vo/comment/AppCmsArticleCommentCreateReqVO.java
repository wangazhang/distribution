package com.hissp.distribution.module.promotion.controller.app.cms.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "App - CMS文章评论创建 Request VO")
@Data
public class AppCmsArticleCommentCreateReqVO {

    @Schema(description = "文章ID", required = true, example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @Schema(description = "父评论ID,0表示一级评论", example = "0")
    private Long parentId = 0L;

    @Schema(description = "评论内容", required = true, example = "写得真不错!")
    @NotBlank(message = "评论内容不能为空")
    private String content;

    @Schema(description = "评论图片数组", example = "[\"https://xxx.jpg\"]")
    private List<String> picUrls;

    @Schema(description = "是否匿名", example = "false")
    private Boolean anonymous = false;

}
