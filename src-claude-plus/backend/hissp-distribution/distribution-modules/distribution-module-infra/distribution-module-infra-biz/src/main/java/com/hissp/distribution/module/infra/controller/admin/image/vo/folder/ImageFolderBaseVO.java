package com.hissp.distribution.module.infra.controller.admin.image.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Size;

@Schema(description = "图片文件夹 Base VO")
@Data
public class ImageFolderBaseVO {

    @Schema(description = "父文件夹ID", example = "1")
    private Long parentId;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "备注", example = "用于存放商品相关图片")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}