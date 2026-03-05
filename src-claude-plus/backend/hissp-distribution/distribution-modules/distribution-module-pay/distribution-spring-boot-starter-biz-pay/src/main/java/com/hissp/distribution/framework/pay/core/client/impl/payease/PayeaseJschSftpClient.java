package com.hissp.distribution.framework.pay.core.client.impl.payease;

import com.hissp.distribution.framework.pay.config.PayeaseSftpProperties;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Slf4j
public class PayeaseJschSftpClient {

    @Resource
    private PayeaseSftpProperties props;
    /**
     * 简单的 SFTP 连接池，用于复用 Session/Channel，避免并发上传时频繁握手
     */
    private BlockingQueue<SftpHolder> pool;

    @PostConstruct
    public void init() {
        int size = props.getPoolSize() == null || props.getPoolSize() <= 0 ? 2 : props.getPoolSize();
        pool = new LinkedBlockingQueue<>(size);
    }

    @PreDestroy
    public void destroy() {
        if (pool == null) {
            return;
        }
        pool.forEach(this::closeQuietly);
        pool.clear();
    }

    public String upload(byte[] content, String remotePath) throws Exception {
        SftpHolder holder = borrow();
        boolean success = false;
        try {
            // 确保目录存在（简化：假设远端存在 basePath）
            String fullPath = formatPath(remotePath);
            ensureRemoteDirectories(holder.channelSftp, fullPath);
            holder.channelSftp.put(new ByteArrayInputStream(content), fullPath);
            log.info("[PayeaseJschSftpClient][upload ok] {}", fullPath);
            success = true;
            return fullPath;
        } finally {
            if (success) {
                release(holder);
            } else {
                closeQuietly(holder);
            }
        }
    }

    private String formatPath(String path) {
        if (path == null) return "/";
        // 绝对路径则不再拼接 basePath
        if (path.startsWith("/")) {
            return path;
        }
        String base = props.getBasePath();
        if (base == null) base = "/";
        if (!base.endsWith("/")) base = base + "/";
        return base + path;
    }

    /**
     * 直接从跳板机读取私钥文件内容
     */
    private String getPrivateKeyContentFromHop(Session hopSession, String remoteKeyPath) {
        ChannelExec exec = null;
        try {
            exec = (ChannelExec) hopSession.openChannel("exec");
            exec.setCommand("cat " + remoteKeyPath);
            InputStream in = exec.getInputStream();
            java.io.ByteArrayOutputStream err = new java.io.ByteArrayOutputStream();
            exec.setErrStream(err);
            exec.connect(5000);
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            while (!exec.isClosed()) { Thread.sleep(50); }
            if (exec.getExitStatus() != 0 || content.isBlank()) {
                log.warn("[PayeaseJschSftpClient][getPrivateKeyContentFromHop] exitStatus={}, err={}", exec.getExitStatus(), err.toString());
                return null;
            }
            return content;
        } catch (Exception e) {
            log.warn("[PayeaseJschSftpClient][getPrivateKeyContentFromHop] failed: {}", e.getMessage());
            return null;
        } finally {
            if (exec != null) {
                try { exec.disconnect(); } catch (Exception ignore) {}
            }
        }
    }

    private void ensureRemoteDirectories(ChannelSftp sftp, String fullPath) throws SftpException {
        try {
            sftp.cd("/");
        } catch (SftpException ignore) {
        }
        String path = fullPath;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        int idx = path.lastIndexOf('/');
        if (idx <= 0) {
            return;
        }
        String dir = path.substring(0, idx);
        String[] parts = dir.split("/");
        String current = "";
        for (String part : parts) {
            if (part == null || part.isEmpty()) {
                continue;
            }
            current = current + "/" + part;
            try {
                sftp.cd(current);
            } catch (SftpException e) {
                try { sftp.mkdir(current); } catch (SftpException ignore) {}
                sftp.cd(current);
            }
        }
    }

    /**
     * 从池中借出一个可用连接，如无可用连接则新建
     */
    private SftpHolder borrow() throws Exception {
        SftpHolder holder = pool.poll();
        if (holder != null && holder.isAlive()) {
            return holder;
        }
        if (holder != null) {
            closeQuietly(holder);
        }
        return createConnection();
    }

    /**
     * 归还连接，若池已满或连接失效则直接关闭
     */
    private void release(SftpHolder holder) {
        if (holder == null) {
            return;
        }
        if (!holder.isAlive() || !pool.offer(holder)) {
            closeQuietly(holder);
        }
    }

    private void closeQuietly(SftpHolder holder) {
        if (holder == null) {
            return;
        }
        try { holder.channelSftp.exit(); } catch (Exception ignore) {}
        try { holder.channel.disconnect(); } catch (Exception ignore) {}
        try { holder.session.disconnect(); } catch (Exception ignore) {}
        try { if (holder.hopSession != null) holder.hopSession.disconnect(); } catch (Exception ignore) {}
    }

