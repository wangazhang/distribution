package com.hissp.distribution.module.promotion.controller.admin.cms.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * CMS文章审核 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS文章审核 Request VO")
@Data
public class CmsArticleAuditReqVO {

    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long id;

    @Schema(description = "审核状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "approved")
    @NotBlank(message = "审核状态不能为空")
    private String auditStatus;

    @Schema(description = "审核备注", example = "审核通过")
    private String auditRemark;

}
