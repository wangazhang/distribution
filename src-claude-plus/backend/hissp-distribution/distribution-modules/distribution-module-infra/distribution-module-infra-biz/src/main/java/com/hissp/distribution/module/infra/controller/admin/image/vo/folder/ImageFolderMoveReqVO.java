package com.hissp.distribution.module.infra.controller.admin.image.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

@Schema(description = "管理后台 - 图片文件夹移动 Request VO")
@Data
public class ImageFolderMoveReqVO {

    @Schema(description = "文件夹ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "文件夹ID不能为空")
    private Long id;

    @Schema(description = "目标父文件夹ID，0表示移动到根目录", example = "0")
    private Long targetParentId;

}