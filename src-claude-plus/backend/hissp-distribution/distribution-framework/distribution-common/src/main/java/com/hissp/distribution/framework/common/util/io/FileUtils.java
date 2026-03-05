package com.hissp.distribution.framework.common.util.io;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 文件工具类
 *
 * @author 芋道源码
 */
public class FileUtils {

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile(String data) {
        File file = createTempFile();
        // 写入内容
        FileUtil.writeUtf8String(data, file);
        return file;
    }

    /**
     * 创建临时文件
     * 该文件会在 JVM 退出时，进行删除
     *
     * @param data 文件内容
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile(byte[] data) {
        File file = createTempFile();
        // 写入内容
        FileUtil.writeBytes(data, file);
        return file;
    }

    /**
     * 创建临时文件，无内容
     * 该文件会在 JVM 退出时，进行删除
     *
     * @return 文件
     */
    @SneakyThrows
    public static File createTempFile() {
        // 创建文件，通过 UUID 保证唯一
        File file = File.createTempFile(IdUtil.simpleUUID(), null);
        // 标记 JVM 退出时，自动删除
        file.deleteOnExit();
        return file;
    }

    /**
     * 生成文件路径
     *
     * @param content      文件内容
     * @param originalName 原始文件名
     * @return path，唯一不可重复，格式: yyyy/MM/dd/sha256值.扩展名
     */
    public static String generatePath(byte[] content, String originalName) {
        return generatePath(content, originalName, null);
    }

    /**
     * 生成文件路径(支持环境目录)
     *
     * @param content      文件内容
     * @param originalName 原始文件名
     * @param envDir       环境目录 (如: prod, dev, test), 为空时不添加环境目录
     * @return path，唯一不可重复，格式: envDir/yyyy/MM/dd/sha256值.扩展名 或 yyyy/MM/dd/sha256值.扩展名
     */
    public static String generatePath(byte[] content, String originalName, String envDir) {
        // 生成日期目录: yyyy/MM/dd
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 生成文件名 (SHA256 哈希值)
        String sha256Hex = DigestUtil.sha256Hex(content);
        String fileName;

        // 情况一：如果存在 name，则优先使用 name 的后缀
        if (StrUtil.isNotBlank(originalName)) {
            String extName = FileNameUtil.extName(originalName);
            fileName = StrUtil.isBlank(extName) ? sha256Hex : sha256Hex + "." + extName;
        } else {
            // 情况二：基于 content 计算
            fileName = sha256Hex + '.' + FileTypeUtil.getType(new ByteArrayInputStream(content));
        }

        // 拼接路径: 环境目录/日期目录/文件名
        if (StrUtil.isNotBlank(envDir)) {
            return envDir + "/" + dateDir + "/" + fileName;
        } else {
            return dateDir + "/" + fileName;
        }
    }

}
