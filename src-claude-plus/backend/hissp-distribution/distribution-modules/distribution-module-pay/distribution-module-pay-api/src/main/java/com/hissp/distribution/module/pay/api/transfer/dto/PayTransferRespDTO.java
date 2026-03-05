package com.hissp.distribution.module.pay.api.transfer.dto;

import com.hissp.distribution.module.pay.enums.transfer.PayTransferStatusEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class PayTransferRespDTO {

    /**
     * 编号
     */
    private Long id;

    /**
     * 转账单号
     */
    private String no;

    /**
     * 转账金额，单位：分
     */
    private Integer price;

    /**
     * 转账状态
     *
     * 枚举 {@link PayTransferStatusEnum}
     */
    private Integer status;

    /**
     * 渠道转账单号
     *
     * <p>用于财务在渠道后台追踪流水。</p>
     */
    private String channelTransferNo;

    /**
     * 渠道错误码
     *
     * <p>出现异常时保留渠道返回的错误编码，便于排查。</p>
     */
    private String channelErrorCode;

    /**
     * 渠道错误提示
     *
     * <p>渠道的原始报错信息，会在 PC 端提示给财务。</p>
     */
    private String channelErrorMsg;

    /**
     * 渠道原始通知数据(JSON)
     *
     * <p>保留渠道回调/同步时的原文，用于阶段可视化。</p>
     */
    private String channelNotifyData;

    /**
     * 渠道扩展参数
     *
     * <p>例如首信易子商户号、银行卡信息等。</p>
     */
    private Map<String, String> channelExtras;

    /**
     * 转账成功时间
     */
    private LocalDateTime successTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间（同步时间）
     */
    private LocalDateTime updateTime;

}
