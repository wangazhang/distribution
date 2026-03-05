package com.hissp.distribution.module.trade.service.brokerage;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertMapByFilter;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.date.LocalDateTimeUtils;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.framework.mybatis.core.util.MyBatisUtils;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.BrokerageUserCreateReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.BrokerageUserPageReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.TeamGraphVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.*;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageUserConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.dal.dataobject.config.TradeConfigDO;
import com.hissp.distribution.module.trade.dal.mysql.brokerage.BrokerageUserMapper;
import com.hissp.distribution.module.trade.dal.redis.RedisKeyConstants;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageBindModeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageEnabledConditionEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.service.config.TradeConfigService;

import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertMap;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.BROKERAGE_USER_NOT_EXISTS;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;

/**
 * 分销用户 Service 实现类
 *
 * @author owen
 */
@Service
@Validated
public class BrokerageUserServiceImpl implements BrokerageUserService {

    @Resource
    private BrokerageUserMapper brokerageUserMapper;

    @Resource
    private TradeConfigService tradeConfigService;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    @Lazy
    private MemberLevelApi memberLevelApi;

    @Override
    public BrokerageUserDO getBrokerageUser(Long id) {
        return brokerageUserMapper.selectById(id);
    }

    @Override
    public PageResult<BrokerageUserDO> getBrokerageUserPage(BrokerageUserPageReqVO pageReqVO) {
        List<Long> childIds = getChildUserIdsByLevel(pageReqVO.getBindUserId(), pageReqVO.getLevel());
        // 有"绑定用户编号"查询条件时，没有查到下级会员，直接返回空
        if (pageReqVO.getBindUserId() != null && CollUtil.isEmpty(childIds)) {
            return PageResult.empty();
        }
        return brokerageUserMapper.selectPage(pageReqVO, childIds);
    }

    @Override
    public void updateBrokerageUserId(Long id, Long bindUserId) {
        // 校验存在
        BrokerageUserDO brokerageUser = validateBrokerageUserExists(id);
        // 绑定关系未发生变化
        if (Objects.equals(brokerageUser.getBindUserId(), bindUserId)) {
            return;
        }

        // 情况一：清除推广员
        if (bindUserId == null) {
            // 清除推广员
            brokerageUserMapper.updateBindUserIdAndBindUserTimeToNull(id);
            return;
        }

        // 情况二：修改推广员
        validateCanBindUser(brokerageUser, bindUserId);
        brokerageUserMapper.updateById(fillBindUserData(bindUserId, new BrokerageUserDO().setId(id)));
    }

