package com.hissp.distribution.module.promotion.controller.admin.cms.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * CMS分类 Base VO
 *
 * @author 芋道源码
 */
@Data
public class CmsCategoryBaseVO {

    @Schema(description = "所属板块ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "所属板块ID不能为空")
    private Long sectionId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java技术")
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @Schema(description = "分类图标URL", example = "https://example.com/icon.png")
    private String icon;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态(0-禁用 1-启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
