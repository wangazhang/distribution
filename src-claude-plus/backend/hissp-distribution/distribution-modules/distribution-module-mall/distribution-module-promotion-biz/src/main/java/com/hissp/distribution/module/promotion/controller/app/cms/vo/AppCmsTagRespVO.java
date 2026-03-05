package com.hissp.distribution.module.promotion.controller.app.cms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端 - CMS标签 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "用户端 - CMS标签 Response VO")
@Data
public class AppCmsTagRespVO {

    @Schema(description = "标签ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Spring Boot")
    private String name;

    @Schema(description = "排序", example = "1")
    private Integer sort;

}
