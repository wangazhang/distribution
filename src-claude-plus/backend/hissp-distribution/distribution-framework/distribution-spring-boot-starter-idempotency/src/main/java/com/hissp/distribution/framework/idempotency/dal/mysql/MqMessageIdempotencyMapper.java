package com.hissp.distribution.framework.idempotency.dal.mysql;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hissp.distribution.framework.idempotency.dal.dataobject.MqMessageIdempotencyDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * MQ消息幂等性记录 Mapper
 *
 * @author system
 */
@Mapper
public interface MqMessageIdempotencyMapper extends BaseMapper<MqMessageIdempotencyDO> {

    /**
     * 根据消息ID和消费者组查询记录
     *
     * @param messageId 消息ID
     * @param consumerGroup 消费者组
     * @return 幂等性记录
     */
    MqMessageIdempotencyDO selectByMessageIdAndConsumerGroup(@Param("messageId") String messageId, 
                                                             @Param("consumerGroup") String consumerGroup);

    /**
     * 根据业务键和消费者组查询记录
     *
     * @param businessKey 业务键
     * @param consumerGroup 消费者组
     * @return 幂等性记录
     */
    MqMessageIdempotencyDO selectByBusinessKeyAndConsumerGroup(@Param("businessKey") String businessKey, 
                                                               @Param("consumerGroup") String consumerGroup);

    /**
     * 更新处理状态
     *
     * @param messageId 消息ID
     * @param consumerGroup 消费者组
     * @param status 状态
     * @param errorMsg 错误信息
     * @return 更新行数
     */
    int updateStatus(@Param("messageId") String messageId, 
                     @Param("consumerGroup") String consumerGroup,
                     @Param("status") Integer status, 
                     @Param("errorMsg") String errorMsg);

    /**
     * 删除过期记录
     *
     * @param expireTime 过期时间
     * @return 删除行数
     */
    int deleteExpiredRecords(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 增加重试次数
     *
     * @param messageId 消息ID
     * @param consumerGroup 消费者组
     * @return 更新行数
     */
    int incrementRetryCount(@Param("messageId") String messageId, 
                           @Param("consumerGroup") String consumerGroup);
}
