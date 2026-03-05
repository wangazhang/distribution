package com.hissp.distribution.module.material.controller.app.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "APP - 物料出库 Response VO")
@Data
public class AppMaterialOutboundRespVO {

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

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

    @Schema(description = "更新时间", required = true)
    private LocalDateTime updateTime;

    // ========== 物料统计信息(列表展示用) ==========

    @Schema(description = "物料种类数", example = "3")
    private Integer itemCount;

    @Schema(description = "物料总数量", example = "100")
    private Integer totalQuantity;

    @Schema(description = "物料图片列表(最多3张)", example = "[\"https://...\", \"https://...\"]")
    private List<String> materialImages;

    @Schema(description = "物料名称列表(最多3个)", example = "[\"胶原蛋白\", \"维生素C\"]")
    private List<String> materialNames;

}