package com.hissp.distribution.module.infra.controller.admin.image.vo.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "管理后台 - 图片资源批量移动 Request VO")
@Data
public class ImageResourceBatchMoveReqVO {

    @Schema(description = "图片资源ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "图片资源ID列表不能为空")
    private List<Long> ids;

    @Schema(description = "目标文件夹ID，0表示移动到根目录", example = "0")
    @NotNull(message = "目标文件夹ID不能为空")
    private Long targetFolderId;

}