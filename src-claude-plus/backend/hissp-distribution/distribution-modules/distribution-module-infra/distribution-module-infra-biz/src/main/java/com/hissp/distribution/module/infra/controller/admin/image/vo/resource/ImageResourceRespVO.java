package com.hissp.distribution.module.infra.controller.admin.image.vo.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 图片资源 Response VO")
@Data
public class ImageResourceRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "关联文件表ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long fileId;

    @Schema(description = "图片名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品主图")
    private String name;

    @Schema(description = "原始文件名", example = "product_main.jpg")
    private String originalName;

    @Schema(description = "所属文件夹ID", example = "1")
    private Long folderId;

    @Schema(description = "文件夹名称", example = "商品图片")
    private String folderName;

    @Schema(description = "文件大小", example = "102400")
    private Long fileSize;

    @Schema(description = "文件大小（友好显示）", example = "100KB")
    private String fileSizeDisplay;

    @Schema(description = "图片宽度", example = "800")
    private Integer width;

    @Schema(description = "图片高度", example = "600")
    private Integer height;

    @Schema(description = "图片格式", example = "jpg")
    private String format;

    @Schema(description = "图片访问URL", requiredMode = Schema.RequiredMode.REQUIRED, example = "http://example.com/image.jpg")
    private String url;

    @Schema(description = "图片标签", example = "商品,主图")
    private String tags;

    @Schema(description = "下载次数", example = "10")
    private Integer downloadCount;

    @Schema(description = "查看次数", example = "100")
    private Integer viewCount;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

}