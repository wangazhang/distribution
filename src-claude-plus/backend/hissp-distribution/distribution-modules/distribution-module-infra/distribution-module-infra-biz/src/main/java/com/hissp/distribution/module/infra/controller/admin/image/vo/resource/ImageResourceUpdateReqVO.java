package com.hissp.distribution.module.infra.controller.admin.image.vo.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理后台 - 图片资源更新 Request VO")
@Data
public class ImageResourceUpdateReqVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "编号不能为空")
    private Long id;

    @Schema(description = "图片名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品主图")
    @NotBlank(message = "图片名称不能为空")
    @Size(max = 255, message = "图片名称长度不能超过255个字符")
    private String name;

    @Schema(description = "所属文件夹ID", example = "1")
    private Long folderId;

    @Schema(description = "图片标签", example = "商品,主图")
    @Size(max = 500, message = "图片标签长度不能超过500个字符")
    private String tags;

}