    private SftpHolder createConnection() throws Exception {
        JSch jsch = new JSch();
        Session session;
        Session hopSession = null;
        ChannelSftp sftp;
        Channel channel;
        if (Boolean.TRUE.equals(props.getUseProxy())) {
            hopSession = buildHopSession(jsch);
            int forwardedPort = hopSession.setPortForwardingL(0, props.getHost(), props.getPort());
            configureSftpIdentityFromHop(jsch, hopSession);
            session = jsch.getSession(props.getUsername(), "localhost", forwardedPort);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "publickey");
            session.setConfig(config);
            session.connect(props.getConnectTimeout());
            channel = session.openChannel("sftp");
            channel.connect(props.getConnectTimeout());
            sftp = (ChannelSftp) channel;
        } else {
            configureDirectIdentity(jsch);
            session = jsch.getSession(props.getUsername(), props.getHost(), props.getPort());
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            if (Boolean.TRUE.equals(props.getUsePrivateKey())) {
                config.put("PreferredAuthentications", "publickey");
            }
            session.setConfig(config);
            if (!Boolean.TRUE.equals(props.getUsePrivateKey())) {
                session.setPassword(props.getPassword());
            }
            session.connect(props.getConnectTimeout());
            channel = session.openChannel("sftp");
            channel.connect(props.getConnectTimeout());
            sftp = (ChannelSftp) channel;
        }
        return new SftpHolder(session, hopSession, channel, sftp);
    }

    private Session buildHopSession(JSch jsch) throws Exception {
        if (props.getHopPrivateKeyPath() != null) {
            if (props.getHopPrivateKeyPassphrase() != null && !props.getHopPrivateKeyPassphrase().isEmpty()) {
                jsch.addIdentity(props.getHopPrivateKeyPath(), props.getHopPrivateKeyPassphrase());
            } else {
                jsch.addIdentity(props.getHopPrivateKeyPath());
            }
        }
        Session hopSession = jsch.getSession(props.getHopUsername(), props.getHopHost(), props.getHopPort());
        java.util.Properties hopConfig = new java.util.Properties();
        hopConfig.put("StrictHostKeyChecking", "no");
        hopConfig.put("PreferredAuthentications", "publickey");
        hopSession.setConfig(hopConfig);
        hopSession.connect(props.getConnectTimeout());
        return hopSession;
    }

    private void configureSftpIdentityFromHop(JSch jsch, Session hopSession) {
        if (!Boolean.TRUE.equals(props.getUsePrivateKey())) {
            return;
        }
        String keyPath = props.getPrivateKeyPath();
        String keyContent = getPrivateKeyContentFromHop(hopSession, keyPath);
        if (keyContent == null || keyContent.isBlank()) {
            throw new RuntimeException("get sftp private key content from hop failed: " + keyPath);
        }
        byte[] keyBytes = keyContent.getBytes(StandardCharsets.UTF_8);
        byte[] passBytes = (props.getPrivateKeyPassphrase() != null && !props.getPrivateKeyPassphrase().isEmpty())
                ? props.getPrivateKeyPassphrase().getBytes(StandardCharsets.UTF_8)
                : null;
        try {
            jsch.addIdentity("sftp-key-from-hop", keyBytes, null, passBytes);
        } catch (Exception e) {
            throw new RuntimeException("add identity for sftp failed", e);
        }
        log.info("[PayeaseJschSftpClient][use hop] add identity from hop content: {}", keyPath);
    }

    private void configureDirectIdentity(JSch jsch) throws Exception {
        if (Boolean.TRUE.equals(props.getUsePrivateKey()) && props.getPrivateKeyPath() != null) {
            if (props.getPrivateKeyPassphrase() != null && !props.getPrivateKeyPassphrase().isEmpty()) {
                jsch.addIdentity(props.getPrivateKeyPath(), props.getPrivateKeyPassphrase());
            } else {
                jsch.addIdentity(props.getPrivateKeyPath());
            }
        }
    }

    /**
     * 对 Session/Channel/ChannelSftp 的包装，便于池化和健康检查
     */
    private static final class SftpHolder {
        private final Session session;
        private final Session hopSession;
        private final Channel channel;
        private final ChannelSftp channelSftp;

        private SftpHolder(Session session, Session hopSession, Channel channel, ChannelSftp channelSftp) {
            this.session = session;
            this.hopSession = hopSession;
            this.channel = channel;
            this.channelSftp = channelSftp;
        }

        private boolean isAlive() {
            return session != null && session.isConnected()
                    && channel != null && channel.isConnected()
                    && channelSftp != null && channelSftp.isConnected()
                    && (hopSession == null || hopSession.isConnected());
        }
    }
}
