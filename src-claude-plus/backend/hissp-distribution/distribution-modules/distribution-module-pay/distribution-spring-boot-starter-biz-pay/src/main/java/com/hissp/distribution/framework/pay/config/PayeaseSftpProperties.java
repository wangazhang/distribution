package com.hissp.distribution.framework.pay.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "payease.sftp")
public class PayeaseSftpProperties {
    private String host;
    private Integer port = 22;
    private String username;
    private String password; // 密码认证
    private String privateKeyPath; // 私钥认证路径（参考 SftpProxyTest）
    private String privateKeyPassphrase; // 私钥密码
    private Boolean usePrivateKey = Boolean.TRUE; // 默认使用私钥
    private String basePath = "/"; // 远程根目录前缀

    // 代理（跳板机）配置：开发环境通过跳板机访问SFTP
    private Boolean useProxy = Boolean.FALSE; // 是否启用代理上传
    private String hopHost; // 跳板机地址
    private Integer hopPort = 22; // 跳板机端口
    private String hopUsername; // 跳板机用户名
    private String hopPrivateKeyPath; // 跳板机私钥路径
    private String hopPrivateKeyPassphrase; // 跳板机私钥密码（可为空）

    /**
     * SFTP 连接池大小，默认 4（并行上传情况下需要复用连接，避免频繁握手）
     */
    private Integer poolSize = 4;

    /**
     * 与 SFTP 建立连接的超时时长，默认 10s
     */
    private Integer connectTimeout = 10000;
}
