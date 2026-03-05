package com.hissp.distribution.framework.pay.core.client.impl.huifu;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderDisplayModeEnum;
import com.hissp.distribution.framework.pay.core.enums.order.PayOrderStatusRespEnum;
import com.hissp.distribution.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import com.huifu.bspay.sdk.opps.core.request.V3TradePaymentJspayRequest;
import com.huifu.bspay.sdk.opps.core.request.V3TradePaymentScanpayQueryRequest;
import com.huifu.bspay.sdk.opps.core.request.V3TradePaymentScanpayRefundRequest;
import com.huifu.bspay.sdk.opps.core.request.V3TradePaymentScanpayRefundqueryRequest;
import com.huifu.bspay.sdk.opps.core.utils.DateTools;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.hissp.distribution.framework.common.exception.enums.GlobalErrorCodeConstants.BAD_REQUEST;
import static com.hissp.distribution.framework.common.util.json.JsonUtils.toJsonString;

/**
 * 汇付天下微信小程序支付的 PayClient 实现类
 */
@Slf4j
public class HuiWxLitePayClient extends AbstractHuiPayClient {

    public HuiWxLitePayClient(Long channelId, HuiPayClientConfig config) {
        super(channelId, PayChannelEnum.HUIFU_WX_LITE.getCode(), config);
    }

    @Override
    protected PayOrderInnerRespDTO doUnifiedOrder(PayOrderUnifiedInnerReqDTO reqDTO) throws Exception {
        // 1. 构建请求参数
        V3TradePaymentJspayRequest request = buildJspayRequest(reqDTO);
        
        // 2. 执行请求
        Map<String, Object> response = executeRequest(request);
        log.info("[doUnifiedOrder][response: {}]", response);
        
        // 3. 处理结果
        if (!isResponseSuccess(response)) {
            // 支付失败
            return PayOrderInnerRespDTO.closedOf(
                    MapUtil.getStr(response, "resp_code"),
                    MapUtil.getStr(response, "resp_desc"),
                    reqDTO.getOutTradeNo(), response);
        }
        
        // 4. 支付成功，构建支付结果
        // 汇付天下微信小程序支付，返回小程序支付所需的参数
        // 支付参数在pay_info字段中，是一个JSON字符串
        String payInfoStr = MapUtil.getStr(response, "pay_info");
        JSONObject payInfo = JSONObject.parseObject(payInfoStr);
        
        // 从支付信息中获取支付所需参数
        JSONObject wxPayParams = new JSONObject();
        wxPayParams.put("appId", payInfo.getString("appId"));
        wxPayParams.put("timeStamp", payInfo.getString("timeStamp"));
        wxPayParams.put("nonceStr", payInfo.getString("nonceStr"));
        wxPayParams.put("package", payInfo.getString("package"));
        wxPayParams.put("signType", payInfo.getString("signType"));
        wxPayParams.put("paySign", payInfo.getString("paySign"));
        
        // 返回支付结果
        return PayOrderInnerRespDTO.waitingOf(
                PayOrderDisplayModeEnum.APP.getMode(),
                toJsonString(wxPayParams),
                reqDTO.getOutTradeNo(),
                response);
    }

    /**
     * 构建聚合支付请求
     */
    private V3TradePaymentJspayRequest buildJspayRequest(PayOrderUnifiedInnerReqDTO reqDTO) {
        // 1. 校验参数
        validateOpenid(reqDTO);
        
        // 2. 构建请求
        V3TradePaymentJspayRequest request = new V3TradePaymentJspayRequest();
        // 基础参数
        Map<String, Object> params = buildCommonParameters();
        request.setReqDate((String) params.get("reqDate"));
        request.setReqSeqId(reqDTO.getOutTradeNo());
        request.setHuifuId((String) params.get("huifuId"));
        
        // 支付参数
        request.setTradeType("T_MINIAPP"); // 微信小程序支付
        request.setTransAmt(formatAmount(reqDTO.getPrice())); // 金额，单位为元，保留两位小数
        request.setGoodsDesc(reqDTO.getSubject()); // 商品描述
        
        // 扩展参数
        Map<String, Object> extendInfoMap = new HashMap<>();
        // 微信小程序支付需要传递 openid
        extendInfoMap.put("wx_data", buildWxData(reqDTO));
        // 设置异步通知地址
        extendInfoMap.put("notify_url", reqDTO.getNotifyUrl());
        // 设置订单失效时间
        if (reqDTO.getExpireTime() != null) {
            extendInfoMap.put("time_expire", formatExpireTime(reqDTO.getExpireTime()));
        }
        
        // 设置扩展参数
        request.setExtendInfo(extendInfoMap);
        
        return request;
    }
    
