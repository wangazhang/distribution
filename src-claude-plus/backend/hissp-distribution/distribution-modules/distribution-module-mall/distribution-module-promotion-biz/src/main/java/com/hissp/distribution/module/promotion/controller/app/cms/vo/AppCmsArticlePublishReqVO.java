package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 用户端 CMS文章发布 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS文章发布 Request VO")
@Data
public class AppCmsArticlePublishReqVO {

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

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

    @Schema(description = "关联商品ID列表", example = "[1, 2, 3]")
    private List<Long> productIds;

}