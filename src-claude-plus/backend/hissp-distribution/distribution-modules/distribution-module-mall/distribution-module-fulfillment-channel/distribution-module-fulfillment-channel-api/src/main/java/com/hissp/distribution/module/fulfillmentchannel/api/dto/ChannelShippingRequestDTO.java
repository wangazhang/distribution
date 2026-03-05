package com.hissp.distribution.module.fulfillmentchannel.api.dto;

import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingChannelEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 渠道发货请求
 */
@Data
public class ChannelShippingRequestDTO implements Serializable {

    /**
     * 渠道类型，默认微信小程序
     */
    private Integer channel = ChannelShippingChannelEnum.WECHAT_MINIAPP.getCode();

    /**
     * 交易订单编号
     */
    @NotNull(message = "交易订单编号不能为空")
    private Long orderId;

    /**
     * 交易订单号
     */
    @NotBlank(message = "交易订单号不能为空")
    private String orderNo;

    /**
     * 支付订单编号
     */
    @NotNull(message = "支付订单编号不能为空")
    private Long payOrderId;

    /**
     * 用户编号
     */
    @NotNull(message = "用户编号不能为空")
    private Long userId;

    /**
     * 物流模式
     */
    private Integer logisticsType;

    /**
     * 是否全部发货，仅在分拆模式下生效
     */
    private Boolean allDelivered = Boolean.TRUE;

    /**
     * 快递公司编码
     */
    private String logisticsCompanyCode;

    /**
     * 物流单号
     */
    private String logisticsNo;

    /**
     * 商品信息描述
     */
    private String itemDesc;

    /**
     * 收件人联系方式（用于微信校验，可为空）
     */
    private String receiverContact;

    /**
     * 发货商品项
     */
    @NotEmpty(message = "发货商品项不能为空")
    @Valid
    private List<Item> items;

    /**
     * 发货备注
     */
    private String remark;

    @Data
    public static class Item implements Serializable {

        @NotNull(message = "订单项编号不能为空")
        private Long orderItemId;

        @NotNull(message = "SPU 编号不能为空")
        private Long spuId;

        @NotBlank(message = "商品名称不能为空")
        private String spuName;

        @NotNull(message = "商品类型不能为空")
        private Integer productType;

        @NotNull(message = "数量不能为空")
        private Integer count;
    }
}
