package com.hissp.distribution.module.infra.controller.app.file.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

@Schema(description = "用户 App - 高价值图片上传 Request VO")
@Data
public class AppHighValueFileUploadReqVO {

    @Schema(description = "文件附件", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "文件附件不能为空")
    private MultipartFile file;

    @Schema(description = "子路径", example = "front",
           implementation = String.class,
           allowableValues = {"front", "back", "avatar"})
    private String subPath;

}