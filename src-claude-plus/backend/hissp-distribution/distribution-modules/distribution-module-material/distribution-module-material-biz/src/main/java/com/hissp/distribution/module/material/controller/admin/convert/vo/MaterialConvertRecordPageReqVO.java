package com.hissp.distribution.module.material.controller.admin.convert.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 物料转化记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialConvertRecordPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

    @Schema(description = "转化规则ID", example = "1024")
    private Long ruleId;

    @Schema(description = "源物料ID", example = "1024")
    private Long sourceMaterialId;

    @Schema(description = "目标物料ID", example = "2048")
    private Long targetMaterialId;

    @Schema(description = "转化状态：1成功 2失败", example = "1")
    private Integer status;

    @Schema(description = "关联订单ID", example = "1024")
    private Long orderId;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}