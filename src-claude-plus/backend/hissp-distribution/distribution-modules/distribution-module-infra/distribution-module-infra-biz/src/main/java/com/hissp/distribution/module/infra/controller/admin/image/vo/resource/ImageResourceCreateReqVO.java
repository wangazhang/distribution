package com.hissp.distribution.module.infra.controller.admin.image.vo.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 图片资源创建 Request VO")
@Data
public class ImageResourceCreateReqVO {

    @Schema(description = "图片名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品主图")
    @NotBlank(message = "图片名称不能为空")
    private String name;

    @Schema(description = "原始文件名", example = "product_image.jpg")
    private String originalName;

    @Schema(description = "所属文件夹ID", example = "1")
    private Long folderId;

    @Schema(description = "图片标签", example = "商品,主图")
    private String tags;

    @Schema(description = "文件URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/image.jpg")
    @NotBlank(message = "文件URL不能为空")
    private String url;

    @Schema(description = "文件大小", example = "1024000")
    private Long fileSize;

    @Schema(description = "图片宽度", example = "1920")
    private Integer width;

    @Schema(description = "图片高度", example = "1080")
    private Integer height;

    @Schema(description = "图片格式", example = "jpg")
    private String format;

}