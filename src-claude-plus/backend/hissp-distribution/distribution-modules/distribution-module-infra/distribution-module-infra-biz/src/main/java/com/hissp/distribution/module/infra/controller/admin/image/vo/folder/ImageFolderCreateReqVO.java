package com.hissp.distribution.module.infra.controller.admin.image.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理后台 - 图片文件夹创建 Request VO")
@Data
public class ImageFolderCreateReqVO {

    @Schema(description = "文件夹名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品图片")
    @NotBlank(message = "文件夹名称不能为空")
    @Size(max = 100, message = "文件夹名称长度不能超过100个字符")
    private String name;

    @Schema(description = "父文件夹ID", example = "1")
    private Long parentId;

    @Schema(description = "权限类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "备注", example = "用于存放商品相关图片")
    private String remark;

}