package com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * CMS标签 Base VO
 *
 * @author 芋道源码
 */
@Data
public class CmsTagBaseVO {

    @Schema(description = "标签名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "Spring Boot")
    @NotBlank(message = "标签名称不能为空")
    private String name;

    @Schema(description = "排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "排序不能为空")
    private Integer sort;

    @Schema(description = "状态(0-禁用 1-启用)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
