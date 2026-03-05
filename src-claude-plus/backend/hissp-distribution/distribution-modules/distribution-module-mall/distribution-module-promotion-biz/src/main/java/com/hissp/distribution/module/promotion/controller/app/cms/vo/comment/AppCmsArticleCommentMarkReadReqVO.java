package com.hissp.distribution.module.promotion.controller.app.cms.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Schema(description = "App - CMS文章评论标记已读 Request VO")
@Data
public class AppCmsArticleCommentMarkReadReqVO {

    @Schema(description = "评论ID列表", required = true, example = "[1, 2, 3]")
    @NotEmpty(message = "评论ID列表不能为空")
    private List<Long> ids;

}
