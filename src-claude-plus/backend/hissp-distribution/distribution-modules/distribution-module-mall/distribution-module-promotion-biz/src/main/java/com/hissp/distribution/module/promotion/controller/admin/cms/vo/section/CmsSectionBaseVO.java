package com.hissp.distribution.module.promotion.controller.admin.cms.vo.section;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

/**
 * CMS板块 Base VO
 *
 * @author 芋道源码
 */
@Data
public class CmsSectionBaseVO {

    @Schema(description = "板块名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "技术分享")
    @NotBlank(message = "板块名称不能为空")
    private String name;

    @Schema(description = "板块类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "article")
    @NotBlank(message = "板块类型不能为空")
    private String type;

    @Schema(description = "排版样式", example = "list")
    private String layoutStyle;

    @Schema(description = "封面展示类型", example = "single")
    private String coverDisplayType;

    @Schema(description = "板块配置(JSON)", example = "{\"showButton\": true}")
    private Map<String, Object> config;

    @Schema(description = "是否需要审核", example = "true")
    private Boolean requireAudit;

    @Schema(description = "自动审核是否启用", example = "true")
    private Boolean autoAuditEnabled;

    @Schema(description = "自动审核延迟(分钟)", example = "30")
    private Integer autoAuditDelayMinutes;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态(0-禁用 1-启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