    /**
     * 构建微信支付所需参数
     */
    private String buildWxData(PayOrderUnifiedInnerReqDTO reqDTO) {
        JSONObject wxData = new JSONObject();
        // 设置小程序的openid
        wxData.put("openid", getOpenid(reqDTO));
        wxData.put("sub_appid", "wx3c530cf455fd8ac8");
        // 其他微信支付参数...
        return wxData.toJSONString();
    }
    
    /**
     * 获取微信小程序openid
     */
    private String getOpenid(PayOrderUnifiedInnerReqDTO reqDTO) {
        String openid = MapUtil.getStr(reqDTO.getChannelExtras(), "openid");
        if (StrUtil.isEmpty(openid)) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "支付请求的 openid 不能为空");
        }
        return openid;
    }
    
    /**
     * 校验openid参数
     */
    private void validateOpenid(PayOrderUnifiedInnerReqDTO reqDTO) {
        if (reqDTO.getChannelExtras() == null 
                || !reqDTO.getChannelExtras().containsKey("openid")
                || StrUtil.isEmpty(reqDTO.getChannelExtras().get("openid"))) {
            throw ServiceExceptionUtil.exception(BAD_REQUEST, "微信小程序支付必须传递 openid 参数");
        }
    }
    
    /**
     * 格式化金额，转换为元，保留两位小数
     */
    private String formatAmount(Integer amountCent) {
        return String.format("%.2f", amountCent / 100.0);
    }
    
    /**
     * 格式化过期时间，转换为yyyyMMddHHmmss格式
     */
    private String formatExpireTime(java.time.LocalDateTime expireTime) {
        return expireTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }
    
    @Override
    protected PayOrderInnerRespDTO doParseOrderNotify(Map<String, String> params, Map<String, String> stringStringMap, String body) throws Exception {
        //log.info("[doParseOrderNotify][汇付微信小程序支付回调数据: params={}, body={}]", params, body);

        try {
            // 汇付天下的回调数据是URL编码格式，不是JSON格式
            // 需要先解析URL编码的参数，然后从resp_data字段中获取实际的交易数据

            // 1. 解析URL编码的回调参数
            Map<String, String> callbackParams = parseUrlEncodedParams(body);
            log.info("[doParseOrderNotify][解析后的回调参数: {}]", callbackParams);

            // 2. 验证基本回调参数
            String respCode = callbackParams.get("resp_code");
            String respDesc = callbackParams.get("resp_desc");
            String sign = callbackParams.get("sign");
            String respDataStr = callbackParams.get("resp_data");

            if (StrUtil.isBlank(respDataStr)) {
                log.error("[doParseOrderNotify][resp_data字段为空]");
                throw ServiceExceptionUtil.exception(BAD_REQUEST, "回调数据中resp_data字段为空");
            }

            // 3. 解析resp_data中的实际交易数据
            JSONObject respData = JSONObject.parseObject(respDataStr);
            log.info("[doParseOrderNotify][交易数据: {}]", JSONObject.toJSONString(respData, true));

            // 4. 提取关键交易信息
            String notifyType = respData.getString("notify_type"); // 通知类型：1-交易异步，2-账务异步
            String transStatus = respData.getString("trans_stat"); // 交易状态
            String acctStatus = respData.getString("acct_stat"); // 账务状态（仅账务异步时有值）
            String merOrdId = respData.getString("mer_ord_id"); // 商户订单号
            String hfSeqId = respData.getString("hf_seq_id"); // 汇付流水号
            String transAmt = respData.getString("trans_amt"); // 交易金额
            String endTime = respData.getString("end_time"); // 交易完成时间
            String huifuId = respData.getString("huifu_id"); // 汇付商户号

            log.info("[doParseOrderNotify][通知类型: {}, 交易状态: {}, 账务状态: {}]", notifyType, transStatus, acctStatus);

            // 5. 获取微信用户信息
            String wxUserId = null;
            JSONObject wxResponse = respData.getJSONObject("wx_response");
            if (wxResponse != null) {
                wxUserId = wxResponse.getString("openid");
                if (StrUtil.isBlank(wxUserId)) {
                    wxUserId = wxResponse.getString("sub_openid");
                }
            }

            // 6. 验证商户号
            if (!config.getHuifuId().equals(huifuId)) {
                log.error("[doParseOrderNotify][商户号不匹配: 配置={}, 回调={}]", config.getHuifuId(), huifuId);
                throw ServiceExceptionUtil.exception(BAD_REQUEST, "商户号不匹配");
            }

            // 7. 根据通知类型处理不同的业务逻辑
            Integer status = parseHuiPayNotifyStatus(notifyType, transStatus, acctStatus);
            log.info("[doParseOrderNotify][订单({}) 通知类型: {}, 交易状态: {}, 账务状态: {}, 最终状态: {}]",
                    merOrdId, notifyType, transStatus, acctStatus, status);

            // 8. 根据通知类型决定处理方式
            if (!shouldProcessNotify(notifyType, transStatus, acctStatus)) {
                log.info("[doParseOrderNotify][订单({}) 交易成功，等待账务异步通知]", merOrdId);
                // 返回处理中状态，表示等待后续账务通知
                return PayOrderInnerRespDTO.of(
                        PayOrderStatusRespEnum.PROCESS.getStatus(), // 处理中状态
                        hfSeqId, // 汇付流水号作为渠道订单号
                        wxUserId, // 微信用户ID
                        null, // 支付完成时间暂为空，等待账务确认
                        merOrdId, // 商户订单号
                        body); // 原始数据
            }

            // 9. 构建最终结果
            return PayOrderInnerRespDTO.of(
                    status,
                    hfSeqId, // 汇付流水号作为渠道订单号
                    wxUserId, // 微信用户ID
                    parseHuiPayTime(endTime), // 支付完成时间
                    merOrdId, // 商户订单号
                    body); // 原始数据

        } catch (Exception e) {
            log.error("[doParseOrderNotify][回调处理异常: params={}, body={}]", params, body, e);
            throw buildPayException(e);
        }
    }
    
    /**
     * 解析URL编码的参数
     */
    private Map<String, String> parseUrlEncodedParams(String body) {
        Map<String, String> params = new HashMap<>();

        if (StrUtil.isBlank(body)) {
            return params;
        }

        try {
            // 按&分割参数
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                    params.put(key, value);
                }
            }
        } catch (Exception e) {
            log.error("[parseUrlEncodedParams][URL解码失败: {}]", body, e);
        }

        return params;
    }

    /**
     * 根据通知类型和状态解析最终的支付状态
     */
    private Integer parseHuiPayNotifyStatus(String notifyType, String transStatus, String acctStatus) {
        if (StrUtil.isBlank(notifyType) || StrUtil.isBlank(transStatus)) {
            log.warn("[parseHuiPayNotifyStatus][通知类型或交易状态为空: notifyType={}, transStatus={}]", notifyType, transStatus);
            return PayOrderStatusRespEnum.CLOSED.getStatus();
        }

        // 1. 交易异步通知 (notify_type='1')
        if ("1".equals(notifyType)) {
            return parseTransactionNotifyStatus(transStatus);
        }

        // 2. 账务异步通知 (notify_type='2')
        if ("2".equals(notifyType)) {
            return parseAccountNotifyStatus(transStatus, acctStatus);
        }

        log.warn("[parseHuiPayNotifyStatus][未知的通知类型: {}]", notifyType);
        return PayOrderStatusRespEnum.CLOSED.getStatus();
    }

    /**
     * 解析交易异步通知状态
     */
    private Integer parseTransactionNotifyStatus(String transStatus) {
        // 交易异步：只关注交易状态
        switch (transStatus) {
            case "S":
                return PayOrderStatusRespEnum.PROCESS.getStatus(); // 交易成功，但需等待账务异步确认
            case "F":
                return PayOrderStatusRespEnum.CLOSED.getStatus(); // 交易失败
            case "P":
                return PayOrderStatusRespEnum.PROCESS.getStatus(); // 处理中
            case "I":
                return PayOrderStatusRespEnum.WAITING.getStatus(); // 初始状态
            default:
                log.warn("[parseTransactionNotifyStatus][未知的交易状态: {}]", transStatus);
                return PayOrderStatusRespEnum.CLOSED.getStatus();
        }
    }

    /**
     * 解析账务异步通知状态
     */
    private Integer parseAccountNotifyStatus(String transStatus, String acctStatus) {
        // 账务异步：需要同时判断交易状态和账务状态
        if ("S".equals(transStatus)) {
            if ("S".equals(acctStatus)) {
                return PayOrderStatusRespEnum.SUCCESS.getStatus(); // 交易成功-入账成功
            } else if ("F".equals(acctStatus)) {
                return PayOrderStatusRespEnum.CLOSED.getStatus(); // 交易成功-入账失败
            }
        }

        log.warn("[parseAccountNotifyStatus][异常的账务通知状态: transStatus={}, acctStatus={}]", transStatus, acctStatus);
        return PayOrderStatusRespEnum.CLOSED.getStatus();
    }

    /**
     * 判断是否应该处理当前通知
     */
    private boolean shouldProcessNotify(String notifyType, String transStatus, String acctStatus) {
        // 1. 交易异步通知处理规则
        if ("1".equals(notifyType)) {
            if ("F".equals(transStatus)) {
                // 交易失败，直接处理
                return true;
            } else if ("S".equals(transStatus)) {
                // 交易成功，但需等待账务异步通知，暂不处理
                log.info("[shouldProcessNotify][交易成功，等待账务异步通知]");
                return false;
            }
            // 其他状态（P、I等）也可以处理
            return true;
        }

        // 2. 账务异步通知处理规则
        if ("2".equals(notifyType)) {
            // 账务异步通知都需要处理，因为这是最终状态
            return true;
        }

        return false;
    }

    /**
     * 解析汇付时间格式
     */
    private java.time.LocalDateTime parseHuiPayTime(String timeStr) {
        if (StrUtil.isEmpty(timeStr)) {
            return null;
        }

        try {
            // 汇付时间格式为 yyyyMMddHHmmss
            return java.time.LocalDateTime.parse(timeStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        } catch (Exception e) {
            log.error("[parseHuiPayTime][时间解析失败: {}]", timeStr, e);
            return null;
        }
    }
    
    @Override
    protected PayOrderInnerRespDTO doGetOrder(String outTradeNo) throws Exception {
        // 实现查询订单接口，调用汇付天下的订单查询API
        log.info("[doGetOrder][查询订单，商户订单号: {}]", outTradeNo);
        
        try {
            // 1. 构建请求参数
            V3TradePaymentScanpayQueryRequest request = new V3TradePaymentScanpayQueryRequest();
            
            // 设置基础参数
            Map<String, Object> params = buildCommonParameters();
            request.setHuifuId((String) params.get("huifuId"));
            
            // 设置查询参数，根据商户订单号查询
            request.setOrgReqSeqId(outTradeNo);
            request.setOrgReqDate(DateTools.getCurrentDateYYYYMMDD());
            
            // 2. 执行请求
            Map<String, Object> response = executeRequest(request);
            log.info("[doGetOrder][response: {}]", response);
            
            // 3. 处理结果
            if (!isResponseSuccess(response)) {
                // 查询失败
                return PayOrderInnerRespDTO.closedOf(
                        MapUtil.getStr(response, "resp_code"),
                        MapUtil.getStr(response, "resp_desc"),
                        outTradeNo, response);
            }
            
            // 4. 解析返回结果
            String tradeStatus = MapUtil.getStr(response, "trans_stat");
            
            // 5. 根据交易状态返回对应的结果
            // 汇付文档中交易状态：P:处理中; S:成功; F:失败; I:初始(初始状态很罕见，请联系汇付技术人员处理)
            Integer status;
            if ("S".equals(tradeStatus)) {
                status = PayOrderStatusRespEnum.SUCCESS.getStatus(); // 支付成功
            } else if ("F".equals(tradeStatus)) {
                status = PayOrderStatusRespEnum.CLOSED.getStatus(); // 支付失败，对应关闭状态
            } else if ("P".equals(tradeStatus)) {
                status = PayOrderStatusRespEnum.PROCESS.getStatus(); // 处理中
            } else if ("I".equals(tradeStatus)) {
                status = PayOrderStatusRespEnum.WAITING.getStatus(); // 初始状态，对应未支付
            } else if ("SUCCESS".equals(tradeStatus) || "FINISH".equals(tradeStatus)) {
                // 兼容原有可能的返回值
                status = PayOrderStatusRespEnum.SUCCESS.getStatus();
            } else if ("CLOSED".equals(tradeStatus)) {
                // 兼容原有可能的返回值
                status = PayOrderStatusRespEnum.CLOSED.getStatus();
            } else if ("WAITING_PAYMENT".equals(tradeStatus)) {
                // 兼容原有可能的返回值
                status = PayOrderStatusRespEnum.WAITING.getStatus();
            } else {
                throw new IllegalArgumentException(StrUtil.format("未知的支付状态({})", tradeStatus));
            }
            
            // 6. 构建返回结果
            return PayOrderInnerRespDTO.of(
                    status,
                    MapUtil.getStr(response, "org_hf_seq_id"), // 渠道单号，汇付可能没有返回
                    MapUtil.getStr(response, "wx_user_id"), // 渠道用户ID，汇付可能没有返回
                    parseOrderSuccessTime(MapUtil.getStr(response, "pay_time")), // 支付成功时间
                    outTradeNo,
                    response);
        } catch (Exception e) {
            log.error("[doGetOrder][查询订单异常，商户订单号: {}]", outTradeNo, e);
            throw buildPayException(e);
        }
    }
    
    /**
     * 解析订单支付成功时间
     */
    private java.time.LocalDateTime parseOrderSuccessTime(String payTime) {
        if (StrUtil.isEmpty(payTime)) {
            return null;
        }
        // 假设格式为 yyyyMMddHHmmss
        return java.time.LocalDateTime.parse(payTime,
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    // ========== 退款相关方法 ==========

    @Override
    protected PayRefundInnerRespDTO doUnifiedRefund(PayRefundUnifiedInnerReqDTO reqDTO) throws Exception {
        log.info("[doUnifiedRefund][发起退款请求: {}]", reqDTO);

        try {
            // 1. 构建退款请求
            V3TradePaymentScanpayRefundRequest request = buildRefundRequest(reqDTO);

            // 2. 执行请求
            Map<String, Object> response = executeRequest(request);
            log.info("[doUnifiedRefund][退款响应: {}]", response);

            // 3. 处理结果
            if (!isResponseSuccess(response)) {
                // 退款失败
                return PayRefundInnerRespDTO.failureOf(
                        MapUtil.getStr(response, "resp_code"),
                        MapUtil.getStr(response, "resp_desc"),
                        reqDTO.getOutRefundNo(), response);
            }

            // 4. 退款请求成功，返回等待状态
            // 汇付天下退款是异步的，需要通过回调或查询获取最终结果
            return PayRefundInnerRespDTO.waitingOf(
                    MapUtil.getStr(response, "hf_seq_id"), // 汇付流水号
                    reqDTO.getOutRefundNo(),
                    response);

        } catch (Exception e) {
            log.error("[doUnifiedRefund][退款请求异常: {}]", reqDTO, e);
            throw buildPayException(e);
        }
    }

    /**
     * 构建退款请求
     */
    private V3TradePaymentScanpayRefundRequest buildRefundRequest(PayRefundUnifiedInnerReqDTO reqDTO) {
        V3TradePaymentScanpayRefundRequest request = new V3TradePaymentScanpayRefundRequest();

        // 基础参数
        Map<String, Object> params = buildCommonParameters();
        request.setReqDate((String) params.get("reqDate"));
        request.setReqSeqId(reqDTO.getOutRefundNo()); // 使用退款单号作为请求流水号
        request.setHuifuId((String) params.get("huifuId"));

        // 退款金额，转换为元，保留两位小数
        request.setOrdAmt(formatAmount(reqDTO.getRefundPrice()));

        // 原交易请求日期，从原订单号中提取或使用当前日期
        request.setOrgReqDate(extractDateFromOrderNo(reqDTO.getOutTradeNo()));

        // 扩展参数
        Map<String, Object> extendInfoMap = new HashMap<>();

        // 设置异步通知地址
        if (StrUtil.isNotBlank(reqDTO.getNotifyUrl())) {
            extendInfoMap.put("notify_url", reqDTO.getNotifyUrl());
        }

        // 退款原因
        if (StrUtil.isNotBlank(reqDTO.getReason())) {
            extendInfoMap.put("remark", reqDTO.getReason());
        }

        // 可以通过原交易流水号或原交易请求流水号来指定要退款的订单
        // 这里使用原交易请求流水号（即原订单号）
        extendInfoMap.put("org_hf_seq_id", reqDTO.getChannelOrderNo());
        //extendInfoMap.put("org_req_seq_id", reqDTO.getOutTradeNo());

        request.setExtendInfo(extendInfoMap);

        return request;
    }

    /**
     * 从订单号中提取日期，如果无法提取则使用当前日期
     */
    private String extractDateFromOrderNo(String outTradeNo) {
        try {
            // 尝试从订单号中提取日期，假设订单号包含日期信息
            // 这里需要根据实际的订单号格式来调整
            if (StrUtil.isNotBlank(outTradeNo) && outTradeNo.length() >= 8) {
                // 假设订单号前8位是日期格式 yyyyMMdd
                String dateStr = outTradeNo.substring(1, 9);
                if (dateStr.matches("\\d{8}")) {
                    return dateStr;
                }
            }
        } catch (Exception e) {
            log.warn("[extractDateFromOrderNo][从订单号提取日期失败: {}]", outTradeNo, e);
        }

        // 如果无法提取，使用当前日期
        return DateTools.getCurrentDateYYYYMMDD();
    }

    @Override
    protected PayRefundInnerRespDTO doParseRefundNotify(Map<String, String> headers, Map<String, String> params, String body) throws Exception {
        log.info("[doParseRefundNotify][汇付退款回调数据: headers={}, params={}, body={}]", headers, params, body);

        try {
            // 1. 解析URL编码的回调参数
            Map<String, String> callbackParams = parseUrlEncodedParams(body);
            log.info("[doParseRefundNotify][解析后的回调参数: {}]", callbackParams);

            // 2. 验证基本回调参数
            String respCode = callbackParams.get("resp_code");
            String respDesc = callbackParams.get("resp_desc");
            String respDataStr = callbackParams.get("resp_data");

            if (StrUtil.isBlank(respDataStr)) {
                log.error("[doParseRefundNotify][resp_data字段为空]");
                throw ServiceExceptionUtil.exception(BAD_REQUEST, "退款回调数据中resp_data字段为空");
            }

            // 3. 解析resp_data中的实际退款数据
            JSONObject respData = JSONObject.parseObject(respDataStr);
            log.info("[doParseRefundNotify][退款数据: {}]", respData);

            // 4. 提取关键退款信息
            String refundStatus = respData.getString("trans_stat"); // 退款状态
            String merOrdId = respData.getString("mer_ord_id"); // 商户订单号
            String hfSeqId = respData.getString("hf_seq_id"); // 汇付流水号
            String refundAmt = respData.getString("ord_amt"); // 退款金额
            String endTime = respData.getString("end_time"); // 退款完成时间
            String huifuId = respData.getString("huifu_id"); // 汇付商户号
            String orgReqSeqId = respData.getString("org_req_seq_id"); // 原交易请求流水号

            // 5. 验证商户号
            if (!config.getHuifuId().equals(huifuId)) {
                log.error("[doParseRefundNotify][商户号不匹配: 配置={}, 回调={}]", config.getHuifuId(), huifuId);
                throw ServiceExceptionUtil.exception(BAD_REQUEST, "退款回调商户号不匹配");
            }

            // 6. 解析退款状态
            Integer status = parseHuiPayRefundStatus(refundStatus);
            log.info("[doParseRefundNotify][退款({}) 状态解析: {} -> {}]", merOrdId, refundStatus, status);

            // 7. 构建退款结果
            if (PayRefundStatusRespEnum.isSuccess(status)) {
                return PayRefundInnerRespDTO.successOf(
                        hfSeqId, // 汇付流水号作为渠道退款号
                        parseHuiPayTime(endTime), // 退款完成时间
                        merOrdId, // 退款单号
                        body); // 原始数据
            } else if (PayRefundStatusRespEnum.isFailure(status)) {
                return PayRefundInnerRespDTO.failureOf(
                        respCode, // 错误码
                        respDesc, // 错误描述
                        merOrdId, // 退款单号
                        body); // 原始数据
            } else {
                // 等待状态
                return PayRefundInnerRespDTO.waitingOf(
                        hfSeqId, // 汇付流水号作为渠道退款号
                        merOrdId, // 退款单号
                        body); // 原始数据
            }

        } catch (Exception e) {
            log.error("[doParseRefundNotify][退款回调处理异常: params={}, body={}]", params, body, e);
            throw buildPayException(e);
        }
    }

    /**
     * 解析汇付退款状态
     */
    private Integer parseHuiPayRefundStatus(String refundStatus) {
        if (StrUtil.isBlank(refundStatus)) {
            log.warn("[parseHuiPayRefundStatus][退款状态为空]");
            return PayRefundStatusRespEnum.FAILURE.getStatus();
        }

        // 汇付文档中退款状态：S:成功; F:失败; P:处理中; I:初始
        switch (refundStatus) {
            case "S":
                return PayRefundStatusRespEnum.SUCCESS.getStatus(); // 退款成功
            case "F":
                return PayRefundStatusRespEnum.FAILURE.getStatus(); // 退款失败
            case "P":
                return PayRefundStatusRespEnum.WAITING.getStatus(); // 处理中，视为等待状态
            case "I":
                return PayRefundStatusRespEnum.WAITING.getStatus(); // 初始状态，视为等待状态
            default:
                log.warn("[parseHuiPayRefundStatus][未知的退款状态: {}]", refundStatus);
                return PayRefundStatusRespEnum.FAILURE.getStatus(); // 默认失败状态
        }
    }

    @Override
    protected PayRefundInnerRespDTO doGetRefund(String outTradeNo, String outRefundNo) throws Exception {
        log.info("[doGetRefund][查询退款，原订单号: {}, 退款单号: {}]", outTradeNo, outRefundNo);

        try {
            // 1. 构建查询请求
            V3TradePaymentScanpayRefundqueryRequest request = new V3TradePaymentScanpayRefundqueryRequest();

            // 设置基础参数
            Map<String, Object> params = buildCommonParameters();
            request.setHuifuId((String) params.get("huifuId"));

            // 设置查询参数
            // 退款请求日期，从退款单号中提取或使用当前日期
            request.setOrgReqDate(extractDateFromOrderNo(outRefundNo));

            // 使用退款请求流水号查询（即退款单号）
            request.setOrgReqSeqId(outRefundNo);

            // 其他查询参数设为空，让汇付根据退款请求流水号查询
            request.setOrgHfSeqId(""); // 退款全局流水号
            request.setMerOrdId(""); // 终端订单号

            // 2. 执行请求
            Map<String, Object> response = executeRequest(request);
            log.info("[doGetRefund][退款查询响应: {}]", response);

            // 3. 处理结果
            if (!isResponseSuccess(response)) {
                // 查询失败，可能是退款不存在或其他错误
                log.warn("[doGetRefund][退款查询失败: {}]", response);
                return PayRefundInnerRespDTO.failureOf(
                        MapUtil.getStr(response, "resp_code"),
                        MapUtil.getStr(response, "resp_desc"),
                        outRefundNo, response);
            }

            // 4. 解析返回结果
            String refundStatus = MapUtil.getStr(response, "trans_stat");
            String channelRefundNo = MapUtil.getStr(response, "hf_seq_id");
            String refundTime = MapUtil.getStr(response, "end_time");

            // 5. 根据退款状态返回对应的结果
            Integer status = parseHuiPayRefundStatus(refundStatus);

            // 6. 构建返回结果
            if (PayRefundStatusRespEnum.isSuccess(status)) {
                return PayRefundInnerRespDTO.successOf(
                        channelRefundNo,
                        parseHuiPayTime(refundTime),
                        outRefundNo,
                        response);
            } else if (PayRefundStatusRespEnum.isFailure(status)) {
                return PayRefundInnerRespDTO.failureOf(
                        MapUtil.getStr(response, "resp_code"),
                        MapUtil.getStr(response, "resp_desc"),
                        outRefundNo, response);
            } else {
                // 处理中状态
                return PayRefundInnerRespDTO.waitingOf(
                        channelRefundNo,
                        outRefundNo,
                        response);
            }

        } catch (Exception e) {
            log.error("[doGetRefund][查询退款异常，原订单号: {}, 退款单号: {}]", outTradeNo, outRefundNo, e);
            throw buildPayException(e);
        }
    }
}