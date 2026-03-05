package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.upay.sdk.transferaccount.builder.OrderBuilder;
import com.upay.sdk.transferaccount.builder.QueryBuilder;
import com.upay.sdk.wallet.builder.WithdrawBuilder;
import com.upay.sdk.wallet.builder.WithdrawQueryBuilder;
import lombok.experimental.UtilityClass;

/**
 * 首信易转账/提现构造工具。
 *
 * <p>集中处理金额格式化、公共字段赋值，保证外部调用尽量简洁。</p>
 */
@UtilityClass
class PayeaseTransferMapper {

    /**
     * 构建账户间转账请求，金额统一按“分”发送。
     */
    OrderBuilder buildTransferOrder(PayeasePayClientConfig config,
                                    PayTransferUnifiedInnerReqDTO reqDTO,
                                    PayeaseTransferExtras extras) {
        OrderBuilder builder = new OrderBuilder(config.getMerchantId());
        builder.setRequestId(reqDTO.getOutTransferNo());
        builder.setAmount(formatAmount(reqDTO.getPrice()));
        builder.setCurrency(StrUtil.blankToDefault(reqDTO.getCurrency(), "CNY"));
        builder.setReceiverType(StrUtil.blankToDefault(reqDTO.getReceiverType(), "MERCHANT_ACC"));
        builder.setReceiverId(extras.getSubMerchantId());
        builder.setRemark(StrUtil.blankToDefault(reqDTO.getRemark(), reqDTO.getSubject()));
        if (StrUtil.isNotBlank(reqDTO.getNotifyUrl())) {
            builder.setNotifyUrl(reqDTO.getNotifyUrl());
        }
        if (StrUtil.isNotBlank(config.getPartnerId())) {
            builder.setPartnerId(config.getPartnerId());
        }
        return builder;
    }

    QueryBuilder buildTransferQuery(PayeasePayClientConfig config, String requestId) {
        QueryBuilder builder = new QueryBuilder(config.getMerchantId());
        builder.setRequestId(requestId);
        if (StrUtil.isNotBlank(config.getPartnerId())) {
            builder.setPartnerId(config.getPartnerId());
        }
        return builder;
    }

    /**
     * 转账后的构建账户提现请求（商户账户余额提至默认结算卡）。
     */
    WithdrawBuilder buildWithdrawOrder4Transfer(PayeasePayClientConfig config,
                                                String requestId,
                                                PayTransferUnifiedInnerReqDTO reqDTO) {
        //商户号使用 receiverId
        WithdrawBuilder builder = new WithdrawBuilder(reqDTO.getReceiverId());
        builder.setRequestId(requestId)
                .setWithdrawAmount(formatAmount(reqDTO.getPrice()))
                .setNotifyUrl(reqDTO.getNotifyUrl());
        if (StrUtil.isNotBlank(reqDTO.getRemark())) {
            builder.setRemark(reqDTO.getRemark());
        } else if (StrUtil.isNotBlank(reqDTO.getSubject())) {
            builder.setRemark(reqDTO.getSubject());
        }
        //该场景下需要使用自己作为服务商
        if (StrUtil.isNotBlank(config.getMerchantId())) {
            builder.setPartnerId(config.getMerchantId());
        }
        return builder;
    }

    WithdrawQueryBuilder buildWithdrawQuery4Transfer(PayeasePayClientConfig config, String requestId, String receiverId) {
        WithdrawQueryBuilder builder = new WithdrawQueryBuilder(receiverId);
        builder.setPartnerId(config.getMerchantId());
        builder.setRequestId(requestId);
        return builder;
    }

    /**
     * 首信易接口要求金额为整型字符串（单位：分）。
     */
    private String formatAmount(Integer amountFen) {
        if (amountFen == null) {
            return "0";
        }
        return String.valueOf(amountFen);
    }
}
