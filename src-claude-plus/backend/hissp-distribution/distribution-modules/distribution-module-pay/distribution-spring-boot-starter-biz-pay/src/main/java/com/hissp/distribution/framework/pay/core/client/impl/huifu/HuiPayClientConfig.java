package com.hissp.distribution.framework.pay.core.client.impl.huifu;

import com.hissp.distribution.framework.common.util.validation.ValidationUtils;
import com.hissp.distribution.framework.pay.core.client.PayClientConfig;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 汇付天下支付的配置类
 */
@Data
public class HuiPayClientConfig implements PayClientConfig {

    /**
     * 商户号
     */
    @NotBlank(message = "商户号不能为空")
    private String huifuId;

    /**
     * 产品号
     */
    @NotBlank(message = "产品号不能为空")
    private String productId;

    /**
     * 系统ID
     */
    @NotBlank(message = "系统ID不能为空")
    private String systemId;

    /**
     * 公钥
     */
    @NotBlank(message = "公钥不能为空")
    private String publicKey;

    /**
     * 私钥
     */
    @NotBlank(message = "私钥不能为空")
    private String privateKey;

    /**
     * 支持 RSA、证书两种方式
     * - RSA: 使用RSA加密方式
     * - CERT: 使用证书方式
     */
    @NotBlank(message = "签名类型不能为空")
    private String signType = "RSA";

    /**
     * 请求日志级别
     */
    private String logLevel = "INFO";

    /**
     * 证书地址，当 signType=CERT 时必填
     */
    private String certPath;

    /**
     * 证书密码，当 signType=CERT 时必填
     */
    private String certPwd;



    @Override
    public void validate(Validator validator) {
        ValidationUtils.validate(validator, this);
    }
}