package com.hissp.distribution.module.member.mq.producer;

import com.hissp.distribution.module.mb.message.MemberLevelChangeMessage;
import com.hissp.distribution.module.mb.message.SubCompanyUpgradeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 会员模块事件消息生产者
 * 用于发送会员相关的业务消息到MQ
 *
 * @author system
 */
@Slf4j
@Component
public class MemberEventProducer {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    // Topic 常量定义
    private static final String SUB_COMPANY_UPGRADE_TOPIC = "sub_company_upgrade";
    private static final String MEMBER_LEVEL_CHANGE_TOPIC = "member_level_change";

    /**
     * 发送子公司升级消息
     *
     * @param message 消息内容
     */
    public void sendSubCompanyUpgradeMessage(SubCompanyUpgradeMessage message) {
        log.info("[sendSubCompanyUpgradeMessage][发送子公司升级消息: {}]", message);
        rocketMQTemplate.convertAndSend(SUB_COMPANY_UPGRADE_TOPIC, message);
    }

    /**
     * 发送会员等级变更消息
     *
     * @param message 消息内容
     */
    public void sendMemberLevelChangeMessage(MemberLevelChangeMessage message) {
        log.info("[sendMemberLevelChangeMessage][发送会员等级变更消息: {}]", message);
        rocketMQTemplate.convertAndSend(MEMBER_LEVEL_CHANGE_TOPIC, message);
    }

}
