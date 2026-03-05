package com.hissp.distribution.module.trade.service.brokerage;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.BrokerageUserCreateReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.BrokerageUserPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserChildSummaryPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserChildSummaryRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByUserCountRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageTeamMemberPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageTeamMemberPageRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageTeamOverviewRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.TeamGraphVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 分销用户 Service 接口
 *
 * @author owen
 */
public interface BrokerageUserService {

    /**
     * 获得分销用户
     *
     * @param id 编号
     * @return 分销用户
     */
    BrokerageUserDO getBrokerageUser(Long id);

    /**
     * 获得分销用户分页
     *
     * @param pageReqVO 分页查询
     * @return 分销用户分页
     */
    PageResult<BrokerageUserDO> getBrokerageUserPage(BrokerageUserPageReqVO pageReqVO);

    /**
     * 修改推广员编号
     *
     * @param id         用户编号
     * @param bindUserId 推广员编号
     */
    void updateBrokerageUserId(Long id, Long bindUserId);

    /**
     * 修改推广资格
     *
     * @param id      用户编号
     * @param enabled 推广资格
     */
    void updateBrokerageUserEnabled(Long id, Boolean enabled);

    /**
     * 修改用户等级
     *
     * @param id      用户编号
     * @param levelId 等级编号
     */
    void updateUserLevel(Long id, Long levelId);

    /**
     * 获得用户的推广人
     *
     * @param id 用户编号
     * @return 用户的推广人
     */
    BrokerageUserDO getBindBrokerageUser(Long id);

    /**
     * 获得或创建分销用户
     *
     * @param id 用户编号
     * @return 分销用户
     */
    BrokerageUserDO getOrCreateBrokerageUser(Long id);

    /**
     * 更新用户佣金
     *
     * @param id    用户编号
     * @param price 用户可用佣金
     * @return 更新结果
     */
    boolean updateUserPrice(Long id, Integer price);

    /**
     * 更新用户冻结佣金
     *
     * @param id          用户编号
     * @param frozenPrice 用户冻结佣金
     */
    void updateUserFrozenPrice(Long id, Integer frozenPrice);

    /**
     * 更新用户冻结佣金（减少），更新用户佣金（增加）
     *
     * @param id          用户编号
     * @param frozenPrice 减少冻结佣金（负数）
     */
    void updateFrozenPriceDecrAndPriceIncr(Long id, Integer frozenPrice);

    /**
     * 获得推广用户数量
     *
     * @param bindUserId 绑定的推广员编号
     * @param level      推广用户等级
     * @return 推广用户数量
     */
    Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level);

    /**
     * 【会员】绑定推广员
     *
     * @param userId     用户编号
     * @param bindUserId 推广员编号
     * @return 是否绑定
     */
    boolean bindBrokerageUser(@NotNull Long userId, @NotNull Long bindUserId);

    /**
     * 【管理员】创建分销用户
     *
     * @param createReqVO 请求
     * @return 编号
     */
    Long createBrokerageUser(@Valid BrokerageUserCreateReqVO createReqVO);


    /**
     * 【会员】创建分销用户并开启分销能力
     * @param createUserDO 创建分销用户
     * @return 分销用户编号
     */
    Long createOrEnableBrokerageUser(@Valid BrokerageUserDO createUserDO);

    /**
     * 获取用户是否有分销资格
     *
     * @param userId 用户编号
     * @return 是否有分销资格
     */
    Boolean getUserBrokerageEnabled(Long userId);

    /**
     * 获得推广人排行
     *
     * @param pageReqVO 分页查询
     * @return 推广人排行
     */
    PageResult<AppBrokerageUserRankByUserCountRespVO> getBrokerageUserRankPageByUserCount(AppBrokerageUserRankPageReqVO pageReqVO);

    /**
     * 获得下级分销统计分页
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 下级分销统计分页
     */
    PageResult<AppBrokerageUserChildSummaryRespVO> getBrokerageUserChildSummaryPage(AppBrokerageUserChildSummaryPageReqVO pageReqVO, Long userId);

    BrokerageUserDO getFirstBossBrokerageUser(Long userId);

    /**
     *  countAgentUserByBindUserAndLevel
     * @param userId userId
     * @param level level
     * @return  Ingeter 数量
     */
    Integer countAgentUserByBindUserAndLevelId(Long userId, Long level);
    
    /**
     * 获取指定等级的分销用户列表
     * 
     * @param levelId 等级编号
     * @return 分销用户列表
     */
    List<BrokerageUserDO> getBrokerageUserListByLevelId(Long levelId);
    
    /**
     * 获取团队成员列表
     *
     * @param pageReqVO 分页查询
     * @param userId    用户编号
     * @return 团队成员列表
     */
    AppBrokerageTeamMemberPageRespVO getTeamMemberList(AppBrokerageTeamMemberPageReqVO pageReqVO, Long userId);

    /**
     * 获取团队总览（统计、筛选项）
     *
     * @param userId 用户编号
     * @return 团队总览
     */
    AppBrokerageTeamOverviewRespVO getTeamOverview(Long userId);

    void createOrUpdateBrokerageUser(BrokerageUserDO brokerageUserDO);

    /**
     * 获取分销团队关系图谱
     *
     * @param userId 用户编号
     * @return 团队关系图谱
     */
    TeamGraphVO getTeamGraph(@NotNull Long userId);

    /**
     * 计算用户在其邀请人下的邀请顺序（第几个被邀请）
     *
     * @param userId 用户编号
     * @return 序号（从 1 开始），若无邀请人则返回 null
     */
    Integer getInviteOrderIndex(Long userId);
}
