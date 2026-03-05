package com.hissp.distribution.module.material.controller.app.cwms.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "APP - 物料变更记录分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppMaterialRecordPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "21737")
    private Long userId;

    @Schema(description = "物料ID（关联mb_material表）", example = "28432")
    private Long materialId;

    @Schema(description = "操作类型（0-获得 1-使用）", example = "1")
    private Integer actionType;

    @Schema(description = "操作数量")
    private Integer amount;

    @Schema(description = "来源类型（0-订单获得 1-活动赠送 2-手动添加）", example = "2")
    private Integer sourceType;

    @Schema(description = "来源ID")
    private String sourceId;

    @Schema(description = "来源描述")
    private String sourceDesc;

    @Schema(description = "变更前数量")
    private Integer beforeAmount;

    @Schema(description = "变更后数量")
    private Integer afterAmount;

    @Schema(description = "业务发生日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] actionDate;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;
}

