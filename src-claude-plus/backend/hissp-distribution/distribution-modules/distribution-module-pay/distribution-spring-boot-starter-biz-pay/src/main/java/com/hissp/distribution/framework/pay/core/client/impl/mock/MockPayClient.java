package com.hissp.distribution.framework.pay.core.client.impl.mock;

import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.impl.AbstractPayClient;
import com.hissp.distribution.framework.pay.core.client.impl.NonePayClientConfig;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferTypeEnum;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 模拟支付的 PayClient 实现类
 *
 * 模拟支付返回结果都是成功，方便大家日常流畅
 *
 * @author jason
 */
public class MockPayClient extends AbstractPayClient<NonePayClientConfig> {

    private static final String MOCK_RESP_SUCCESS_DATA = "MOCK_SUCCESS";

    public MockPayClient(Long channelId, NonePayClientConfig config) {
        super(channelId, PayChannelEnum.MOCK.getCode(), config);
    }

    @Override
    protected void doInit() {
    }

    @Override
    protected PayOrderInnerRespDTO doUnifiedOrder(PayOrderUnifiedInnerReqDTO reqDTO) {
        return PayOrderInnerRespDTO.successOf("MOCK-P-" + reqDTO.getOutTradeNo(), "", LocalDateTime.now(),
                reqDTO.getOutTradeNo(), MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayOrderInnerRespDTO doGetOrder(String outTradeNo) {
        return PayOrderInnerRespDTO.successOf("MOCK-P-" + outTradeNo, "", LocalDateTime.now(),
                outTradeNo, MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundInnerRespDTO doUnifiedRefund(PayRefundUnifiedInnerReqDTO reqDTO) {
        return PayRefundInnerRespDTO.successOf("MOCK-R-" + reqDTO.getOutRefundNo(), LocalDateTime.now(),
                reqDTO.getOutRefundNo(), MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayRefundInnerRespDTO doGetRefund(String outTradeNo, String outRefundNo) {
        return PayRefundInnerRespDTO.successOf("MOCK-R-" + outRefundNo, LocalDateTime.now(),
                outRefundNo, MOCK_RESP_SUCCESS_DATA);
    }

    @Override
    protected PayTransferInnerRespDTO doParseTransferNotify(Map<String, String> headers,
                                                           Map<String, String> params,
                                                           String body) throws Throwable {
        throw new UnsupportedOperationException("未实现");
    }

    @Override
    protected PayRefundInnerRespDTO doParseRefundNotify(Map<String, String> headers, Map<String, String> params, String body) {
        throw new UnsupportedOperationException("模拟支付无退款回调");
    }

    @Override
    protected PayOrderInnerRespDTO doParseOrderNotify(Map<String, String> headers, Map<String, String> params, String body) {
        throw new UnsupportedOperationException("模拟支付无支付回调");
    }

    @Override
    protected PayTransferInnerRespDTO doUnifiedTransfer(PayTransferUnifiedInnerReqDTO reqDTO) {
        throw new UnsupportedOperationException("待实现");
    }

    @Override
    protected PayTransferInnerRespDTO doGetTransfer(String outTradeNo, PayTransferTypeEnum type) {
        throw new UnsupportedOperationException("待实现");
    }

}
