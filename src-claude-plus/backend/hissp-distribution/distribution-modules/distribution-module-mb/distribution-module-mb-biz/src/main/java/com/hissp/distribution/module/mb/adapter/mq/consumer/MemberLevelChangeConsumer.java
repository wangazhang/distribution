package com.hissp.distribution.module.mb.adapter.mq.consumer;

import com.hissp.distribution.module.mb.domain.service.mbdt.MbBizOperationService;
import com.hissp.distribution.module.mb.message.MemberLevelChangeMessage;
import com.hissp.distribution.framework.idempotency.core.IdempotencyService;
import com.hissp.distribution.framework.idempotency.utils.IdempotencyUtils;
import com.hissp.distribution.module.mb.constants.MbIdempotencyConstants;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.member.event.MemberLevelChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 会员等级变更消息消费者
 * 处理手动的等级变更逻辑
 *
 * @author azhanga
 */
@Component
@Slf4j
@RocketMQMessageListener(
    topic = "member_level_change",
    consumerGroup = "member_level_change_consumer_group"
)
public class MemberLevelChangeConsumer implements RocketMQListener<MemberLevelChangeMessage> {

    public static final String CONSUMER_GROUP = "member_level_change_consumer_group";

    @Resource
    private MbBizOperationService mbDistributionService;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private MemberLevelApi memberLevelApi;

    @Resource
    private IdempotencyService idempotencyService;

    @Override
    public void onMessage(MemberLevelChangeMessage message) {
        // 生成消息ID和业务键
        String businessKey = IdempotencyUtils.generateTimestampBusinessKey(
            MbIdempotencyConstants.MEMBER_LEVEL_CHANGE_PREFIX,
            message.getUserId(), message.getOperateTime());
        String messageId = IdempotencyUtils.generateCustomMessageId(CONSUMER_GROUP, businessKey);

        log.info("[onMessage][收到会员等级变更消息: userId={}, messageId={}]",
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

            // 查询变更后的等级信息，确保等级数据完整性
            MemberLevelRespDTO afterLevelDTO = memberLevelApi.getMemberLevel(message.getAfterLevelId());
            if (afterLevelDTO == null) {
                log.error("[onMessage][等级不存在，levelId: {}]", message.getAfterLevelId());
                throw new IllegalArgumentException("等级不存在，levelId: " + message.getAfterLevelId());
            }

            // 查询变更前的等级信息（可能为空）
            MemberLevelRespDTO beforeLevelDTO = null;
            if (message.getBeforeLevelId() != null) {
                beforeLevelDTO = memberLevelApi.getMemberLevel(message.getBeforeLevelId());
            }

            log.info("[onMessage][查询到完整数据，userId: {}, 变更前等级: {}, 变更后等级: {}]",
                message.getUserId(),
                beforeLevelDTO != null ? beforeLevelDTO.getName() : "无",
                afterLevelDTO.getName());

            // 使用完整的数据创建事件，保持业务逻辑不变
            MemberLevelChangeEvent event = new MemberLevelChangeEvent(
                message.getUserId(),
                message.getBeforeLevelId(),
                message.getBeforeLevel(),
                message.getAfterLevelId(),
                message.getAfterLevel(),
                message.getManualEnableBrokerage(),
                message.getPackageId(),
                message.getOperatorId(),
                message.getOperateTime()
            );
            mbDistributionService.handleManualMemberLevelChange(event);

            // 标记处理成功
            idempotencyService.markSuccess(businessKey, CONSUMER_GROUP);
            log.info("[onMessage][处理会员等级变更消息完成: userId={}]", message.getUserId());
        } catch (Exception e) {
            // 标记处理失败
            String errorMsg = IdempotencyUtils.truncateErrorMsg(e.getMessage(), 500);
            idempotencyService.markFailed(messageId, CONSUMER_GROUP, errorMsg);
            log.error("[onMessage][处理会员等级变更消息失败: userId={}]", message.getUserId(), e);
            throw e; // 重新抛出异常，让RocketMQ进行重试
        }
    }

}
