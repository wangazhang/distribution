package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo;

import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.hissp.distribution.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 补货价格表：记录不同用户等级对应的商品补货价格分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialRestockPricePageReqVO extends PageParam {

    @Schema(description = "商品ID", example = "21675")
    private Long productId;

    @Schema(description = "用户等级ID", example = "3")
    private Long levelId;

    @Schema(description = "补货价格（单位：分）", example = "16931")
    private Integer restockPrice;

    @Schema(description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
