package com.hissp.distribution.module.pay.controller.admin.order.vo;

import com.hissp.distribution.framework.pay.core.enums.order.PayOrderDeliveryStatusRespEnum;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 管理后台 - 支付订单渠道查询结果 VO
 *
 * <p>封装渠道返回的核心字段，方便前端直接展示，并附带原始报文用于排查。</p>
 */
@Data
public class PayOrderChannelRespVO {

    @Schema(description = "渠道状态编码", example = "10")
    private Integer status;

    @Schema(description = "渠道状态名称", example = "支付成功")
    private String statusName;

    @Schema(description = "妥投状态编码", example = "DELIVERED")
    private String deliveryStatus;

    @Schema(description = "妥投状态名称", example = "已妥投")
    private String deliveryStatusName;

    @Schema(description = "商户订单号", example = "MR123456789")
    private String outTradeNo;

    @Schema(description = "渠道订单号", example = "4200001234")
    private String channelOrderNo;

    @Schema(description = "渠道流水号", example = "4200001234")
    private String channelTransactionNo;

    @Schema(description = "渠道用户编号", example = "openid_xxx")
    private String channelUserId;

    @Schema(description = "支付成功时间")
    private LocalDateTime successTime;

    @Schema(description = "渠道原始返回")
    private Object rawData;

    public void fillStatusName() {
        this.statusName = "未知";
        if (this.status == null) {
            return;
        }
        for (PayOrderStatusRespEnum value : PayOrderStatusRespEnum.values()) {
            if (value.getStatus().equals(this.status)) {
                this.statusName = value.getName();
                return;
            }
        }
    }

    public void fillDeliveryStatusName() {
        if (this.deliveryStatus == null) {
            this.deliveryStatusName = null;
            return;
        }
        PayOrderDeliveryStatusRespEnum statusEnum = PayOrderDeliveryStatusRespEnum.from(this.deliveryStatus);
        this.deliveryStatusName = statusEnum != null ? statusEnum.getName() : null;
    }
}
