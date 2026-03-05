package com.hissp.distribution;

import com.jcraft.jsch.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * SFTP客户端测试类 - 支持SSH跳板机连接和私钥认证
 *
 * 连接架构: 本地机器 --私钥认证--> SSH跳板机 --端口转发--> 目标SFTP服务器
 * 注意：这是一个测试类，实际使用时需要配置真实的服务器信息
 */
public class SftpProxyTest {

    private static final Logger logger = LoggerFactory.getLogger(SftpProxyTest.class);

    // SSH跳板机配置
    private String hopHost = "203.0.113.17";       // SSH跳板机地址，请替换为实际地址
    private int hopPort = 22;                             // 跳板机SSH端口
    private String hopUsername = "root";              // 跳板机用户名，请替换为实际用户名
    private String hopPrivateKeyPath = "/Users/qianzhang/.ssh/id_rsa";  // 跳板机私钥路径
    private String hopPrivateKeyPassphrase = null;        // 跳板机私钥密码（如果没有则为null）
    private boolean useHopServer = true;                  // 是否使用SSH跳板机

    // 目标SFTP服务器配置
    private String sftpHost = "merchant-sftp.5upay.com";  // SFTP服务器地址
    private int sftpPort = 2822;                          // SFTP端口
    private String sftpUsername = "891897377";            // SFTP用户名
    private String sftpPrivateKeyPath = "/root/.ssh/891897377.rsa";  // 目标SFTP私钥路径
    private String sftpPrivateKeyPassphrase = null;       // SFTP私钥密码（如果没有则为null）
    private boolean useSftpPrivateKey = true;             // 是否使用SFTP私钥认证

    // SSH会话管理
    private Session hopSession;        // 跳板机SSH会话
    private Session sftpSession;       // SFTP服务器SSH会话
    private ChannelSftp channelSftp;   // SFTP通道
    private int forwardedPort = -1;     // 转发的本地端口

    @BeforeEach
    public void setUp() throws Exception {
        logger.info("初始化SFTP连接...");
        initializeSftpConnection();
    }

    /**
     * 初始化SFTP连接（支持SSH跳板机和双层私钥认证）
     */
    private void initializeSftpConnection() throws Exception {
        JSch jsch = new JSch();

        if (useHopServer) {
            // 步骤1: 连接SSH跳板机
            initializeHopConnection(jsch);

            // 步骤2: 设置端口转发
            setupPortForwarding();

            // 步骤3: 连接目标SFTP服务器
            connectSftpThroughHop(jsch);
        } else {
            // 直接连接SFTP服务器（不使用跳板机）
            connectSftpDirectly(jsch);
        }
    }

    /**
     * 连接SSH跳板机
     */
    private void initializeHopConnection(JSch jsch) throws Exception {
        logger.info("连接SSH跳板机: {}:{}", hopHost, hopPort);

        // 检查跳板机私钥文件
        if (!Files.exists(Paths.get(hopPrivateKeyPath))) {
            throw new IllegalArgumentException("跳板机私钥文件不存在: " + hopPrivateKeyPath);
        }

        // 添加跳板机私钥
        if (hopPrivateKeyPassphrase != null && !hopPrivateKeyPassphrase.isEmpty()) {
            jsch.addIdentity(hopPrivateKeyPath, hopPrivateKeyPassphrase);
            logger.info("使用带密码的跳板机私钥: {}", hopPrivateKeyPath);
        } else {
            jsch.addIdentity(hopPrivateKeyPath);
            logger.info("使用无密码跳板机私钥: {}", hopPrivateKeyPath);
        }

        // 创建跳板机SSH会话
        hopSession = jsch.getSession(hopUsername, hopHost, hopPort);

        // 配置跳板机SSH参数
        Properties hopConfig = new Properties();
        hopConfig.put("StrictHostKeyChecking", "no");
        hopConfig.put("PreferredAuthentications", "publickey");
        hopSession.setConfig(hopConfig);

        // 连接跳板机
        hopSession.connect();
        logger.info("SSH跳板机连接成功");
    }

