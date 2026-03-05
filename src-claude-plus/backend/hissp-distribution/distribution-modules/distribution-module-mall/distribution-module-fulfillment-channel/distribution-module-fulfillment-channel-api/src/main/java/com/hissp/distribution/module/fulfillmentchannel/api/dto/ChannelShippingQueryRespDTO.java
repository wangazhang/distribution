package com.hissp.distribution.module.fulfillmentchannel.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 渠道发货状态查询结果
 *
 * <p>提供统一的发货状态与扩展数据，扩展数据通过 key 区分不同渠道。</p>
 *
 * @author Codex
 */
@Data
@Accessors(chain = true)
public class ChannelShippingQueryRespDTO implements Serializable {

    public static final String EXTRA_WEIXIN_CONFIRM = "weixinConfirmExtra";

    private static final long serialVersionUID = 1L;

    /**
     * 是否已在渠道完成发货录入
     */
    private boolean shipped;

    /**
     * 渠道返回的确认收货状态
     */
    private ChannelShippingConfirmStatus confirmStatus = ChannelShippingConfirmStatus.UNKNOWN;

    /**
     * 渠道扩展数据
     */
    private Map<String, Object> extras = new HashMap<>();

    public ChannelShippingQueryRespDTO addExtra(String key, Object value) {
        if (value != null) {
            extras.put(key, value);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key, Class<T> type) {
        Object value = extras.get(key);
        return type.isInstance(value) ? (T) value : null;
    }

    public enum ChannelShippingConfirmStatus {
        UNKNOWN,
        UNCONFIRMED,
        CONFIRMED,
        CANCELED
    }
}
