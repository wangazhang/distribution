package com.hissp.distribution.module.material.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料出库响应 DTO
 */
@Data
public class MaterialOutboundRespDTO {

    /**
     * 出库单ID
     */
    private Long id;

    /**
     * 出库单号
     */
    private String outboundNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String userNickname;

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
     * 状态
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 物流单号
     */
    private String logisticsCode;

    /**
     * 物流公司
     */
    private String logisticsCompany;

    /**
     * 审核时间
     */
    private LocalDateTime approveTime;

    /**
     * 审核人ID
     */
    private Long approveUserId;

    /**
     * 审核人姓名
     */
    private String approveUserName;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 申请人
     */
    private String createBy;

    /**
     * 出库明细列表
     */
    private List<MaterialOutboundItemRespDTO> items;

    /**
     * 物料出库明细响应 DTO
     */
    @Data
    public static class MaterialOutboundItemRespDTO {

        /**
         * 明细ID
         */
        private Long id;

        /**
         * 物料ID
         */
        private Long materialId;

        /**
         * 物料名称
         */
        private String materialName;

        /**
         * 物料编码
         */
        private String materialCode;

        /**
         * 出库数量
         */
        private Integer quantity;

        /**
         * 基础单位
         */
        private String baseUnit;
    }

}