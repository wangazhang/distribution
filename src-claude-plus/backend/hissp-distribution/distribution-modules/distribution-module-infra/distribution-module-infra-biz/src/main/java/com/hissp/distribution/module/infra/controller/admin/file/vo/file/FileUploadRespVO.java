package com.hissp.distribution.module.infra.controller.admin.file.vo.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 文件上传 Response VO")
@Data
public class FileUploadRespVO {

    @Schema(description = "文件ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "文件URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://example.com/file.jpg")
    private String url;

    @Schema(description = "文件名", example = "file.jpg")
    private String name;

}