package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.codec.Base64;
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
 * 首信易 - 微信扫码支付(Native) Client实现类
 *
 * @author system
 */
@Slf4j
public class PayeaseWxNativePayClient extends AbstractPayeasePayClient {

    /**
     * 微信扫码支付方式编码
     * 官方文档: https://open.payeasenet.com/API/unify_order/encoding/Payment_method_code.html
     */
    private static final String PAYMENT_MODE_CODE = "SCANCODE-WEIXIN_PAY-P2P";

    public PayeaseWxNativePayClient(Long channelId, PayeasePayClientConfig config) {
        super(channelId, PayChannelEnum.PAYEASE_WX_NATIVE.getCode(), config);
    }

    @Override
    protected OrderBuilder buildOrderRequest(PayOrderUnifiedInnerReqDTO reqDTO) {
        // 1. 调用父类构建基础订单信息
        OrderBuilder builder = super.buildOrderRequest(reqDTO);

        // 2. 设置微信扫码支付特有参数
        builder.setPaymentModeCode(PAYMENT_MODE_CODE);

        log.info("[buildOrderRequest][微信扫码支付] outTradeNo={}", reqDTO.getOutTradeNo());

        return builder;
    }

    @Override
    protected PayOrderInnerRespDTO parseOrderResponse(JSONObject jsonObject, PayOrderUnifiedInnerReqDTO reqDTO) {
        log.info("[parseOrderResponse][解析微信扫码支付响应] response={}", jsonObject.toJSONString());

        // 1. 获取响应状态
        String status = jsonObject.getString("status");
        String requestId = jsonObject.getString("requestId");

        // 2. 解析scanCode (Base64编码的二维码图片)
        String scanCodeBase64 = jsonObject.getString("scanCode");

        if (StrUtil.isBlank(scanCodeBase64)) {
            log.error("[parseOrderResponse][微信扫码支付响应缺少scanCode] response={}", jsonObject.toJSONString());
            return PayOrderInnerRespDTO.closedOf("MISSING_SCAN_CODE", "缺少二维码数据",
                    reqDTO.getOutTradeNo(), jsonObject.toJSONString());
        }

        // 3. 二维码数据已经是Base64格式,直接返回给前端展示
        // 前端可以使用 <img src="data:image/png;base64,{scanCodeBase64}" /> 显示二维码
        Map<String, String> displayMap = new HashMap<>();
        displayMap.put("qrCode", scanCodeBase64);  // Base64二维码
        displayMap.put("qrCodeType", "base64");    // 标识为base64格式

        // 4. 返回等待支付状态,前端展示二维码
        return PayOrderInnerRespDTO.waitingOf(
                "qr_code", // 展示模式
                "data:image/png;base64," + scanCodeBase64, // 展示内容(可直接用于img标签的src)
                requestId,
                displayMap // 展示参数
        );
    }
}