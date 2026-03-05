package com.hissp.distribution.framework.pay.core.client;

import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferTypeEnum;

import java.util.Map;

/**
 * 支付客户端，用于对接各支付渠道的 SDK，实现发起支付、退款等功能
 */
public interface PayClient {

    Long getId();

    // ============ 支付相关 ==========

    PayOrderInnerRespDTO unifiedOrder(PayOrderUnifiedInnerReqDTO reqDTO);

    PayOrderInnerRespDTO parseOrderNotify(Map<String, String> headers, Map<String, String> params, String body);

    PayOrderInnerRespDTO getOrder(String outTradeNo);

    // ============ 退款相关 ==========

    PayRefundInnerRespDTO unifiedRefund(PayRefundUnifiedInnerReqDTO reqDTO);

    PayRefundInnerRespDTO parseRefundNotify(Map<String, String> headers, Map<String, String> params, String body);

    PayRefundInnerRespDTO getRefund(String outTradeNo, String outRefundNo);

    // ============ 转账相关 ==========

    PayTransferInnerRespDTO unifiedTransfer(PayTransferUnifiedInnerReqDTO reqDTO);

    PayTransferInnerRespDTO getTransfer(String outTradeNo, PayTransferTypeEnum type);

    PayTransferInnerRespDTO parseTransferNotify(Map<String, String> headers, Map<String, String> params, String body);
}
