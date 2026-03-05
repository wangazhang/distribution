package com.hissp.distribution.module.promotion.controller.app.cms.vo.comment;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "App - CMS文章评论分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppCmsArticleCommentPageReqVO extends PageParam {

    @Schema(description = "文章ID", example = "1")
    private Long articleId;

    @Schema(description = "父评论ID,0表示一级评论", example = "0")
    private Long parentId;

    @Schema(description = "用户ID", example = "1", hidden = true)
    private Long userId;

}
