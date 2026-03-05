package com.hissp.distribution.module.mp.framework.web.config;

import com.hissp.distribution.framework.swagger.config.DistributionSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mp 模块的 web 组件的 Configuration
 *
 * @author 芋道源码
 */
@Configuration(proxyBeanMethods = false)
public class MpWebConfiguration {

    /**
     * mp 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi mpGroupedOpenApi() {
        return DistributionSwaggerAutoConfiguration.buildGroupedOpenApi("mp");
    }

}
