package com.hissp.distribution.module.fulfillmentchannel.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 渠道发货状态批量查询结果
 *
 * <p>数据结构贴合微信 getList 返回值，方便上层直接消费。</p>
 */
@Data
@Accessors(chain = true)
public class ChannelShippingQueryListRespDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 翻页游标
     */
    private String lastIndex;

    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;

    /**
     * 订单列表
     */
    private List<Order> orders = new ArrayList<>();

    @Data
    @Accessors(chain = true)
    public static class Order implements Serializable {

        private static final long serialVersionUID = 1L;

        private String transactionId;
        private String merchantId;
        private String subMerchantId;
        private String merchantTradeNo;
        private String description;
        private Long paidAmount;
        private String openId;
        private Long tradeCreateTime;
        private Long payTime;
        private Integer orderState;
        private Boolean inComplaint;
        private Shipping shipping;
    }

    @Data
    @Accessors(chain = true)
    public static class Shipping implements Serializable {

        private static final long serialVersionUID = 1L;

        private Integer deliveryMode;
        private Integer logisticsType;
        private Boolean finishShipping;
        private String goodsDesc;
        private Integer finishShippingCount;
        private List<ShippingItem> shippingList = new ArrayList<>();
    }

    @Data
    @Accessors(chain = true)
    public static class ShippingItem implements Serializable {

        private static final long serialVersionUID = 1L;

        private String trackingNo;
        private String expressCompany;
        private String goodsDesc;
        private Long uploadTime;
        private Contact contact;
    }

    @Data
    @Accessors(chain = true)
    public static class Contact implements Serializable {

        private static final long serialVersionUID = 1L;

        private String consignorContact;
        private String receiverContact;
    }
}