    /**
     * 设置端口转发
     */
    private void setupPortForwarding() throws JSchException {
        logger.info("设置端口转发: localhost:* -> {}:{}", sftpHost, sftpPort);

        // 设置本地端口转发：本地随机端口 -> 目标SFTP服务器
        forwardedPort = hopSession.setPortForwardingL(0, sftpHost, sftpPort);
        logger.info("端口转发设置成功: localhost:{} -> {}:{}", forwardedPort, sftpHost, sftpPort);
    }

    /**
     * 通过跳板机连接SFTP服务器
     */
    private void connectSftpThroughHop(JSch jsch) throws Exception {
        logger.info("通过跳板机连接SFTP服务器");

        // 在跳板机模式下，真正的SFTP操作需要在跳板机上执行
        // 我们需要将跳板机的私钥复制到本地，或者使用SSH代理转发

        // 方案1: 尝试将跳板机私钥内容复制到临时文件
        logger.info("尝试从跳板机获取SFTP私钥: {}", sftpPrivateKeyPath);

        try {
            // 通过跳板机读取私钥文件内容
            String privateKeyContent = getPrivateKeyContentFromHop();
            if (privateKeyContent != null) {
                // 创建临时私钥文件
                java.nio.file.Path tempKeyFile = java.nio.file.Files.createTempFile("sftp_temp_key", ".rsa");
                java.nio.file.Files.writeString(tempKeyFile, privateKeyContent);
                tempKeyFile.toFile().deleteOnExit();

                // 设置临时文件权限
                tempKeyFile.toFile().setReadable(true, false);
                tempKeyFile.toFile().setWritable(true, false);

                // 使用临时私钥文件
                if (sftpPrivateKeyPassphrase != null && !sftpPrivateKeyPassphrase.isEmpty()) {
                    jsch.addIdentity(tempKeyFile.toString(), sftpPrivateKeyPassphrase);
                    logger.info("使用临时SFTP私钥文件（带密码）");
                } else {
                    jsch.addIdentity(tempKeyFile.toString());
                    logger.info("使用临时SFTP私钥文件（无密码）");
                }
            } else {
                logger.warn("无法从跳板机获取SFTP私钥，尝试其他认证方式");
            }
        } catch (Exception e) {
            logger.warn("获取跳板机SFTP私钥失败，尝试连接: {}", e.getMessage());
        }

        // 连接转发的本地端口
        sftpSession = jsch.getSession(sftpUsername, "localhost", forwardedPort);

        // 配置SFTP SSH参数
        Properties sftpConfig = new Properties();
        sftpConfig.put("StrictHostKeyChecking", "no");
        sftpConfig.put("PreferredAuthentications", "publickey,password");
        sftpSession.setConfig(sftpConfig);

        // 连接SFTP会话
        sftpSession.connect();
        logger.info("通过跳板机连接SFTP服务器成功");

        // 打开SFTP通道
        Channel channel = sftpSession.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;
        logger.info("SFTP通道连接成功");
    }

