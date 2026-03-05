package com.hissp.distribution.module.material.controller.app.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "APP - 物料出库详情 Response VO")
@Data
public class AppMaterialOutboundDetailRespVO {

    @Schema(description = "出库单ID", required = true, example = "1024")
    private Long id;

    @Schema(description = "出库单号", required = true, example = "OUT20240918001")
    private String outboundNo;

    @Schema(description = "用户ID", required = true, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "状态：1待审核 2审核通过 3审核拒绝 4已发货 5已签收 6已取消", 
            required = true, example = "1")
    private Integer status;

    @Schema(description = "状态名称", required = true, example = "待审核")
    private String statusName;

    @Schema(description = "收货人姓名", required = true, example = "张三")
    private String receiverName;

    @Schema(description = "收货人手机号", required = true, example = "18888888888")
    private String receiverMobile;

    @Schema(description = "收货人省份", required = true, example = "广东省")
    private String receiverProvince;

    @Schema(description = "收货人城市", required = true, example = "深圳市")
    private String receiverCity;

    @Schema(description = "收货人区县", required = true, example = "南山区")
    private String receiverDistrict;

    @Schema(description = "收货人详细地址", required = true, example = "科技园南区")
    private String receiverDetailAddress;

    @Schema(description = "完整收货地址", required = true, example = "广东省深圳市南山区科技园南区")
    private String fullAddress;

    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    @Schema(description = "快递公司", example = "顺丰速运")
    private String logisticsCompany;

    @Schema(description = "快递单号", example = "SF1234567890")
    private String trackingNumber;

    @Schema(description = "发货时间")
    private LocalDateTime shippedTime;

    @Schema(description = "签收时间")
    private LocalDateTime receivedTime;

    @Schema(description = "审核意见", example = "审核通过")
    private String approveRemark;

    @Schema(description = "审核时间")
    private LocalDateTime approveTime;

    @Schema(description = "发货时间")
    private LocalDateTime shipTime;

    @Schema(description = "完成时间")
    private LocalDateTime completeTime;

    @Schema(description = "取消时间")
    private LocalDateTime cancelTime;

    @Schema(description = "取消原因")
    private String cancelReason;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

    @Schema(description = "出库明细列表", required = true)
    private List<AppMaterialOutboundItemRespVO> items;

    @Schema(description = "APP - 物料出库明细 Response VO")
    @Data
    public static class AppMaterialOutboundItemRespVO {

        @Schema(description = "明细ID", required = true, example = "1024")
        private Long id;

        @Schema(description = "物料ID", required = true, example = "1024")
        private Long materialId;

        @Schema(description = "物料名称", required = true, example = "胶原蛋白")
        private String materialName;

        @Schema(description = "物料图片", example = "https://...")
        private String materialImage;

        @Schema(description = "物料规格", example = "500g/瓶")
        private String materialSpec;

        @Schema(description = "物料单位", example = "瓶")
        private String materialUnit;

        @Schema(description = "出库数量", required = true, example = "10")
        private Integer quantity;

        @Schema(description = "备注", example = "加急处理")
        private String remark;
    }

}