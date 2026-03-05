package com.hissp.distribution.framework.pay.core.client.impl.payease;

import com.hissp.distribution.framework.pay.core.client.PayClientConfig;
import lombok.Data;

import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 首信易支付的 PayClientConfig 实现类
 *
 * @author system
 */
@Data
public class PayeasePayClientConfig implements PayClientConfig {

    /**
     * 商户号（9位数字）
     */
    @NotBlank(message = "商户号不能为空")
    @Pattern(regexp = "^\\d{9}$", message = "商户号必须是9位数字")
    private String merchantId;

    /**
     * 客户端私钥路径（.pfx文件）
     */
    @NotBlank(message = "私钥路径不能为空")
    private String privateKeyPath;

    /**
     * 私钥密码
     */
    @NotBlank(message = "私钥密码不能为空")
    private String privateKeyPassword;

    /**
     * 服务器公钥路径（.cer文件）
     */
    @NotBlank(message = "公钥路径不能为空")
    private String publicKeyPath;

    /**
     * 服务商ID（选填，服务商模式时使用）
     */
    private String partnerId;


    @Override
    public void validate(Validator validator) {
        // 首信易支付配置暂时不需要复杂的验证逻辑
        // 只使用基础的字段验证注解即可
    }

}
