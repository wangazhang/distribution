package com.hissp.distribution.framework.pay.core.client.impl.huifu;

import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.exception.PayException;
import com.hissp.distribution.framework.pay.core.client.impl.AbstractPayClient;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import com.huifu.bspay.sdk.opps.client.BasePayClient;
import com.huifu.bspay.sdk.opps.core.BasePay;
import com.huifu.bspay.sdk.opps.core.config.MerConfig;
import com.huifu.bspay.sdk.opps.core.request.BaseRequest;
import com.huifu.bspay.sdk.opps.core.utils.DateTools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.hissp.distribution.framework.common.exception.enums.GlobalErrorCodeConstants.NOT_IMPLEMENTED;

/**
 * 汇付天下支付的抽象类，实现汇付天下的通用逻辑
 */
@Slf4j
public abstract class AbstractHuiPayClient extends AbstractPayClient<HuiPayClientConfig> {

    public AbstractHuiPayClient(Long channelId, String channelCode, HuiPayClientConfig config) {
        super(channelId, channelCode, config);
    }

    /**
     * 初始化汇付天下支付配置
     */
    @Override
    protected void doInit() {
        try {
            MerConfig merConfig = createMerConfig();
            BasePay.initWithMerConfig(merConfig);
            //log.info("[doInit][配置初始化完成：{}]", config);
        } catch (Exception e) {
            log.error("[doInit][配置初始化失败]", e);
            throw new RuntimeException("汇付天下支付初始化失败", e);
        }
    }

    /**
     * 创建商户配置
     */
    protected MerConfig createMerConfig() {
        MerConfig merConfig = new MerConfig();
        //merConfig.setHuifuId(config.getHuifuId());
        merConfig.setProcutId(config.getProductId());
        merConfig.setSysId(config.getSystemId());
        merConfig.setRsaPublicKey(config.getPublicKey());
        merConfig.setRsaPrivateKey(config.getPrivateKey());
        //merConfig.setLogLevel(config.getLogLevel());
        //merConfig.setSignType(config.getSignType());
        // 如果是证书模式，设置证书路径和密码
        //if ("CERT".equalsIgnoreCase(config.getSignType())) {
        //    merConfig.setCertPath(config.getCertPath());
        //    merConfig.setCertPassword(config.getCertPwd());
        //}
        return merConfig;
    }

    /**
     * 执行请求调用
     */
    protected Map<String, Object> executeRequest(BaseRequest request) throws Exception {
        return BasePayClient.request(request, false);
    }

    /**
     * 执行请求调用（页面版本）
     */
    protected Map<String, Object> executePageRequest(BaseRequest request) throws Exception {
        return BasePayClient.request(request, true);
    }

    /**
     * 统一下单
     */
    @Override
    protected PayOrderInnerRespDTO doUnifiedOrder(PayOrderUnifiedInnerReqDTO reqDTO) throws Exception {
        // 由子类实现具体的支付方式
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 解析支付结果通知
     */
    @Override
    protected PayOrderInnerRespDTO doParseOrderNotify(Map<String, String> headers, Map<String, String> params, String body) throws Exception {
        log.info("[doParseOrderNotify][params: {}, body: {}]", params, body);
        
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 查询支付订单
     */
    @Override
    protected PayOrderInnerRespDTO doGetOrder(String outTradeNo) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 退款
     */
    @Override
    protected PayRefundInnerRespDTO doUnifiedRefund(PayRefundUnifiedInnerReqDTO reqDTO) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 解析退款通知
     */
    @Override
    protected PayRefundInnerRespDTO doParseRefundNotify(Map<String, String> headers, Map<String, String> params, String body) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 查询退款结果
     */
    @Override
    protected PayRefundInnerRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 转账
     */
    @Override
    protected PayTransferInnerRespDTO doUnifiedTransfer(PayTransferUnifiedInnerReqDTO reqDTO) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 解析转账通知
     */
    @Override
    protected PayTransferInnerRespDTO doParseTransferNotify(Map<String, String> headers,
                                                            Map<String, String> params,
                                                            String body) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 查询转账结果
     */
    @Override
    protected PayTransferInnerRespDTO doGetTransfer(String outTradeNo, PayTransferTypeEnum type) throws Exception {
        // 默认实现，子类可以覆盖此方法提供具体实现
        throw ServiceExceptionUtil.exception(NOT_IMPLEMENTED);
    }

    /**
     * 构建请求的通用参数
     */
    protected Map<String, Object> buildCommonParameters() {
        Map<String, Object> params = new HashMap<>();
        params.put("reqDate", DateTools.getCurrentDateYYYYMMDD()); // 请求日期 yyyyMMdd
        //params.put("reqSeqId", SequenceTools.getReqSeqId32()); // 请求流水号，确保每次请求唯一
        params.put("huifuId", config.getHuifuId()); // 商户号
        return params;
    }

    /**
     * 检查汇付响应是否成功
     */
    protected boolean isResponseSuccess(Map<String, Object> response) {
        if (response == null) {
            return false;
        }
        // 汇付SDK成功返回码为 "00000000"
        return "00000000".equals(response.get("resp_code"))||"00000100".equals(response.get("resp_code"));
    }

    /**
     * 创建汇付天下请求异常
     */
    protected PayException buildPayException(Throwable e) {
        if (e instanceof PayException) {
            return (PayException) e;
        }
        return new PayException(e);
    }
} 
