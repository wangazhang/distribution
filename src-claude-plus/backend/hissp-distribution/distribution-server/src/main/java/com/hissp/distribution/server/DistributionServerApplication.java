package com.hissp.distribution.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 *
 * 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
 * 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
 *
 * @author 芋道源码
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${distribution.info.base-package}
@SpringBootApplication(scanBasePackages = {"${distribution.info.base-package}.server", "${distribution.info.base-package}.module"})
public class DistributionServerApplication {

    public static void main(String[] args) {
        // 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章

        SpringApplication.run(DistributionServerApplication.class, args);
//        new SpringApplicationBuilder(DistributionServerApplication.class)
//                .applicationStartup(new BufferingApplicationStartup(20480))
//                .run(args);

        // 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
        // 如果你碰到启动的问题，请认真阅读 https://doc.example.com/quick-start/ 文章
    }

}
