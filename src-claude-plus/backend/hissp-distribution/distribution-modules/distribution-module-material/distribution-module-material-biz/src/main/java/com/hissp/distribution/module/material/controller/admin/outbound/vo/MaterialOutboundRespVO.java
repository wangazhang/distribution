package com.hissp.distribution.module.material.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 物料出库 Response VO")
@Data
public class MaterialOutboundRespVO {

    @Schema(description = "出库单ID", example = "1024")
    private Long id;

    @Schema(description = "出库单号", example = "OUT202401010001")
    private String outboundNo;

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String userNickname;

    @Schema(description = "收货地址ID", example = "1024")
    private Long addressId;

    @Schema(description = "收货人姓名", example = "张三")
    private String receiverName;

    @Schema(description = "收货人手机号", example = "18888888888")
    private String receiverMobile;

    @Schema(description = "收货人省份", example = "广东省")
    private String receiverProvince;

    @Schema(description = "收货人城市", example = "深圳市")
    private String receiverCity;

    @Schema(description = "收货人区县", example = "南山区")
    private String receiverDistrict;

    @Schema(description = "收货人详细地址", example = "科技园南区")
    private String receiverDetailAddress;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "状态名称", example = "待审核")
    private String statusName;

    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    @Schema(description = "物流单号", example = "SF1234567890")
    private String logisticsCode;

    @Schema(description = "物流公司", example = "顺丰速运")
    private String logisticsCompany;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "审核人ID", example = "1024")
    private Long approveUserId;

    @Schema(description = "审核人姓名", example = "管理员")
    private String approveUserName;

    @Schema(description = "发货时间")
    private LocalDateTime shipTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因", example = "用户主动取消")
    private String cancelReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "申请人", example = "张三")
    private String createBy;

    @Schema(description = "出库明细列表")
    private List<MaterialOutboundItemRespVO> items;

    @Schema(description = "物料出库明细")
    @Data
    public static class MaterialOutboundItemRespVO {

        @Schema(description = "明细ID", example = "1024")
        private Long id;

        @Schema(description = "物料ID", example = "1024")
        private Long materialId;

        @Schema(description = "物料名称", example = "胶原蛋白")
        private String materialName;

        @Schema(description = "物料编码", example = "MAT001")
        private String materialCode;

        @Schema(description = "出库数量", example = "10")
        private Integer quantity;

        @Schema(description = "基础单位", example = "瓶")
        private String baseUnit;
    }

}