    /**
     * 从跳板机获取SFTP私钥内容
     */
    private String getPrivateKeyContentFromHop() throws Exception {
        logger.info("从跳板机读取SFTP私钥文件: {}", sftpPrivateKeyPath);

        // 创建一个执行命令的通道
        ChannelExec execChannel = (ChannelExec) hopSession.openChannel("exec");

        // 构建读取私钥文件的命令
        String command = "cat " + sftpPrivateKeyPath;
        execChannel.setCommand(command);

        // 读取命令输出
        java.io.InputStream in = execChannel.getInputStream();
        java.io.ByteArrayOutputStream errorStream = new java.io.ByteArrayOutputStream();
        execChannel.setErrStream(errorStream);

        execChannel.connect();

        // 读取私钥内容
        java.util.Scanner scanner = new java.util.Scanner(in, "UTF-8");
        StringBuilder privateKeyContent = new StringBuilder();
        while (scanner.hasNextLine()) {
            privateKeyContent.append(scanner.nextLine()).append("\n");
        }
        scanner.close();

        // 等待命令完成
        while (!execChannel.isClosed()) {
            Thread.sleep(100);
        }
        execChannel.disconnect();

        // 检查执行结果
        if (execChannel.getExitStatus() != 0) {
            String errorMsg = errorStream.toString();
            logger.error("读取跳板机私钥文件失败: {}", errorMsg);
            return null;
        }

        String content = privateKeyContent.toString().trim();
        logger.info("成功从跳板机读取私钥内容，长度: {} 字符", content.length());
        return content;
    }

    /**
     * 直接连接SFTP服务器（不使用跳板机）
     */
    private void connectSftpDirectly(JSch jsch) throws Exception {
        logger.info("直接连接SFTP服务器: {}:{}", sftpHost, sftpPort);

        // 检查SFTP私钥文件
        if (!Files.exists(Paths.get(sftpPrivateKeyPath))) {
            throw new IllegalArgumentException("SFTP私钥文件不存在: " + sftpPrivateKeyPath);
        }

        // 添加SFTP私钥
        if (sftpPrivateKeyPassphrase != null && !sftpPrivateKeyPassphrase.isEmpty()) {
            jsch.addIdentity(sftpPrivateKeyPath, sftpPrivateKeyPassphrase);
            logger.info("使用带密码的SFTP私钥: {}", sftpPrivateKeyPath);
        } else {
            jsch.addIdentity(sftpPrivateKeyPath);
            logger.info("使用无密码SFTP私钥: {}", sftpPrivateKeyPath);
        }

        // 创建SFTP会话
        sftpSession = jsch.getSession(sftpUsername, sftpHost, sftpPort);

        // 配置SFTP SSH参数
        Properties sftpConfig = new Properties();
        sftpConfig.put("StrictHostKeyChecking", "no");
        sftpConfig.put("PreferredAuthentications", "publickey");
        sftpSession.setConfig(sftpConfig);

        // 连接SFTP会话
        sftpSession.connect();
        logger.info("SFTP服务器连接成功");

        // 打开SFTP通道
        Channel channel = sftpSession.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;
        logger.info("SFTP通道连接成功");
    }

    /**
     * 测试上传文件到SFTP服务器
     */
    @Test
    //@Disabled("需要配置真实的服务器信息才能运行")
    public void testUploadFile() throws Exception {
        logger.info("开始测试文件上传...");

        // 准备测试文件内容
        String fileContent = "这是一个测试文件内容\n测试时间: " + new java.util.Date();
        String fileName = "test_file_" + System.currentTimeMillis() + ".txt";

        // 获取当前工作目录
        String currentDir = channelSftp.pwd();
        logger.info("当前工作目录: {}", currentDir);

        // 尝试多个可能的路径
        String[] possiblePaths = {
            "/transfer/card/" + fileName,
            "/ftp/" + fileName,
            "/data/" + fileName,
            "/files/" + fileName,
            currentDir + "/" + fileName,
            "./" + fileName,
            "/" + fileName
        };

        boolean uploadSuccess = false;
        String successPath = null;

        for (String path : possiblePaths) {
            try {
                logger.info("尝试上传到路径: {}", path);

                // 尝试上传文件
                try (InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes("UTF-8"))) {
                    channelSftp.put(inputStream, path);
                }

                // 上传成功，验证文件
                verifyFileAndShowPath(path);
                successPath = path;
                uploadSuccess = true;
                logger.info("文件上传成功: {}", path);
                break;

            } catch (SftpException e) {
                logger.warn("上传到 {} 失败: {}", path, e.getMessage());
            }
        }

