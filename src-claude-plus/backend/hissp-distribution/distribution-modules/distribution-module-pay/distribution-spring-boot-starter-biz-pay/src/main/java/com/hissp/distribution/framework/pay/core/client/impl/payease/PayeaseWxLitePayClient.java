package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.upay.sdk.onlinepay.builder.OrderBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 首信易 - 微信小程序支付 Client实现类
 *
 * @author system
 */
@Slf4j
public class PayeaseWxLitePayClient extends AbstractPayeasePayClient {

    /**
     * 微信小程序支付方式编码
     * 官方文档: https://open.payeasenet.com/API/unify_order/encoding/Payment_method_code.html
     */
    private static final String PAYMENT_MODE_CODE = "MINIAPPS-WEIXIN_PAY-P2P";

    public PayeaseWxLitePayClient(Long channelId, PayeasePayClientConfig config) {
        super(channelId, PayChannelEnum.PAYEASE_WX_LITE.getCode(), config);
    }

    @Override
    protected OrderBuilder buildOrderRequest(PayOrderUnifiedInnerReqDTO reqDTO) {
        // 1. 调用父类构建基础订单信息
        OrderBuilder builder = super.buildOrderRequest(reqDTO);

        // 2. 设置微信小程序特有参数
        builder.setPaymentModeCode(PAYMENT_MODE_CODE);

        // 3. 微信小程序需要appId和openId
        String appId = getChannelExtra(reqDTO, "appid");
        String openId = getChannelExtra(reqDTO, "openid");

        if (StrUtil.isNotBlank(appId)) {
            builder.setAppId(appId);
        }
        if (StrUtil.isNotBlank(openId)) {
            builder.setOpenId(openId);
        }

        log.info("[buildOrderRequest][微信小程序支付] outTradeNo={}, appId={}, openId={}",
                reqDTO.getOutTradeNo(), appId, openId);

        return builder;
    }

    @Override
    protected PayOrderInnerRespDTO parseOrderResponse(JSONObject jsonObject, PayOrderUnifiedInnerReqDTO reqDTO) {
        log.info("[parseOrderResponse][解析微信小程序支付响应] response={}", jsonObject.toJSONString());

        // 1. 获取响应状态
        String status = jsonObject.getString("status");
        String merchantId = jsonObject.getString("merchantId");
        String requestId = jsonObject.getString("requestId");

        // 2. 解析jsString (微信小程序支付签名数据)
        String jsString = jsonObject.getString("jsString");

        if (StrUtil.isBlank(jsString)) {
            log.error("[parseOrderResponse][微信小程序支付响应缺少jsString] response={}", jsonObject.toJSONString());
            return PayOrderInnerRespDTO.closedOf("MISSING_JS_STRING", "缺少微信小程序支付签名数据",
                    reqDTO.getOutTradeNo(), jsonObject.toJSONString());
        }

        // 3. jsString 是微信支付所需的签名参数,需要原样返回给前端
        // 格式示例: {"appId":"wxxx","timeStamp":"1234567890","nonceStr":"xxx","package":"prepay_id=xxx","signType":"MD5","paySign":"xxx"}
        Map<String, String> displayMap = new HashMap<>();
        try {
            JSONObject jsObj = JSONObject.parseObject(jsString);
            displayMap.put("appId", jsObj.getString("appId"));
            displayMap.put("timeStamp", jsObj.getString("timeStamp"));
            displayMap.put("nonceStr", jsObj.getString("nonceStr"));
            displayMap.put("packageValue", jsObj.getString("package"));
            displayMap.put("signType", jsObj.getString("signType"));
            displayMap.put("paySign", jsObj.getString("paySign"));
        } catch (Exception e) {
            log.error("[parseOrderResponse][解析jsString失败] jsString={}", jsString, e);
            // 解析失败也返回原始数据
            displayMap.put("jsString", jsString);
        }

        // 4. 返回等待支付状态,前端使用displayContent调起微信支付
        return PayOrderInnerRespDTO.waitingOf(
                "jsapi", // 展示模式
                jsString, // 展示内容(原始jsString)
                requestId,
                displayMap // 展示参数(解析后的Map)
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