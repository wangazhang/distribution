package com.hissp.distribution.module.material.controller.admin.inbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料入库 Response VO")
@Data
public class MaterialInboundRespVO {

    @Schema(description = "入库单ID", required = true, example = "1")
    private Long id;

    @Schema(description = "入库单号", required = true, example = "INB202312010001")
    private String inboundNo;

    @Schema(description = "申请人ID", required = true, example = "1024")
    private Long applicantId;

    @Schema(description = "申请人姓名", required = true, example = "张三")
    private String applicantName;

    @Schema(description = "物料ID", required = true, example = "2048")
    private Long materialId;

    @Schema(description = "物料名称", required = true, example = "胶原蛋白")
    private String materialName;

    @Schema(description = "入库数量", required = true, example = "100")
    private Integer quantity;

    @Schema(description = "单价（分）", required = true, example = "1000")
    private Integer unitPrice;

    @Schema(description = "总金额（分）", required = true, example = "100000")
    private Integer totalAmount;

    @Schema(description = "供应商", required = true, example = "XX供应商")
    private String supplier;

    @Schema(description = "入库原因", required = true, example = "新采购入库")
    private String reason;

    @Schema(description = "状态", required = true, example = "1")
    private Integer status;

    @Schema(description = "状态描述", required = true, example = "已审核")
    private String statusDesc;

    @Schema(description = "审核人ID", example = "2048")
    private Long approveUserId;

    @Schema(description = "审核人姓名", example = "李四")
    private String approveUserName;

    @Schema(description = "审核意见", example = "审核通过")
    private String approveReason;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}