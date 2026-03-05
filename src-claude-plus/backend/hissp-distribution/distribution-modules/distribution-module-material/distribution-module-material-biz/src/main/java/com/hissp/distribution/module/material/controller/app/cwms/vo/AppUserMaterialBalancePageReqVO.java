package com.hissp.distribution.module.material.controller.app.cwms.vo;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.hissp.distribution.framework.common.pojo.PageParam;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "App - 用户物料余额事实表，记录用户每种物料的余额信息分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppUserMaterialBalancePageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "6122")
    private Long userId;

    @Schema(description = "物料ID（关联mb_material表）", example = "19745")
    private Long materialId;

    @Schema(description = "物料余额，单位为分")
    private Integer balance;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    // 明确提供 setter，避免 Lombok 生成异常
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

