package com.hissp.distribution.module.mb.adapter.mq.consumer;

import com.hissp.distribution.module.mb.domain.service.mbdt.MbBizOperationService;
import com.hissp.distribution.module.mb.message.SubCompanyUpgradeMessage;
import com.hissp.distribution.framework.idempotency.core.IdempotencyService;
import com.hissp.distribution.framework.idempotency.utils.IdempotencyUtils;
import com.hissp.distribution.module.mb.constants.MbIdempotencyConstants;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.member.event.SubCompanyUpgradeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 子公司升级消息消费者
 * 处理子公司升级相关的业务逻辑
 *
 * @author azhanga
 */
@Component
@Slf4j
@RocketMQMessageListener(
    topic = "sub_company_upgrade",
    consumerGroup = "sub_company_upgrade_consumer_group"
)
public class SubCompanyUpgradeConsumer implements RocketMQListener<SubCompanyUpgradeMessage> {

    public static final String CONSUMER_GROUP = "sub_company_upgrade_consumer_group";

    @Resource
    private MbBizOperationService mbDistributionService;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private IdempotencyService idempotencyService;

    @Override
    public void onMessage(SubCompanyUpgradeMessage message) {
        // 生成消息ID和业务键
        String businessKey = IdempotencyUtils.generateParameterizedBusinessKey(
            MbIdempotencyConstants.SUB_COMPANY_UPGRADE_PREFIX,
            message.getUserId(), message.getOperatorId(),
            message.getOperateTime().format(IdempotencyUtils.getDateTimeFormatter()));
        String messageId = IdempotencyUtils.generateCustomMessageId(CONSUMER_GROUP, businessKey);

        log.info("[onMessage][收到子公司升级消息: userId={}, messageId={}]",
            message.getUserId(), messageId);

        // 幂等性检查
        if (!idempotencyService.checkAndRecord(messageId, CONSUMER_GROUP, businessKey)) {
            log.info("[onMessage][消息已处理过，跳过处理: userId={}]", message.getUserId());
            return;
        }

        try {
            // 根据消息中的userId查询完整的用户数据，确保数据完整性
            MemberUserRespDTO userDTO = memberUserApi.getUser(message.getUserId());
            if (userDTO == null) {
                log.error("[onMessage][用户不存在，userId: {}]", message.getUserId());
                throw new IllegalArgumentException("用户不存在，userId: " + message.getUserId());
            }

            log.info("[onMessage][查询到完整用户数据，userId: {}, nickname: {}]",
                message.getUserId(), userDTO.getNickname());

            // 使用完整的数据创建事件，保持业务逻辑不变
            SubCompanyUpgradeEvent event = new SubCompanyUpgradeEvent(
                message.getUserId(),
                message.getBeforeLevel(),
                message.getOperatorId(),
                message.getOperateTime()
            );
            mbDistributionService.handleSubCompanyUpgradeListener(event);

            // 标记处理成功
            idempotencyService.markSuccess(messageId, CONSUMER_GROUP);
            log.info("[onMessage][处理子公司升级消息完成: userId={}]", message.getUserId());
        } catch (Exception e) {
            // 标记处理失败
            String errorMsg = IdempotencyUtils.truncateErrorMsg(e.getMessage(), 500);
            idempotencyService.markFailed(messageId, CONSUMER_GROUP, errorMsg);
            log.error("[onMessage][处理子公司升级消息失败: userId={}]", message.getUserId(), e);
            throw e; // 重新抛出异常，让RocketMQ进行重试
        }
    }

}
