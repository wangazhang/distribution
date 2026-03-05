package com.hissp.distribution.module.infra.controller.admin.image.vo.folder;

import lombok.*;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import com.hissp.distribution.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 图片文件夹分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImageFolderPageReqVO extends PageParam {

    @Schema(description = "文件夹名称", example = "商品图片")
    private String name;

    @Schema(description = "父文件夹ID", example = "1")
    private Long parentId;

    @Schema(description = "权限类型", example = "1")
    private Integer permissionType;

    @Schema(description = "创建者", example = "admin")
    private String creator;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}