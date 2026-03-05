package com.hissp.distribution.framework.common.util.spring;

import cn.hutool.extra.spring.SpringUtil;

import java.util.Objects;

/**
 * Spring 工具类
 *
 * @author 芋道源码
 */
public class SpringUtils extends SpringUtil {

    /**
     * 是否为生产环境
     *
     * @return 是否生产环境
     */
    public static boolean isProd() {
        String activeProfile = getActiveProfile();
        return Objects.equals("prod", activeProfile);
    }

    /**
     * 获取环境目录名称
     * 用于文件存储时区分不同环境
     *
     * @return 环境目录名称 (prod-生产环境, dev-开发环境, test-测试环境等)
     */
    public static String getEnvDir() {
        String activeProfile = getActiveProfile();
        // 如果未配置环境,默认为 dev
        return activeProfile != null ? activeProfile : "dev";
    }

}
