package com.hissp.distribution.module.infra.controller.admin.image.vo.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "管理后台 - 图片上传 Request VO")
@Data
public class ImageResourceUploadReqVO {

    @Schema(description = "文件附件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    @Schema(description = "图片名称", example = "商品主图")
    private String name;

    @Schema(description = "所属文件夹ID", example = "1")
    private Long folderId;

    @Schema(description = "图片标签", example = "商品,主图")
    private String tags;

}