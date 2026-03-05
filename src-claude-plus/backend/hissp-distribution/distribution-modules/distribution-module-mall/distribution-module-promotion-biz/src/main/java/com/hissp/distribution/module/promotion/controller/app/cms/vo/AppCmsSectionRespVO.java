package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 用户端 - CMS板块 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS板块 Response VO")
@Data
public class AppCmsSectionRespVO {

    @Schema(description = "板块ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "板块名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "技术分享")
    private String name;

    @Schema(description = "板块类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "article")
    private String type;

    @Schema(description = "排版样式", example = "list")
    private String layoutStyle;

    @Schema(description = "封面展示类型", example = "single")
    private String coverDisplayType;

    @Schema(description = "板块配置(JSON)", example = "{\"showButton\": true}")
    private Map<String, Object> config;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
