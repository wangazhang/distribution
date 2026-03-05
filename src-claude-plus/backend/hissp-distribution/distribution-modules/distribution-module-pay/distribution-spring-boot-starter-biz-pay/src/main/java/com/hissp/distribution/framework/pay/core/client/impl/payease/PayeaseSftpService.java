package com.hissp.distribution.framework.pay.core.client.impl.payease;

import cn.hutool.extra.ssh.Sftp;
import com.hissp.distribution.framework.pay.config.PayeaseSftpProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PayeaseSftpService {

    @Resource
    private PayeaseSftpProperties props;

    @Resource
    private PayeaseJschSftpClient jschClient;

    @Resource
    private Environment environment;

    /**
     * 上传二进制文件至首信易SFTP
     * - 开发环境/需要代理：通过跳板机转发 + 私钥认证（参考 SftpProxyTest）
     * - 生产/测试环境：直连 SFTP（优先私钥，其次密码）
     */
    public String upload(byte[] content, String remotePath) {
        String[] profilesArr = environment != null ? environment.getActiveProfiles() : new String[0];
        String profile = (profilesArr != null && profilesArr.length > 0) ? String.join(",", profilesArr)
                : (environment != null ? environment.getProperty("spring.profiles.active", "default") : "default");
        boolean useProxy = Boolean.TRUE.equals(props.getUseProxy());
        String proxyInfo = useProxy
                ? String.format("hop=%s@%s:%d, sftp=%s:%d, user=%s",
                nullToEmpty(props.getHopUsername()), nullToEmpty(props.getHopHost()), props.getHopPort() != null ? props.getHopPort() : 22,
                nullToEmpty(props.getHost()), props.getPort() != null ? props.getPort() : 22, nullToEmpty(props.getUsername()))
                : String.format("sftp=%s:%d, user=%s",
                nullToEmpty(props.getHost()), props.getPort() != null ? props.getPort() : 22, nullToEmpty(props.getUsername()));
        log.info("[PayeaseSftpService][profile={}, useProxy={}, info={}]", profile, useProxy, proxyInfo);

        try {
            // 统一走 JSCH 客户端，内部根据 props.useProxy/usePrivateKey 选择实现
            return jschClient.upload(content, remotePath);
        } catch (Exception e) {
            log.warn("[PayeaseSftpService][jsch upload failed, fallback hutool] {}", e.getMessage());
        }
        // 白名单环境（useProxy=true）禁止直连兜底，直接抛错以便上层处理
        if (Boolean.TRUE.equals(props.getUseProxy())) {
            throw new RuntimeException("SFTP upload failed via proxy and direct connection is disabled by whitelist");
        }
        // 非代理环境才允许兜底：仅当必须用密码时使用 Hutool Sftp
        Sftp sftp = new Sftp(props.getHost(), props.getPort(), props.getUsername(), props.getPassword());
        String fullPath = formatPath(remotePath);
        try {
            sftp.upload(fullPath, cn.hutool.core.io.FileUtil.writeBytes(content, cn.hutool.core.io.FileUtil.createTempFile()));
            log.info("[PayeaseSftpService][upload ok] {}", fullPath);
            return fullPath;
        } finally {
            try { sftp.close(); } catch (Exception ignore) {}
        }
    }

    private String formatPath(String path) {
        String base = props.getBasePath();
        if (!base.endsWith("/")) base = base + "/";
        return base + path;
    }

    private String nullToEmpty(String s) { return s == null ? "" : s; }
}
