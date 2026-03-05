package com.hissp.distribution.module.material.controller.admin.convert.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料转化记录 Response VO")
@Data
public class MaterialConvertRecordRespVO {

    @Schema(description = "记录ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "用户ID", required = true, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "转化规则ID", required = true, example = "1024")
    private Long ruleId;

    @Schema(description = "转化规则名称", required = true, example = "积分转金币规则")
    private String ruleName;

    @Schema(description = "源物料ID", required = true, example = "1024")
    private Long sourceMaterialId;

    @Schema(description = "源物料名称", required = true, example = "积分")
    private String sourceMaterialName;

    @Schema(description = "源物料单位", example = "个")
    private String sourceMaterialUnit;

    @Schema(description = "目标物料ID", required = true, example = "2048")
    private Long targetMaterialId;

    @Schema(description = "目标物料名称", required = true, example = "金币")
    private String targetMaterialName;

    @Schema(description = "目标物料单位", example = "个")
    private String targetMaterialUnit;

    @Schema(description = "源物料数量", required = true, example = "100")
    private Integer sourceQuantity;

    @Schema(description = "目标物料数量", required = true, example = "150")
    private Integer targetQuantity;

    @Schema(description = "转化费用", example = "10.50")
    private BigDecimal convertPrice;

    @Schema(description = "转化状态：1成功 2失败", required = true, example = "1")
    private Integer status;

    @Schema(description = "状态名称", required = true, example = "转化成功")
    private String statusName;

    @Schema(description = "关联订单ID", example = "1024")
    private Long orderId;

    @Schema(description = "转化原因/备注", example = "用户主动转化")
    private String reason;

    @Schema(description = "失败原因", example = "源物料余额不足")
    private String failureReason;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

}