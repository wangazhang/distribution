package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.upay.sdk.onlinepay.builder.OrderBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 首信易 - 支付宝支付 Client实现类
 * 支持多种支付宝支付方式: 扫码支付、网站支付、App支付等
 *
 * @author system
 */
@Slf4j
public class PayeaseAlipayPayClient extends AbstractPayeasePayClient {

    /**
     * 支付宝扫码支付方式编码(默认)
     * 官方文档: https://open.payeasenet.com/API/unify_order/encoding/Payment_method_code.html
     * 其他支付宝支付方式:
     * - ALIPAY-WAP-P2P: 支付宝H5支付
     * - MINIAPPS-ALI_PAY-P2P: 支付宝小程序
     * - ALIPAY-OFFICIAL-P2P: 支付宝生活号
     */
    private static final String PAYMENT_MODE_CODE = "SCANCODE-ALI_PAY-P2P";

    public PayeaseAlipayPayClient(Long channelId, PayeasePayClientConfig config) {
        super(channelId, PayChannelEnum.PAYEASE_ALIPAY.getCode(), config);
    }

    @Override
    protected OrderBuilder buildOrderRequest(PayOrderUnifiedInnerReqDTO reqDTO) {
        // 1. 调用父类构建基础订单信息
        OrderBuilder builder = super.buildOrderRequest(reqDTO);

        // 2. 设置支付宝支付特有参数
        // 可以通过channelExtras传递具体的支付方式类型
        String paymentModeCode = getChannelExtra(reqDTO, "paymentModeCode");
        if (StrUtil.isBlank(paymentModeCode)) {
            paymentModeCode = PAYMENT_MODE_CODE; // 使用默认支付方式
        }
        builder.setPaymentModeCode(paymentModeCode);

        log.info("[buildOrderRequest][支付宝支付] outTradeNo={}, paymentModeCode={}",
                reqDTO.getOutTradeNo(), paymentModeCode);

        return builder;
    }

    @Override
    protected PayOrderInnerRespDTO parseOrderResponse(JSONObject jsonObject, PayOrderUnifiedInnerReqDTO reqDTO) {
        log.info("[parseOrderResponse][解析支付宝支付响应] response={}", jsonObject.toJSONString());

        // 1. 获取响应状态
        String status = jsonObject.getString("status");
        String requestId = jsonObject.getString("requestId");

        // 2. 判断响应类型
        // 2.1 如果是二维码支付,返回scanCode
        String scanCode = jsonObject.getString("scanCode");
        if (StrUtil.isNotBlank(scanCode)) {
            return parseQrCodeResponse(scanCode, requestId);
        }

        // 2.2 如果是跳转支付(WAP/PC),返回redirectUrl
        String redirectUrl = jsonObject.getString("redirectUrl");
        if (StrUtil.isNotBlank(redirectUrl)) {
            return parseRedirectResponse(redirectUrl, requestId);
        }

        // 2.3 如果是App支付,返回appParams
        String appParams = jsonObject.getString("appParams");
        if (StrUtil.isNotBlank(appParams)) {
            return parseAppResponse(appParams, requestId);
        }

        // 3. 未识别的响应类型
        log.error("[parseOrderResponse][支付宝支付响应格式未知] response={}", jsonObject.toJSONString());
        return PayOrderInnerRespDTO.closedOf("UNKNOWN_RESPONSE", "未知的支付响应格式",
                reqDTO.getOutTradeNo(), jsonObject.toJSONString());
    }

    /**
     * 解析二维码支付响应
     */
    private PayOrderInnerRespDTO parseQrCodeResponse(String scanCodeBase64, String requestId) {
        Map<String, String> displayMap = new HashMap<>();
        displayMap.put("qrCode", scanCodeBase64);
        displayMap.put("qrCodeType", "base64");

        return PayOrderInnerRespDTO.waitingOf(
                "qr_code",
                "data:image/png;base64," + scanCodeBase64,
                requestId,
                displayMap
        );
    }

    /**
     * 解析跳转支付响应(WAP/PC)
     */
    private PayOrderInnerRespDTO parseRedirectResponse(String redirectUrl, String requestId) {
        Map<String, String> displayMap = new HashMap<>();
        displayMap.put("redirectUrl", redirectUrl);

        return PayOrderInnerRespDTO.waitingOf(
                "redirect",
                redirectUrl,
                requestId,
                displayMap
        );
    }

    /**
     * 解析App支付响应
     */
    private PayOrderInnerRespDTO parseAppResponse(String appParams, String requestId) {
        Map<String, String> displayMap = new HashMap<>();
        displayMap.put("appParams", appParams);

        return PayOrderInnerRespDTO.waitingOf(
                "app",
                appParams,
                requestId,
                displayMap
        );
    }

    /**
     * 从reqDTO的channelExtras中获取参数
     */
    private String getChannelExtra(PayOrderUnifiedInnerReqDTO reqDTO, String key) {
        if (reqDTO.getChannelExtras() == null) {
            return null;
        }
        return reqDTO.getChannelExtras().get(key);
    }
}