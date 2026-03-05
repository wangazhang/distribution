package com.hissp.distribution.module.infra.controller.admin.image.vo.resource;

import lombok.*;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import com.hissp.distribution.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 图片资源分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImageResourcePageReqVO extends PageParam {

    @Schema(description = "图片名称", example = "商品主图")
    private String name;

    @Schema(description = "原始文件名", example = "product_main.jpg")
    private String originalName;

    @Schema(description = "所属文件夹ID", example = "1")
    private Long folderId;

    @Schema(description = "图片格式", example = "jpg")
    private String format;

    @Schema(description = "图片标签", example = "商品,主图")
    private String tags;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "文件大小范围", example = "[1024, 1048576]")
    private Long[] fileSize;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}