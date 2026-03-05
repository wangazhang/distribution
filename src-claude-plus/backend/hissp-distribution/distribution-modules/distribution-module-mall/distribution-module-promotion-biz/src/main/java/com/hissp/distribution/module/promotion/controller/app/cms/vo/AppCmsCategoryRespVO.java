package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 - CMS分类 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS分类 Response VO")
@Data
public class AppCmsCategoryRespVO {

    @Schema(description = "分类ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "所属板块ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sectionId;

    @Schema(description = "分类名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Java技术")
    private String name;

    @Schema(description = "分类图标URL", example = "https://example.com/icon.png")
    private String icon;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
