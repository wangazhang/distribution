package com.hissp.distribution.module.pay.dal.dataobject.request;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * 通用渠道请求 DO
 *
 * @author Cascade
 */
@TableName("pay_channel_request")
@KeySequence("pay_channel_request_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayChannelRequestDO extends BaseDO {

    /** 编号 */
    private Long id;
    /** 关联的支付订单ID (可选) */
    private Long orderId;
    /** 用户ID */
    private Long userId;
    /** 应用ID */
    private Long appId;
    /** 业务类型: account_open, card_bind, withdraw, etc. */
    private String bizType;
    /** 渠道请求流水号 */
    private String reqSeqId;
    /** 渠道用户ID (例如：汇付子商户号) */
    private String thirdUserId;
    /** 渠道绑卡令牌 (例如：汇付 token_no) */
    private String tokenNo;
    /** 请求状态 */
    private Integer status;
    /** 渠道原始响应 (JSON) */
    private String rawResp;
    /** 业务扩展参数，JSON 格式 */
    private String bizExtra;
    /** 渠道编码 */
    private String channelCode;
    /** 调用渠道的错误码 */
    private String channelErrorCode;
    /** 调用渠道报错时，错误信息 */
    private String channelErrorMsg;
    /** 渠道原始请求 (JSON) */
    private String rawReq;
    /** 渠道特定响应数据 (JSON) - 存储渠道特有的字段 */
    private String channelSpecificData;

}

