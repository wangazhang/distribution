package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户端 - CMS用户行为 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS用户行为 Request VO")
@Data
public class AppCmsUserActionReqVO {

    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @Schema(description = "行为类型(like-点赞 collect-收藏)", requiredMode = Schema.RequiredMode.REQUIRED, example = "like")
    @NotBlank(message = "行为类型不能为空")
    private String actionType;

}
