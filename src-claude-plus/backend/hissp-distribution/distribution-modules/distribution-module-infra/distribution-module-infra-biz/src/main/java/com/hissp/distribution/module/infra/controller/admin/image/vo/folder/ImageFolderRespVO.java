package com.hissp.distribution.module.infra.controller.admin.image.vo.folder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 图片文件夹 Response VO")
@Data
public class ImageFolderRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "文件夹名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "商品图片")
    private String name;

    @Schema(description = "父文件夹ID", example = "1")
    private Long parentId;

    @Schema(description = "文件夹层级", example = "2")
    private Integer level;

    @Schema(description = "完整路径", requiredMode = Schema.RequiredMode.REQUIRED, example = "/商品图片")
    private String path;

    @Schema(description = "权限类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer permissionType;

    @Schema(description = "权限类型名称", example = "共享")
    private String permissionTypeName;

    @Schema(description = "排序", example = "1")
    private Integer sortOrder;

    @Schema(description = "备注", example = "用于存放商品相关图片")
    private String remark;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime updateTime;

    @Schema(description = "子文件夹数量", example = "5")
    private Integer childrenCount;

    @Schema(description = "图片数量", example = "120")
    private Integer imageCount;

    @Schema(description = "子文件夹列表")
    private java.util.List<ImageFolderRespVO> children;

}