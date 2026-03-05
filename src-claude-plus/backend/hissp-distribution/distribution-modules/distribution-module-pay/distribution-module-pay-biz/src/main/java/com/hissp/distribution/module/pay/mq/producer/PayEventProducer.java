package com.hissp.distribution.module.pay.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 支付模块事件消息生产者
 * 用于发送支付相关的业务消息到MQ
 *
 * @author system
 */
@Slf4j
@Component
public class PayEventProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // 注意：补货支付成功消息已统一使用 trade-order-events 主题
    // 各业务模块通过监听统一的支付成功消息，根据订单号前缀判断是否处理

}
