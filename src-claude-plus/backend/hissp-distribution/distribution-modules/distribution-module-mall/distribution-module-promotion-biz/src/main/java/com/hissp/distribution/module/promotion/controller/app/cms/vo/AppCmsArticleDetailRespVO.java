package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 用户端 - CMS文章详情 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS文章详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCmsArticleDetailRespVO extends AppCmsArticleRespVO {

    @Schema(description = "文章内容(富文本/Markdown)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    @Schema(description = "内容类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "richtext")
    private String contentType;

    @Schema(description = "是否已点赞", example = "false")
    private Boolean hasLiked;

    @Schema(description = "是否已收藏", example = "false")
   private Boolean hasCollected;

    @Schema(description = "是否允许移动端发布", example = "0")
   private Integer enableMobilePublish;

    @Schema(description = "关联商品ID列表", example = "[1, 2, 3]")
    private java.util.List<Long> productIds;

    @Schema(description = "关联商品信息列表")
    private java.util.List<AppProductSimpleRespVO> products;

}
