package com.hissp.distribution.framework.idempotency.dal.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * MQ消息幂等性记录 DO
 * 用于记录已处理的消息，确保幂等性
 *
 * @author system
 */
@TableName(value = "mq_message_idempotency")
@Data
@EqualsAndHashCode(callSuper = true)
public class MqMessageIdempotencyDO extends BaseDO {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 消息唯一标识
     * RocketMQ的msgId或自定义的消息ID
     */
    private String messageId;

    /**
     * 消费者组名称
     */
    private String consumerGroup;

    /**
     * 主题名称
     */
    private String topic;

    /**
     * 业务唯一键
     * 用于业务级别的幂等性判断
     */
    private String businessKey;

    /**
     * 处理状态
     * 0-处理中, 1-成功, 2-失败
     */
    private Integer status;

    /**
     * 错误信息
     * 处理失败时记录错误详情
     */
    private String errorMsg;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 处理状态枚举
     */
    public static class Status {
        public static final int PROCESSING = 0;
        public static final int SUCCESS = 1;
        public static final int FAILED = 2;
    }
}
