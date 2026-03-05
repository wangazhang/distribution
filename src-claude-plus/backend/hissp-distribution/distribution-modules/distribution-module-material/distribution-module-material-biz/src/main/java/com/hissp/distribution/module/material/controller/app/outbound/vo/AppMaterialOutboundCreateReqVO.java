package com.hissp.distribution.module.material.controller.app.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "APP - 物料出库创建 Request VO")
@Data
public class AppMaterialOutboundCreateReqVO {

    @Schema(description = "收货地址ID", example = "1024")
    private Long addressId;

    @Schema(description = "收货人姓名", required = true, example = "张三")
    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @Schema(description = "收货人手机号", required = true, example = "18888888888")
    @NotBlank(message = "收货人手机号不能为空")
    private String receiverMobile;

    @Schema(description = "收货人省份", required = true, example = "广东省")
    @NotBlank(message = "收货人省份不能为空")
    private String receiverProvince;

    @Schema(description = "收货人城市", required = true, example = "深圳市")
    @NotBlank(message = "收货人城市不能为空")
    private String receiverCity;

    @Schema(description = "收货人区县", required = true, example = "南山区")
    @NotBlank(message = "收货人区县不能为空")
    private String receiverDistrict;

    @Schema(description = "收货人详细地址", required = true, example = "科技园南区")
    @NotBlank(message = "收货人详细地址不能为空")
    private String receiverDetailAddress;

    @Schema(description = "备注", example = "请尽快发货")
    private String remark;

    @Schema(description = "出库明细列表", required = true)
    @NotEmpty(message = "出库明细不能为空")
    private List<AppMaterialOutboundItemCreateReqVO> items;

    @Schema(description = "用户ID", hidden = true)
    private Long userId;

    @Schema(description = "APP - 物料出库明细创建 Request VO")
    @Data
    public static class AppMaterialOutboundItemCreateReqVO {

        @Schema(description = "物料ID", required = true, example = "1024")
        @NotNull(message = "物料ID不能为空")
        private Long materialId;

        @Schema(description = "出库数量", required = true, example = "10")
        @NotNull(message = "出库数量不能为空")
        private Integer quantity;

        @Schema(description = "备注", example = "加急处理")
        private String remark;
    }

}