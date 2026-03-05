package com.hissp.distribution.module.trade.api.brokerage;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserCreateDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserCreateOrUpdateDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;

/**
 * 分销用户 API 接口
 */
public interface BrokerageUserApi {

    /**
     * 【管理员】创建分销用户
     *
     * @param createReqDTO 创建信息
     * @return 分销用户编号，同用户编号
     */
    Long createOrEnableBrokerageUser(BrokerageUserCreateDTO createReqDTO);

    /**
     * 获取用户的推广人
     *
     * @param userId 用户编号
     * @return 推广人
     */
    BrokerageUserRespDTO getBindBrokerageUser(Long userId);

    /**
     * 获取分销用户信息
     *
     * @param userId 用户编号
     * @return 分销用户
     */
    BrokerageUserRespDTO getBrokerageUser(Long userId);

    /**
     * 获取用户的团队leader
     *
     * @param userId 用户编号
     * @return 团队leader
     */
    BrokerageUserRespDTO getFirstBossBrokerageUser(Long userId);

    /**
     * 获取推广人的下级用户数量
     *
     * @param bindUserId 推广人编号
     * @param level 层级 1-一级 2-二级
     * @return 下级用户数量
     */
    Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level);

    /**
     * 获取 用户当前已经邀请的代理数量(不包含)
     *
     * @param userId 用户id
     * @param level 用户等级
     * @return Integer 数量
     */
    Integer countAgentUserByBindUserAndLevelId(Long userId, Long level);

    /**
     *  更新 开启关闭分销能力
     * @param userId
     * @param enable
     * @return
     */
    void updateBrokerageUserEnabled(Long userId, Boolean enable);

    /**
     * 更新分销用户等级
     * 
     * @param userId 用户编号
     * @param levelId 等级编号
     */
    void updateUserLevel(Long userId, Long levelId);

    void createOrUpdateBrokerageUser(BrokerageUserCreateOrUpdateDTO brokerageUserCreateOrUpdateDTO);

    /**
     * 计算当前用户在其邀请人处的排序（第几个被邀请）。
     * 上层无需关心数据来源，交由 trade 模块统一维护绑定时间与排序逻辑。
     *
     * @param userId 用户编号
     * @return 序号（从 1 开始），若无邀请关系则返回 null
     */
    Integer getInviteOrderIndex(Long userId);

}