        if (!uploadSuccess) {
            // 如果所有预设路径都失败，尝试创建上传目录
            try {
                String uploadPath = "/transfer/card/" + fileName;
                createDirectoryIfNotExists("/transfer/card");

                try (InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes("UTF-8"))) {
                    channelSftp.put(inputStream, uploadPath);
                }

                verifyFileAndShowPath(uploadPath);
                logger.info("文件上传成功（创建目录后）: {}", uploadPath);

            } catch (Exception e) {
                logger.error("所有上传尝试均失败", e);
                throw e;
            }
        }
    }

    /**
     * 创建目录（如果不存在）
     */
    private void createDirectoryIfNotExists(String dirPath) throws SftpException {
        try {
            // 尝试进入目录
            String currentDir = channelSftp.pwd();
            channelSftp.cd(dirPath);
            channelSftp.cd(currentDir); // 返回原目录
            logger.info("目录已存在: {}", dirPath);
        } catch (SftpException e) {
            // 目录不存在，尝试创建
            try {
                channelSftp.mkdir(dirPath);
                logger.info("创建目录成功: {}", dirPath);
            } catch (SftpException mkdirEx) {
                logger.error("创建目录失败: {}", dirPath);
                throw mkdirEx;
            }
        }
    }

    /**
     * 验证文件并显示路径信息
     */
    private void verifyFileAndShowPath(String filePath) throws SftpException {
        logger.info("验证文件: {}", filePath);

        // 获取文件属性
        SftpATTRS attrs = channelSftp.stat(filePath);
        logger.info("文件大小: {} bytes", attrs.getSize());
        logger.info("文件修改时间: {}", new java.util.Date(attrs.getMTime() * 1000L));

        // 获取当前工作目录
        String currentDir = channelSftp.pwd();
        logger.info("当前工作目录: {}", currentDir);

        // 如果是相对路径，构造完整路径
        String fullPath = filePath.startsWith("/") ? filePath : currentDir + "/" + filePath;
        logger.info("文件完整路径: {}", fullPath);

        // 列出文件所在目录的内容
        String dirPath = filePath.substring(0, filePath.lastIndexOf('/'));
        if (!dirPath.isEmpty()) {
            channelSftp.cd(dirPath);
            logger.info("切换到目录: {}", channelSftp.pwd());

            // 列出目录内容
            java.util.Vector<ChannelSftp.LsEntry> files = channelSftp.ls(".");
            logger.info("目录内容:");
            for (ChannelSftp.LsEntry entry : files) {
                logger.info("  {} ({} bytes)", entry.getFilename(), entry.getAttrs().getSize());
            }
        }
    }

    /**
     * 测试SSH跳板机连接
     */
    @Test
    //@Disabled("需要配置真实的服务器信息才能运行")
    public void testHopConnection() throws Exception {
        logger.info("开始测试SSH跳板机连接...");

        try {
            // 检查连接状态
            boolean isActive = isConnectionActive();
            logger.info("连接状态: {}", isActive ? "活跃" : "非活跃");

            if (isActive) {
                // 获取当前工作目录
                String currentDir = channelSftp.pwd();
                logger.info("当前工作目录: {}", currentDir);

                // 列出当前目录内容
                logger.info("当前目录内容:");
                listDirectoryContents(".");

                // 列出根目录内容
                logger.info("根目录内容:");
                listDirectoryContents("/");

                // 查找可能的上传目录
                logger.info("查找可能的上传目录:");
                findUploadDirectories("/");
            }

            logger.info("SSH跳板机连接测试完成");

        } catch (Exception e) {
            logger.error("SSH跳板机连接测试失败", e);
            throw e;
        }
    }

    /**
     * 列出目录内容
     */
    private void listDirectoryContents(String path) throws SftpException {
        try {
            java.util.Vector<ChannelSftp.LsEntry> files = channelSftp.ls(path);
            for (ChannelSftp.LsEntry entry : files) {
                String type = entry.getAttrs().isDir() ? "目录" : "文件";
                long size = entry.getAttrs().getSize();
                logger.info("  {} [{}] - {} bytes", entry.getFilename(), type, size);
            }
        } catch (SftpException e) {
            logger.warn("无法访问目录 {}: {}", path, e.getMessage());
        }
    }

    /**
     * 查找上传目录
     */
    private void findUploadDirectories(String path) throws SftpException {
        try {
            java.util.Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);
            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (entry.getAttrs().isDir() && !name.equals(".") && !name.equals("..")) {
                    String fullPath = path.equals("/") ? "/" + name : path + "/" + name;

                    // 检查是否是上传相关目录
                    if (name.toLowerCase().contains("upload") ||
                        name.toLowerCase().contains("ftp") ||
                        name.toLowerCase().contains("data") ||
                        name.toLowerCase().contains("file")) {
                        logger.info("发现可能的目录: {}", fullPath);
                        listDirectoryContents(fullPath);
                    }

                    // 递归查找（限制深度避免无限递归）
                    if (path.split("/").length <= 3) {
                        findUploadDirectories(fullPath);
                    }
                }
            }
        } catch (SftpException e) {
            logger.debug("无法扫描目录 {}: {}", path, e.getMessage());
        }
    }

    /**
     * 测试列出远程目录文件
     */
    @Test
    //@Disabled("需要配置真实的服务器信息才能运行")
    public void testListFiles() throws Exception {
        logger.info("开始测试列出文件...");

        try {
            // 获取当前工作目录
            String currentDir = channelSftp.pwd();
            logger.info("当前工作目录: {}", currentDir);

            // 尝试列出多个目录的内容
            String[] directoriesToCheck = {
                ".",
                "/",
                "/transfer/card",
                "/ftp",
                "/data",
                "/files"
            };

            for (String dir : directoriesToCheck) {
                try {
                    logger.info("尝试列出目录: {}", dir);
                    listDirectoryContents(dir);
                } catch (SftpException e) {
                    logger.warn("无法访问目录 {}: {}", dir, e.getMessage());
                }
            }

        } catch (Exception e) {
            logger.error("列出文件测试失败", e);
            throw e;
        }
    }

    /**
     * 清理资源（包括跳板机和SFTP会话）
     */
    public void tearDown() {
        try {
            // 清理SFTP通道
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
                logger.info("SFTP通道已断开");
            }

            // 清理SFTP会话
            if (sftpSession != null && sftpSession.isConnected()) {
                sftpSession.disconnect();
                logger.info("SFTP会话已断开");
            }

            // 清理跳板机会话
            if (hopSession != null && hopSession.isConnected()) {
                hopSession.disconnect();
                logger.info("SSH跳板机会话已断开");
            }

            // 重置端口转发状态
            forwardedPort = -1;
            logger.info("资源清理完成");

        } catch (Exception e) {
            logger.error("清理资源时发生错误", e);
        }
    }

    /**
     * 检查连接状态
     */
    public boolean isConnectionActive() {
        try {
            boolean sftpActive = channelSftp != null && channelSftp.isConnected();
            boolean sftpSessionActive = sftpSession != null && sftpSession.isConnected();
            boolean hopSessionActive = !useHopServer || (hopSession != null && hopSession.isConnected());

            return sftpActive && sftpSessionActive && hopSessionActive;
        } catch (Exception e) {
            logger.warn("检查连接状态时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查跳板机私钥文件是否存在和可读
     */
    public boolean checkHopPrivateKeyFile() {
        if (!useHopServer) {
            logger.info("未使用跳板机，跳过跳板机私钥检查");
            return true;
        }

        if (hopPrivateKeyPath == null || hopPrivateKeyPath.isEmpty()) {
            logger.error("跳板机私钥路径未设置");
            return false;
        }

        try {
            java.nio.file.Path path = Paths.get(hopPrivateKeyPath);
            if (!Files.exists(path)) {
                logger.error("跳板机私钥文件不存在: {}", hopPrivateKeyPath);
                return false;
            }

            if (!Files.isReadable(path)) {
                logger.error("跳板机私钥文件不可读: {}", hopPrivateKeyPath);
                return false;
            }

            logger.info("跳板机私钥文件检查通过: {}", hopPrivateKeyPath);
            return true;
        } catch (Exception e) {
            logger.error("检查跳板机私钥文件时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查SFTP私钥文件是否存在和可读
     */
    public boolean checkSftpPrivateKeyFile() {
        if (!useSftpPrivateKey) {
            logger.info("未使用SFTP私钥认证，跳过SFTP私钥检查");
            return true;
        }

        // 如果使用跳板机，SFTP私钥在跳板机上，直接返回成功
        if (useHopServer) {
            logger.info("使用跳板机连接，SFTP私钥文件在跳板机上: {}，跳过所有本地检查", sftpPrivateKeyPath);
            return true;
        }

        // 直接连接时需要检查本地SFTP私钥文件
        if (sftpPrivateKeyPath == null || sftpPrivateKeyPath.isEmpty()) {
            logger.error("SFTP私钥路径未设置");
            return false;
        }

        try {
            java.nio.file.Path path = Paths.get(sftpPrivateKeyPath);
            if (!Files.exists(path)) {
                logger.error("SFTP私钥文件不存在: {}", sftpPrivateKeyPath);
                return false;
            }

            if (!Files.isReadable(path)) {
                logger.error("SFTP私钥文件不可读: {}", sftpPrivateKeyPath);
                return false;
            }

            logger.info("本地SFTP私钥文件检查通过: {}", sftpPrivateKeyPath);
            return true;
        } catch (Exception e) {
            logger.error("检查SFTP私钥文件时发生错误: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查所有私钥文件
     */
    public boolean checkAllPrivateKeyFiles() {
        boolean hopKeyValid = checkHopPrivateKeyFile();
        boolean sftpKeyValid = checkSftpPrivateKeyFile();
        return hopKeyValid && sftpKeyValid;
    }

    /**
     * 静态方法：使用示例
     */
    public static void main(String[] args) {
        SftpProxyTest test = new SftpProxyTest();

        try {
            // SSH跳板机配置（请替换为实际配置）
            //test.hopHost = "your-hop-server.com";      // 替换为实际跳板机地址
            //test.hopPort = 22;
            //test.hopUsername = "hop-user";             // 替换为实际跳板机用户名
            //test.hopPrivateKeyPath = "/Users/qianzhang/.ssh/hop_key";  // 替换为跳板机私钥路径
            //test.hopPrivateKeyPassphrase = null;       // 如果跳板机私钥有密码，请设置
            //test.useHopServer = true;                  // 是否使用跳板机
            //
            //// 目标SFTP服务器配置
            //test.sftpHost = "merchant-sftp.5upay.com";
            //test.sftpPort = 2822;
            //test.sftpUsername = "891897377";
            //test.sftpPrivateKeyPath = "/Users/qianzhang/.ssh/merchant_sftp_key";  // 替换为SFTP私钥路径
            //test.sftpPrivateKeyPassphrase = null;       // 如果SFTP私钥有密码，请设置
            //test.useSftpPrivateKey = true;

            // 检查所有私钥文件
            if (!test.checkAllPrivateKeyFiles()) {
                System.err.println("私钥文件检查失败，请检查私钥路径配置");
                return;
            }

            // 初始化连接
            test.setUp();

            // 执行SSH跳板机连接测试
            test.testHopConnection();

            // 执行文件上传测试
            test.testUploadFile();

            // 执行列出文件测试
            test.testListFiles();

        } catch (Exception e) {
            System.err.println("测试执行失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 清理资源
            test.tearDown();
        }
    }
}