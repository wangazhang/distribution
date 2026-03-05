package com.hissp.distribution.module.promotion.controller.admin.cms.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * CMS文章批量审核 Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS文章批量审核 Request VO")
@Data
public class CmsArticleBatchAuditReqVO {

    @Schema(description = "文章ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "文章ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "审核状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "approved")
    @NotBlank(message = "审核状态不能为空")
    private String auditStatus;

    @Schema(description = "审核备注", example = "批量审核通过")
    private String auditRemark;

}