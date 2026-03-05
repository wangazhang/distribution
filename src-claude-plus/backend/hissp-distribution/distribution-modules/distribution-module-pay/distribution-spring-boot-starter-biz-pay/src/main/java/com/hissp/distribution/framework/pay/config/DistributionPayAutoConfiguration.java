package com.hissp.distribution.framework.pay.config;

import com.hissp.distribution.framework.pay.core.client.PayClientFactory;
import com.hissp.distribution.framework.pay.core.client.impl.PayClientFactoryImpl;
import com.hissp.distribution.framework.pay.core.client.impl.payease.PayeaseJschSftpClient;
import com.hissp.distribution.framework.pay.core.client.impl.payease.PayeaseSftpService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 支付配置类
 *
 * @author 芋道源码
 */
@AutoConfiguration
@EnableConfigurationProperties(PayeaseSftpProperties.class)
public class DistributionPayAutoConfiguration {

    @Bean
    public PayClientFactory payClientFactory() {
        return new PayClientFactoryImpl();
    }

    // 注册首信易 SFTP 相关 Bean（位于 starter 包，不会被主工程组件扫描）
    @Bean
    public PayeaseJschSftpClient payeaseJschSftpClient() {
        return new PayeaseJschSftpClient();
    }

    @Bean
    public PayeaseSftpService payeaseSftpService() {
        return new PayeaseSftpService();
    }

}
