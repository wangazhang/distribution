package com.hissp.distribution.framework.pay.core.enums.channel;

import com.hissp.distribution.framework.pay.core.client.PayClientConfig;
import com.hissp.distribution.framework.pay.core.client.impl.NonePayClientConfig;
import com.hissp.distribution.framework.pay.core.client.impl.alipay.AlipayPayClientConfig;
import com.hissp.distribution.framework.pay.core.client.impl.huifu.HuiPayClientConfig;
import com.hissp.distribution.framework.pay.core.client.impl.payease.PayeasePayClientConfig;
import com.hissp.distribution.framework.pay.core.client.impl.weixin.WxPayClientConfig;

import cn.hutool.core.util.ArrayUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付渠道的编码的枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    WX_PUB("wx_pub", "微信 JSAPI 支付", WxPayClientConfig.class), // 公众号网页
    WX_LITE("wx_lite", "微信小程序支付", WxPayClientConfig.class),
    WX_APP("wx_app", "微信 App 支付", WxPayClientConfig.class),
    WX_NATIVE("wx_native", "微信 Native 支付", WxPayClientConfig.class),
    WX_WAP("wx_wap", "微信 Wap 网站支付", WxPayClientConfig.class), // H5 网页
    WX_BAR("wx_bar", "微信付款码支付", WxPayClientConfig.class),

    ALIPAY_PC("alipay_pc", "支付宝 PC 网站支付", AlipayPayClientConfig.class),
    ALIPAY_WAP("alipay_wap", "支付宝 Wap 网站支付", AlipayPayClientConfig.class),
    ALIPAY_APP("alipay_app", "支付宝App 支付", AlipayPayClientConfig.class),
    ALIPAY_QR("alipay_qr", "支付宝扫码支付", AlipayPayClientConfig.class),
    ALIPAY_BAR("alipay_bar", "支付宝条码支付", AlipayPayClientConfig.class),

    HUIFU_WX_LITE("huifu_wx_lite", "汇付天下微信小程序支付", HuiPayClientConfig.class),
    HUIFU_WX_NATIVE("huifu_wx_native", "汇付天下微信扫码支付", HuiPayClientConfig.class),
    HUIFU_ALIPAY("huifu_alipay", "汇付天下支付宝支付", HuiPayClientConfig.class),

    MOCK("mock", "模拟支付", NonePayClientConfig.class),

    PAYEASE_WX_LITE("payease_wx_lite", "首信易微信小程序支付", PayeasePayClientConfig.class),
    PAYEASE_WX_NATIVE("payease_wx_native", "首信易微信扫码支付", PayeasePayClientConfig.class),
    PAYEASE_ALIPAY("payease_alipay", "首信易支付宝支付", PayeasePayClientConfig.class),
    PAYEASE_ACCOUNT("payease_account", "首信易账户服务", PayeasePayClientConfig.class),

    WALLET("wallet", "钱包支付", NonePayClientConfig.class),

    ;

    /**
     * 编码
     */
    private final String code;
    /** 名字 */
    private final String name;
    /** 配置类 */
    private final Class<? extends PayClientConfig> configClass;

    /** 微信支付 */
    public static final String WECHAT = "WECHAT";
    /** 支付宝支付 */
    public static final String ALIPAY = "ALIPAY";
    /** 汇付天下支付 */
    public static final String HUIFU = "HUIFU";
    /** 首信易支付 */
    public static final String PAYEASE = "PAYEASE";

    public static PayChannelEnum getByCode(String code) {
        return ArrayUtil.firstMatch(o -> o.getCode().equals(code), values());
    }

    public static boolean isAlipay(String channelCode) {
        return channelCode != null && channelCode.startsWith("alipay");
    }

    public static boolean isWechat(String channelCode) {
        return channelCode != null && channelCode.startsWith("wx_");
    }

    public static boolean isHuifu(String channelCode) {
        return channelCode != null && channelCode.startsWith("huifu");
    }

    public static boolean isPayease(String channelCode) {
        return channelCode != null && channelCode.startsWith("payease");
    }
}
