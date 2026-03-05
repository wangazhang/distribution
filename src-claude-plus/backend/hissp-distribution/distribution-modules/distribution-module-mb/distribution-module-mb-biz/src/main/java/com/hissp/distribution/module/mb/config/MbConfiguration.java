package com.hissp.distribution.module.mb.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

/**
 * MB模块配置类
 * 配置MB模块相关的Bean
 *
 * 注意：不需要额外的@MapperScan，因为全局的DistributionMybatisAutoConfiguration
 * 已经扫描了整个com.hissp.distribution包下的@Mapper注解
 *
 * @author system
 */
@Configuration
@Slf4j
public class MbConfiguration {

    public MbConfiguration() {
        log.info("[MbConfiguration][MB模块配置初始化]");
    }

}
