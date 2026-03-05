package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 物料出库创建请求 DTO
 */
@Data
public class MaterialOutboundCreateReqDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 收货地址ID
     */
    private Long addressId;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机号
     */
    private String receiverMobile;

    /**
     * 收货人省份
     */
    private String receiverProvince;

    /**
     * 收货人城市
     */
    private String receiverCity;

    /**
     * 收货人区县
     */
    private String receiverDistrict;

    /**
     * 收货人详细地址
     */
    private String receiverDetailAddress;

    /**
     * 备注
     */
    private String remark;

    /**
     * 出库明细列表
     */
    @NotEmpty(message = "出库明细不能为空")
    @Valid
    private List<MaterialOutboundItemCreateReqDTO> items;

    /**
     * 物料出库明细创建请求 DTO
     */
    @Data
    public static class MaterialOutboundItemCreateReqDTO {

        /**
         * 物料ID
         */
        @NotNull(message = "物料ID不能为空")
        private Long materialId;

        /**
         * 出库数量
         */
        @NotNull(message = "出库数量不能为空")
        @Positive(message = "出库数量必须大于0")
        private Integer quantity;
    }

}