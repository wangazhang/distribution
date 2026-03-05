package com.hissp.distribution.module.material.controller.app.outbound.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.hissp.distribution.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "APP - 物料出库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AppMaterialOutboundPageReqVO extends PageParam {

    @Schema(description = "出库单号", example = "OUT20240918001")
    private String outboundNo;

    @Schema(description = "状态：1待审核 2审核通过 3审核拒绝 4已发货 5已签收 6已取消", example = "1")
    private Integer status;

    @Schema(description = "物料ID", example = "1024")
    private Long materialId;

    @Schema(description = "收货人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "收货人手机号", example = "18888888888")
    private String receiverMobile;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

    @Schema(description = "用户ID", hidden = true)
    private Long userId;

}