    @Override
    public void updateBrokerageUserEnabled(Long id, Boolean enabled) {
        // 校验存在
        validateBrokerageUserExists(id);
        if (BooleanUtil.isTrue(enabled)) {
            // 开通推广资格
            brokerageUserMapper.updateById(
                new BrokerageUserDO().setId(id).setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now()));
        } else {
            // 取消推广资格
            brokerageUserMapper.updateEnabledFalseAndBrokerageTimeToNull(id);
        }
    }

    @Override
    public void updateUserLevel(Long id, Long levelId) {
        // 校验存在
        validateBrokerageUserExists(id);
        // 更新等级
        brokerageUserMapper.updateLevel(id, levelId);
    }

    private BrokerageUserDO validateBrokerageUserExists(Long id) {
        BrokerageUserDO brokerageUserDO = brokerageUserMapper.selectById(id);
        if (brokerageUserDO == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }
        return brokerageUserDO;
    }

    @Override
    public BrokerageUserDO getBindBrokerageUser(Long id) {
        return Optional.ofNullable(id).map(this::getBrokerageUser).map(BrokerageUserDO::getBindUserId)
            .map(this::getBrokerageUser).orElse(null);
    }

    @Override
    public BrokerageUserDO getOrCreateBrokerageUser(Long id) {
        BrokerageUserDO brokerageUser = brokerageUserMapper.selectById(id);
        // 特殊：人人分销的情况下，如果分销人为空则创建分销人
        if (brokerageUser == null && ObjUtil.equal(BrokerageEnabledConditionEnum.ALL.getCondition(),
            tradeConfigService.getTradeConfig().getBrokerageEnabledCondition())) {
            brokerageUser = new BrokerageUserDO().setId(id).setBrokerageEnabled(true).setBrokeragePrice(0)
                .setBrokerageTime(LocalDateTime.now()).setFrozenPrice(0);
            brokerageUserMapper.insert(brokerageUser);
        }
        return brokerageUser;
    }

    @Override
    public boolean updateUserPrice(Long id, Integer price) {
        if (price > 0) {
            brokerageUserMapper.updatePriceIncr(id, price);
        } else if (price < 0) {
            return brokerageUserMapper.updatePriceDecr(id, price) > 0;
        }
        return true;
    }

    @Override
    public void updateUserFrozenPrice(Long id, Integer frozenPrice) {
        if (frozenPrice > 0) {
            brokerageUserMapper.updateFrozenPriceIncr(id, frozenPrice);
        } else if (frozenPrice < 0) {
            brokerageUserMapper.updateFrozenPriceDecr(id, frozenPrice);
        }
    }

    @Override
    public void updateFrozenPriceDecrAndPriceIncr(Long id, Integer frozenPrice) {
        Assert.isTrue(frozenPrice < 0);
        int updateRows = brokerageUserMapper.updateFrozenPriceDecrAndPriceIncr(id, frozenPrice);
        if (updateRows == 0) {
            throw exception(BROKERAGE_USER_FROZEN_PRICE_NOT_ENOUGH);
        }
    }

    @Override
    public Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level) {
        List<Long> childIds = getChildUserIdsByLevel(bindUserId, level);
        return (long)CollUtil.size(childIds);
    }

    @Override
    public boolean bindBrokerageUser(Long userId, Long bindUserId) {
        // 1. 获得分销用户
        boolean isNewBrokerageUser = false;
        BrokerageUserDO brokerageUser = brokerageUserMapper.selectById(userId);
        if (brokerageUser == null) { // 分销用户不存在的情况：1. 新注册；2. 旧数据；3. 分销功能关闭后又打开
            isNewBrokerageUser = true;
            brokerageUser =
                new BrokerageUserDO().setId(userId).setBrokerageEnabled(false).setBrokeragePrice(0).setFrozenPrice(0);
        }

        // 2.1 校验是否能绑定用户
        boolean validated = isUserCanBind(brokerageUser);
        if (!validated) {
            return false;
        }
        // 2.3 校验能否绑定
        validateCanBindUser(brokerageUser, bindUserId);
        // 2.3 绑定用户
        if (isNewBrokerageUser) {
            Integer enabledCondition = tradeConfigService.getTradeConfig().getBrokerageEnabledCondition();
            if (BrokerageEnabledConditionEnum.ALL.getCondition().equals(enabledCondition)) { // 人人分销：用户默认就有分销资格
                brokerageUser.setBrokerageEnabled(true).setBrokerageTime(LocalDateTime.now());
            } else {
                brokerageUser.setBrokerageEnabled(false).setBrokerageTime(LocalDateTime.now());
            }
            brokerageUserMapper.insert(fillBindUserData(bindUserId, brokerageUser));
        } else {
            brokerageUserMapper.updateById(fillBindUserData(bindUserId, new BrokerageUserDO().setId(userId)));
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrokerageUser(BrokerageUserCreateReqVO createReqVO) {
        // 1.1 校验分销用户是否已存在
        BrokerageUserDO brokerageUser = brokerageUserMapper.selectById(createReqVO.getUserId());
        if (brokerageUser != null) {
            throw exception(BROKERAGE_CREATE_USER_EXISTS);
        }
        // 1.2 校验是否能绑定用户
        brokerageUser = BeanUtils.toBean(createReqVO, BrokerageUserDO.class).setId(createReqVO.getUserId())
            .setBrokerageTime(LocalDateTime.now());
        // validateCanBindUser(brokerageUser, createReqVO.getBindUserId());

        // 2. 创建分销人
        brokerageUserMapper.insert(brokerageUser);
        return brokerageUser.getId();
    }

    @Override
    public Long createOrEnableBrokerageUser(BrokerageUserDO createUserDO) {
        // 1.1 校验分销用户是否已存在
        createUserDO.setBrokerageTime(LocalDateTime.now());
        // 2. 创建or 更新分销人
        brokerageUserMapper.insertOrUpdate(createUserDO);
        return createUserDO.getId();
    }

    /**
     * 补全绑定用户的字段
     *
     * @param bindUserId 绑定的用户编号
     * @param brokerageUser update 对象
     * @return 补全后的 update 对象
     */
    private BrokerageUserDO fillBindUserData(Long bindUserId, BrokerageUserDO brokerageUser) {
        return brokerageUser.setBindUserId(bindUserId).setBindUserTime(LocalDateTime.now());
    }

    @Override
    public Boolean getUserBrokerageEnabled(Long userId) {
        // 全局分销功能是否开启
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || BooleanUtil.isFalse(tradeConfig.getBrokerageEnabled())) {
            return false;
        }

        // 用户是否有分销资格
        return Optional.ofNullable(getBrokerageUser(userId)).map(BrokerageUserDO::getBrokerageEnabled).orElse(false);
    }

    @Override
    public PageResult<AppBrokerageUserRankByUserCountRespVO>
        getBrokerageUserRankPageByUserCount(AppBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppBrokerageUserRankByUserCountRespVO> pageResult =
            brokerageUserMapper.selectCountPageGroupByBindUserId(MyBatisUtils.buildPage(pageReqVO),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public PageResult<AppBrokerageUserChildSummaryRespVO>
        getBrokerageUserChildSummaryPage(AppBrokerageUserChildSummaryPageReqVO pageReqVO, Long userId) {
        // 1.1 查询下级用户编号列表
        List<Long> childIds = getChildUserIdsByLevel(userId, pageReqVO.getLevel());
        if (CollUtil.isEmpty(childIds)) {
            return PageResult.empty();
        }
        // 1.2 根据昵称过滤下级用户
        List<MemberUserRespDTO> users = memberUserApi.getUserList(childIds);
        Map<Long, MemberUserRespDTO> userMap = convertMapByFilter(users,
            user -> StrUtil.contains(user.getNickname(), pageReqVO.getNickname()), MemberUserRespDTO::getId);
        if (CollUtil.isEmpty(userMap)) {
            return PageResult.empty();
        }

        // 2. 分页查询
        IPage<AppBrokerageUserChildSummaryRespVO> pageResult = brokerageUserMapper.selectSummaryPageByUserId(
            MyBatisUtils.buildPage(pageReqVO), BrokerageRecordBizTypeEnum.ORDER_ADD.getType(),
            BrokerageRecordStatusEnum.SETTLEMENT.getStatus(), userMap.keySet(), pageReqVO.getSortingField());

        // 3. 拼接数据并返回
        BrokerageUserConvert.INSTANCE.copyTo(pageResult.getRecords(), userMap);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public BrokerageUserDO getFirstBossBrokerageUser(Long userId) {
        Integer firstBossBrokerageUserId = brokerageUserMapper.getFirstBossBrokerageUser(userId);
        if (null == firstBossBrokerageUserId) {
            return null;
        }
        return brokerageUserMapper.selectById(firstBossBrokerageUserId);
    }

    private boolean isUserCanBind(BrokerageUserDO user) {
        // 校验分销功能是否启用
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || !BooleanUtil.isTrue(tradeConfig.getBrokerageEnabled())) {
            return false;
        }

        // 校验分销关系绑定模式
        if (BrokerageBindModeEnum.REGISTER.getMode().equals(tradeConfig.getBrokerageBindMode())) {
            // 判断是否为新用户：注册时间在 30 秒内的，都算新用户
            if (!isNewRegisterUser(user.getId())) {
                throw exception(BROKERAGE_BIND_MODE_REGISTER); // 只有在注册时可以绑定
            }
        } else if (BrokerageBindModeEnum.ANYTIME.getMode().equals(tradeConfig.getBrokerageBindMode())) {
            if (user.getBindUserId() != null) {
                throw exception(BROKERAGE_BIND_OVERRIDE); // 已绑定了推广人
            }
        }
        return true;
    }

    /**
     * 判断是否为新用户
     * <p>
     * 标准：注册时间在 30 秒内的，都算新用户
     * <p>
     * 疑问：为什么通过这样的方式实现？ 回答：因为注册在 member 模块，希望它和 trade 模块解耦，所以只能用这种约定的逻辑。
     *
     * @param userId 用户编号
     * @return 是否新用户
     */
    private boolean isNewRegisterUser(Long userId) {
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        return user != null && LocalDateTimeUtils.afterNow(user.getCreateTime().plusSeconds(30));
    }

    private void validateCanBindUser(BrokerageUserDO user, Long bindUserId) {
        // 1.1 校验推广人是否存在
        MemberUserRespDTO bindUserInfo = memberUserApi.getUser(bindUserId);
        if (bindUserInfo == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }
        // 1.2 校验要绑定的用户有无推广资格
        BrokerageUserDO bindUser = getOrCreateBrokerageUser(bindUserId);
        if (bindUser == null || BooleanUtil.isFalse(bindUser.getBrokerageEnabled())) {
            throw exception(BROKERAGE_BIND_USER_NOT_ENABLED);
        }

        // 2. 校验绑定自己
        if (Objects.equals(user.getId(), bindUserId)) {
            throw exception(BROKERAGE_BIND_SELF);
        }

        // 3. 下级不能绑定自己的上级
        for (int i = 0; i <= Short.MAX_VALUE; i++) {
            if (Objects.equals(bindUser.getBindUserId(), user.getId())) {
                throw exception(BROKERAGE_BIND_LOOP);
            }
            bindUser = getBrokerageUser(bindUser.getBindUserId());
            // 找到根节点，结束循环
            if (bindUser == null || bindUser.getBindUserId() == null) {
                break;
            }
        }
    }

    /**
     * 根据绑定用户编号，获得下级用户编号列表
     *
     * @param bindUserId 绑定用户编号
     * @param level 下级用户的层级。 如果 level 为空，则查询 1+2 两个层级
     * @return 下级用户编号列表
     */
    private List<Long> getChildUserIdsByLevel(Long bindUserId, Integer level) {
        if (bindUserId == null) {
            return Collections.emptyList();
        }
        // 先查第 1 级
        List<Long> bindUserIds = brokerageUserMapper.selectIdListByBindUserIdIn(Collections.singleton(bindUserId));
        if (CollUtil.isEmpty(bindUserIds)) {
            return Collections.emptyList();
        }

        // 情况一：level 为空，查询所有级别
        if (level == null) {
            // 再查第 2 级，并合并结果
            bindUserIds.addAll(brokerageUserMapper.selectIdListByBindUserIdIn(bindUserIds));
            return bindUserIds;
        }
        // 情况二：level 为 1，只查询第 1 级
        if (level == 1) {
            return bindUserIds;
        }
        // 情况三：level 为 1，只查询第 2 级
        if (level == 2) {
            return brokerageUserMapper.selectIdListByBindUserIdIn(bindUserIds);
        }
        throw exception(BROKERAGE_USER_LEVEL_NOT_SUPPORT);
    }
    @Override
    public Integer countAgentUserByBindUserAndLevelId(Long userId, Long levelId) {
        return brokerageUserMapper.countAgentUserByBindUserAndLevel(userId, levelId);
    }

    @Override
    public List<BrokerageUserDO> getBrokerageUserListByLevelId(Long levelId) {
        return brokerageUserMapper.selectListByLevelId(levelId);
    }

    @Override
    @Cacheable(cacheNames = RedisKeyConstants.BROKERAGE_TEAM_MEMBER_PAGE + "#5m",
            key = "T(com.hissp.distribution.module.trade.service.brokerage.BrokerageUserServiceImpl)"
                    + ".buildTeamMemberCacheKey(#userId, #pageReqVO)")
    public AppBrokerageTeamMemberPageRespVO getTeamMemberList(AppBrokerageTeamMemberPageReqVO pageReqVO, Long userId) {
        // 1. 创建返回结果对象
        AppBrokerageTeamMemberPageRespVO result = new AppBrokerageTeamMemberPageRespVO();
        
        // 2. 根据类型查询不同的成员列表
        String type = pageReqVO.getType();
        List<BrokerageUserDO> memberList = new ArrayList<>();
        
        // 3. 查询统计数据
        AppBrokerageTeamStatsRespVO stats = new AppBrokerageTeamStatsRespVO();
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        
        // 4. 根据不同类型查询不同的数据
        if ("community".equals(type)) {
            // 查询社群成员 - 所有下级用户
            memberList = getCommunityMembers(userId, pageReqVO);
            
            // 统计总数、今日新增、本月新增
            stats.setTotal(brokerageUserMapper.selectCommunityMemberCountByUserId(userId, null, pageReqVO.getNickname()).intValue());
            stats.setTodayNew(brokerageUserMapper.countByBindUserIdTreeAndCreateTimeGte(userId, todayStart).intValue());
            stats.setMonthNew(brokerageUserMapper.countByBindUserIdTreeAndCreateTimeGte(userId, monthStart).intValue());
        } else if ("direct".equals(type)) {
            // 查询直接代理 - 直接下级且有分销资格的用户
            memberList = getDirectAgentMembers(userId, pageReqVO);
            
            // 统计总数、今日新增、本月新增
            stats.setTotal(brokerageUserMapper.selectDirectAgentMemberCountByUserId(userId, pageReqVO.getLevel(), pageReqVO.getNickname()).intValue());
            stats.setTodayNew(brokerageUserMapper.countByBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, todayStart, pageReqVO.getLevel()).intValue());
            stats.setMonthNew(brokerageUserMapper.countByBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, monthStart, pageReqVO.getLevel()).intValue());
        } else if ("indirect".equals(type)) {
            // 查询间接代理 - 间接下级且有分销资格的用户
            memberList = getIndirectAgentMembers(userId, pageReqVO);
            
            // 统计总数、今日新增、本月新增
            stats.setTotal(brokerageUserMapper.selectIndirectAgentMemberCountByUserId(userId, pageReqVO.getLevel(), pageReqVO.getNickname()).intValue());
            stats.setTodayNew(brokerageUserMapper.countByIndirectBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, todayStart, pageReqVO.getLevel()).intValue());
            stats.setMonthNew(brokerageUserMapper.countByIndirectBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, monthStart, pageReqVO.getLevel()).intValue());
        }
        
        // 5. 转换为VO对象
        List<AppBrokerageTeamMemberRespVO> memberRespList = new ArrayList<>();
        if (!CollUtil.isEmpty(memberList)) {
            // 用户信息
            Set<Long> memberSet = convertSet(memberList, BrokerageUserDO::getId);
            //邀请者信息
            memberSet.addAll(convertSet(memberList, BrokerageUserDO::getBindUserId));
            //获取这些成员的用户情况
            Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                    memberSet);

            // 获取等级信息
            Set<Long> levelIds = userMap.values().stream()
                    .filter(Objects::nonNull)
                    .map(MemberUserRespDTO::getLevelId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, MemberLevelRespDTO> levelMap = CollUtil.isEmpty(levelIds)
                    ? Collections.emptyMap()
                    : memberLevelApi.getMemberLevelMap(levelIds);
            for (BrokerageUserDO member : memberList) {
                AppBrokerageTeamMemberRespVO memberResp = new AppBrokerageTeamMemberRespVO();
                memberResp.setId(member.getId());
                
                // 设置用户信息
                MemberUserRespDTO user = userMap.get(member.getId());
                if (user != null) {
                    memberResp.setNickname(user.getNickname());
                    memberResp.setAvatar(user.getAvatar());
                    memberResp.setPhone(user.getMobile());
                    memberResp.setRegisterTime(user.getCreateTime());
                    Long levelId = user.getLevelId();
                    memberResp.setLevelId(levelId);
                    if (levelId != null) {
                        MemberLevelRespDTO level = levelMap.get(levelId);
                        memberResp.setLevelName(level != null ? level.getName() : null);
                    }
                }
                
                // 设置邀请人信息
                if (member.getBindUserId() != null) {
                    MemberUserRespDTO inviter = userMap.get(member.getBindUserId());
                    if (inviter != null) {
                        memberResp.setInviter(inviter.getNickname());
                    }
                }
                
                // 设置消费额和佣金收益
                // 这里需要调用其他服务获取用户消费额和佣金收益，简化处理
                memberResp.setConsumption(0); // 实际项目中应该从订单服务获取
                memberResp.setRevenue(member.getBrokeragePrice());
                
                // 设置社群人数
                memberResp.setCommunityCount(brokerageUserMapper.selectCommunityMemberCountByUserId(member.getId(), null, null).intValue());
                
                memberRespList.add(memberResp);
            }
        }
        
        // 6. 设置返回结果
        result.setStats(stats);
        result.setList(memberRespList);
        
        return result;
    }

    @Override
    @Cacheable(cacheNames = RedisKeyConstants.BROKERAGE_TEAM_OVERVIEW + "#5m", key = "#userId")
    public AppBrokerageTeamOverviewRespVO getTeamOverview(Long userId) {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);

        AppBrokerageTeamOverviewRespVO overview = new AppBrokerageTeamOverviewRespVO();
        overview.setCommunityStats(buildCommunityStats(userId, todayStart, monthStart));
        overview.setDirectStats(buildDirectStats(userId, todayStart, monthStart));
        overview.setIndirectStats(buildIndirectStats(userId, todayStart, monthStart));
        overview.setLevelOptions(buildLevelOptions());
        return overview;
    }

    @Override
    public void createOrUpdateBrokerageUser(BrokerageUserDO brokerageUserDO) {
        // 2. 创建or 更新分销人信息
        brokerageUserMapper.insertOrUpdate(brokerageUserDO);
    }

    private AppBrokerageTeamStatsRespVO buildCommunityStats(Long userId, LocalDateTime todayStart, LocalDateTime monthStart) {
        AppBrokerageTeamStatsRespVO stats = new AppBrokerageTeamStatsRespVO();
        stats.setTotal(brokerageUserMapper.selectCommunityMemberCountByUserId(userId, null, null).intValue());
        stats.setTodayNew(brokerageUserMapper.countByBindUserIdTreeAndCreateTimeGte(userId, todayStart).intValue());
        stats.setMonthNew(brokerageUserMapper.countByBindUserIdTreeAndCreateTimeGte(userId, monthStart).intValue());
        return stats;
    }

    private AppBrokerageTeamStatsRespVO buildDirectStats(Long userId, LocalDateTime todayStart, LocalDateTime monthStart) {
        AppBrokerageTeamStatsRespVO stats = new AppBrokerageTeamStatsRespVO();
        stats.setTotal(brokerageUserMapper.countByBindUserIdAndBrokerageEnabled(userId, Boolean.TRUE).intValue());
        stats.setTodayNew(brokerageUserMapper.countByBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, todayStart, null).intValue());
        stats.setMonthNew(brokerageUserMapper.countByBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, monthStart, null).intValue());
        return stats;
    }

    private AppBrokerageTeamStatsRespVO buildIndirectStats(Long userId, LocalDateTime todayStart, LocalDateTime monthStart) {
        AppBrokerageTeamStatsRespVO stats = new AppBrokerageTeamStatsRespVO();
        stats.setTotal(brokerageUserMapper.countByIndirectBindUserIdAndBrokerageEnabled(userId, Boolean.TRUE).intValue());
        stats.setTodayNew(brokerageUserMapper.countByIndirectBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, todayStart, null).intValue());
        stats.setMonthNew(brokerageUserMapper.countByIndirectBindUserIdAndBrokerageEnabledAndCreateTimeGte(userId, Boolean.TRUE, monthStart, null).intValue());
        return stats;
    }

    private List<AppBrokerageTeamLevelOptionRespVO> buildLevelOptions() {
        List<AppBrokerageTeamLevelOptionRespVO> options = new ArrayList<>();
        AppBrokerageTeamLevelOptionRespVO allOption = new AppBrokerageTeamLevelOptionRespVO();
        allOption.setId(0L);
        allOption.setName("全部");
        options.add(allOption);

        List<MemberLevelRespDTO> levels = memberLevelApi.getEnableMemberLevelList();
        if (CollUtil.isNotEmpty(levels)) {
            for (MemberLevelRespDTO level : levels) {
                AppBrokerageTeamLevelOptionRespVO option = new AppBrokerageTeamLevelOptionRespVO();
                option.setId(level.getId());
                option.setName(level.getName());
                options.add(option);
            }
        }
        return options;
    }

    /**
     * 获取社群成员列表 - 所有下级用户（使用级联查询）
     */
    private List<BrokerageUserDO> getCommunityMembers(Long userId, AppBrokerageTeamMemberPageReqVO pageReqVO) {
        // 计算分页偏移量
        int offset = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
        
        // 使用级联查询获取所有下级用户
        return brokerageUserMapper.selectCommunityMembersByUserId(
            userId,
            pageReqVO.getLevel(),
            pageReqVO.getNickname(),
            pageReqVO.getPageSize(),
            offset
        );
    }

    /**
     * 获取直接代理成员列表 - 直接下级且有分销资格的用户
     */
    private List<BrokerageUserDO> getDirectAgentMembers(Long userId, AppBrokerageTeamMemberPageReqVO pageReqVO) {
        // 计算分页偏移量
        int offset = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();
        
        // 使用直接查询获取直接代理成员
        return brokerageUserMapper.selectDirectAgentMembersByUserId(
            userId,
            pageReqVO.getLevel(),
            pageReqVO.getNickname(),
            pageReqVO.getPageSize(),
            offset
        );
    }

    /**
     * 获取间接代理成员列表 - 间接下级且有分销资格的用户
     */
    private List<BrokerageUserDO> getIndirectAgentMembers(Long userId, AppBrokerageTeamMemberPageReqVO pageReqVO) {
        // 计算分页偏移量
        int offset = (pageReqVO.getPageNo() - 1) * pageReqVO.getPageSize();

        // 使用级联查询获取间接代理成员
        return brokerageUserMapper.selectIndirectAgentMembersByUserId(
            userId,
            pageReqVO.getLevel(),
            pageReqVO.getNickname(),
            pageReqVO.getPageSize(),
            offset
        );
    }

    @Override
    public TeamGraphVO getTeamGraph(Long userId) {
        // 1. 校验用户是否存在
        BrokerageUserDO currentUser = brokerageUserMapper.selectById(userId);
        if (currentUser == null) {
            throw exception(BROKERAGE_USER_NOT_EXISTS);
        }

        // 2. 向上查找分公司或总公司级别的根用户
        BrokerageUserDO rootUser = brokerageUserMapper.selectCompanyLevelAncestor(userId);
        if (rootUser == null) {
            // 若不存在公司级上级，则回溯到链路最顶层的用户
            rootUser = brokerageUserMapper.selectTopAncestor(userId);
        }
        if (rootUser == null) {
            // 兜底：至少返回当前用户的团队
            rootUser = currentUser;
        }

        // 3. 获取团队所有成员
        List<BrokerageUserDO> teamMembers = brokerageUserMapper.selectTeamMembersByRootUserId(rootUser.getId());
        if (CollUtil.isEmpty(teamMembers)) {
            // 如果没有团队成员，返回只有根节点的图谱
            return buildSingleNodeTeamGraph(rootUser);
        }

        // 4. 批量获取用户信息和等级信息
        Set<Long> userIds = convertSet(teamMembers, BrokerageUserDO::getId);
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);

        // 从MemberUserRespDTO中获取levelId（不使用BrokerageUserDO中的levelId，因为它是错误的）
        Set<Long> levelIds = teamMembers.stream()
                .map(user -> {
                    MemberUserRespDTO memberUser = userMap.get(user.getId());
                    return memberUser != null ? memberUser.getLevelId() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, MemberLevelRespDTO> levelMap = CollUtil.isEmpty(levelIds) ?
                Collections.emptyMap() : memberLevelApi.getMemberLevelMap(levelIds);

        // 5. 批量获取直接下级人数
        Set<Long> allUserIds = convertSet(teamMembers, BrokerageUserDO::getId);
        List<Map<String, Object>> childCountResults = brokerageUserMapper.selectDirectChildrenCountBatch(allUserIds);
        Map<Long, Integer> childCountMap = childCountResults.stream()
                .collect(Collectors.toMap(
                        result -> Long.valueOf(result.get("userId").toString()),
                        result -> Integer.valueOf(result.get("childCount").toString())
                ));

        // 6. 构建团队树结构
        TeamGraphVO.TeamGraphNodeVO rootNode = buildTeamTree(teamMembers, userMap, levelMap, childCountMap, rootUser.getId());

        // 7. 计算统计信息
        TeamGraphVO teamGraph = new TeamGraphVO();
        teamGraph.setRootNode(rootNode);
        teamGraph.setTotalCount(teamMembers.size());
        teamGraph.setLevelCount(calculateMaxDepth(rootNode));
        teamGraph.setTotalCommission(calculateTotalCommission(teamMembers));

        return teamGraph;
    }

    @Override
    /**
     * 计算用户在其邀请链中的排名：依赖绑定时间（或创建时间）与推广人 ID。
     */
    public Integer getInviteOrderIndex(Long userId) {
        if (userId == null) {
            return null;
        }
        BrokerageUserDO self = brokerageUserMapper.selectById(userId);
        if (self == null || self.getBindUserId() == null) {
            return null;
        }
        LocalDateTime referenceTime = self.getBindUserTime();
        if (referenceTime == null) {
            referenceTime = self.getCreateTime();
        }
        if (referenceTime == null) {
            referenceTime = LocalDateTime.of(1970, 1, 1, 0, 0);
        }
        Integer order = brokerageUserMapper.selectInviteOrderIndex(self.getBindUserId(), referenceTime, self.getId());
        return order != null && order > 0 ? order : null;
    }

    public static String buildTeamMemberCacheKey(Long userId, AppBrokerageTeamMemberPageReqVO pageReqVO) {
        return StrUtil.join(":", userId,
                StrUtil.blankToDefault(pageReqVO.getType(), ""),
                pageReqVO.getLevel() == null ? "" : pageReqVO.getLevel(),
                StrUtil.blankToDefault(pageReqVO.getNickname(), ""),
                pageReqVO.getPageNo(),
                pageReqVO.getPageSize());
    }

    /**
     * 构建只有单个节点的团队图谱
     */
    private TeamGraphVO buildSingleNodeTeamGraph(BrokerageUserDO user) {
        MemberUserRespDTO memberUser = memberUserApi.getUser(user.getId());
        String levelName = "游客"; // 默认等级

        // 从MemberUserRespDTO中获取levelId（不使用BrokerageUserDO中的levelId，因为它是错误的）
        Long levelId = null;
        if (memberUser != null) {
            levelId = memberUser.getLevelId();
        }

        if (levelId != null) {
            MemberLevelRespDTO level = memberLevelApi.getMemberLevel(levelId);
            if (level != null) {
                levelName = level.getName();
            }
        }

        TeamGraphVO.TeamGraphNodeVO rootNode = new TeamGraphVO.TeamGraphNodeVO();
        rootNode.setId("user_" + user.getId());
        rootNode.setNickname(memberUser != null && memberUser.getNickname() != null ?
                memberUser.getNickname() : "用户" + user.getId());
        rootNode.setAvatar(memberUser != null ? memberUser.getAvatar() : null);
        rootNode.setLevelName(levelName);
        rootNode.setTotalCommission(user.getTotalBrokeragePrice() != null ?
                new BigDecimal(user.getTotalBrokeragePrice()).divide(new BigDecimal(100)) : BigDecimal.ZERO);
        rootNode.setChildCount(0);
        rootNode.setCreateTime(user.getCreateTime() != null ?
                user.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        rootNode.setChildren(Collections.emptyList());

        TeamGraphVO teamGraph = new TeamGraphVO();
        teamGraph.setRootNode(rootNode);
        teamGraph.setTotalCount(1);
        teamGraph.setLevelCount(1);
        teamGraph.setTotalCommission(user.getTotalBrokeragePrice() != null ?
                new BigDecimal(user.getTotalBrokeragePrice()).divide(new BigDecimal(100)) : BigDecimal.ZERO);

        return teamGraph;
    }

    /**
     * 构建团队树结构
     */
    private TeamGraphVO.TeamGraphNodeVO buildTeamTree(List<BrokerageUserDO> teamMembers,
                                                      Map<Long, MemberUserRespDTO> userMap,
                                                      Map<Long, MemberLevelRespDTO> levelMap,
                                                      Map<Long, Integer> childCountMap,
                                                      Long rootUserId) {
        // 构建父子关系映射
        Map<Long, List<BrokerageUserDO>> childrenMap = teamMembers.stream()
                .filter(user -> user.getBindUserId() != null)
                .collect(Collectors.groupingBy(BrokerageUserDO::getBindUserId));

        // 构建用户映射
        Map<Long, BrokerageUserDO> userDoMap = convertMap(teamMembers, BrokerageUserDO::getId);

        // 递归构建树
        return buildNodeRecursively(rootUserId, userDoMap, childrenMap, userMap, levelMap, childCountMap);
    }

    /**
     * 递归构建节点
     */
    private TeamGraphVO.TeamGraphNodeVO buildNodeRecursively(Long userId,
                                                             Map<Long, BrokerageUserDO> userDoMap,
                                                             Map<Long, List<BrokerageUserDO>> childrenMap,
                                                             Map<Long, MemberUserRespDTO> userMap,
                                                             Map<Long, MemberLevelRespDTO> levelMap,
                                                             Map<Long, Integer> childCountMap) {
        BrokerageUserDO userDO = userDoMap.get(userId);
        if (userDO == null) {
            return null;
        }

        MemberUserRespDTO memberUser = userMap.get(userId);
        String levelName = "游客"; // 默认等级

        // 从MemberUserRespDTO中获取levelId（不使用BrokerageUserDO中的levelId，因为它是错误的）
        Long levelId = null;
        if (memberUser != null) {
            levelId = memberUser.getLevelId();
        }

        if (levelId != null) {
            MemberLevelRespDTO level = levelMap.get(levelId);
            if (level != null) {
                levelName = level.getName();
            }
        }

        TeamGraphVO.TeamGraphNodeVO node = new TeamGraphVO.TeamGraphNodeVO();
        node.setId("user_" + userId);
        node.setNickname(memberUser != null && memberUser.getNickname() != null ?
                memberUser.getNickname() : "用户" + userId);
        node.setAvatar(memberUser != null ? memberUser.getAvatar() : null);
        node.setLevelName(levelName);
        node.setTotalCommission(userDO.getTotalBrokeragePrice() != null ?
                new BigDecimal(userDO.getTotalBrokeragePrice()).divide(new BigDecimal(100)) : BigDecimal.ZERO);
        node.setCreateTime(userDO.getCreateTime() != null ?
                userDO.getCreateTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        // 构建子节点
        List<BrokerageUserDO> children = childrenMap.getOrDefault(userId, Collections.emptyList());

        // 使用批量查询的结果获取直接下级人数
        Integer actualChildCount = childCountMap.getOrDefault(userId, 0);
        node.setChildCount(actualChildCount);

        List<TeamGraphVO.TeamGraphNodeVO> childNodes = children.stream()
                .map(child -> buildNodeRecursively(child.getId(), userDoMap, childrenMap, userMap, levelMap, childCountMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        node.setChildren(childNodes);

        return node;
    }

    /**
     * 计算最大深度
     */
    private Integer calculateMaxDepth(TeamGraphVO.TeamGraphNodeVO node) {
        if (node == null || CollUtil.isEmpty(node.getChildren())) {
            return 1;
        }

        int maxChildDepth = node.getChildren().stream()
                .mapToInt(this::calculateMaxDepth)
                .max()
                .orElse(0);

        return maxChildDepth + 1;
    }

    /**
     * 计算总佣金
     */
    private BigDecimal calculateTotalCommission(List<BrokerageUserDO> teamMembers) {
        return teamMembers.stream()
                .filter(user -> user.getTotalBrokeragePrice() != null)
                .map(user -> new BigDecimal(user.getTotalBrokeragePrice()).divide(new BigDecimal(100)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
