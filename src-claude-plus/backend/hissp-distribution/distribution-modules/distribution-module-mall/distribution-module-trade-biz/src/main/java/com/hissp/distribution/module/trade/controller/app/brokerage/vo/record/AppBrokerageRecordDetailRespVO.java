package com.hissp.distribution.module.trade.controller.app.brokerage.vo.record;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "用户 App - 分销记录详情 Response VO")
@Data
public class AppBrokerageRecordDetailRespVO {

    @Schema(description = "记录编号", example = "1024")
    private Long id;

    @Schema(description = "业务编号", example = "NO202401010001")
    private String bizId;

    @Schema(description = "业务类型", example = "1")
    private Integer bizType;

    @Schema(description = "业务类型名称", example = "推广佣金")
    private String bizTypeName;

    @Schema(description = "业务大类", example = "1")
    private Integer bizCategory;

    @Schema(description = "分销标题", example = "用户下单")
    private String title;

    @Schema(description = "分销描述", example = "用户下单，获得 10.00 元佣金")
    private String description;

    @Schema(description = "分销金额，单位：分", example = "1000")
    private Integer price;

    @Schema(description = "当前状态", example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "待结算")
    private String statusName;

    @Schema(description = "冻结天数", example = "7")
    private Integer frozenDays;

    @Schema(description = "解冻时间", example = "2024-01-01T00:00:00")
    private LocalDateTime unfreezeTime;

    @Schema(description = "记录创建时间", example = "2024-01-01T00:00:00")
    private LocalDateTime createTime;

    @Schema(description = "记录更新时间", example = "2024-01-02T00:00:00")
    private LocalDateTime updateTime;

    @Schema(description = "来源用户编号", example = "888")
    private Long sourceUserId;

    @Schema(description = "来源用户昵称", example = "小王")
    private String sourceUserNickname;

    @Schema(description = "来源用户头像")
    private String sourceUserAvatar;

    @Schema(description = "来源用户与当前用户的层级", example = "1")
    private Integer sourceUserLevel;
}
