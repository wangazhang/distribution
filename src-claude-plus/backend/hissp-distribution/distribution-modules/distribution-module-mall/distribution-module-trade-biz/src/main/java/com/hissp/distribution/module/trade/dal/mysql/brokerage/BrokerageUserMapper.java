package com.hissp.distribution.module.trade.dal.mysql.brokerage;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Assert;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.pojo.SortingField;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.user.BrokerageUserPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserChildSummaryRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByUserCountRespVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 分销用户 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageUserMapper extends BaseMapperX<BrokerageUserDO> {

    default PageResult<BrokerageUserDO> selectPage(BrokerageUserPageReqVO reqVO, List<Long> ids) {
        return selectPage(reqVO, new LambdaQueryWrapperX<BrokerageUserDO>()
                .inIfPresent(BrokerageUserDO::getId, ids)
                .eqIfPresent(BrokerageUserDO::getId, reqVO.getUserId()) // 添加按用户ID查询
                .eqIfPresent(BrokerageUserDO::getBrokerageEnabled, reqVO.getBrokerageEnabled())
                .eqIfPresent(BrokerageUserDO::getLevelId, reqVO.getLevelId())
                .betweenIfPresent(BrokerageUserDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(BrokerageUserDO::getBindUserTime, reqVO.getBindUserTime())
                .orderByDesc(BrokerageUserDO::getId));
    }

    /**
     * 更新用户可用佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（正数）
     */
    default void updatePriceIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" brokerage_price = brokerage_price + " + incrCount)
                .setSql(" total_brokerage_price = total_brokerage_price + " + incrCount)
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户可用佣金（减少）
     * 注意：理论上佣金可能已经提现，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 增加佣金（负数）
     * @return 更新行数
     */
    default int updatePriceDecr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" brokerage_price = brokerage_price + " + incrCount) // 负数，所以使用 + 号
                .eq(BrokerageUserDO::getId, id);
        return update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 增加冻结佣金（正数）
     */
    default void updateFrozenPriceIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount > 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" frozen_price = frozen_price + " + incrCount)
                .setSql(" total_brokerage_price = total_brokerage_price + " + incrCount)
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（减少）
     * 注意：理论上冻结佣金可能已经解冻，这时会扣出负数，确保平台不会造成损失
     *
     * @param id        用户编号
     * @param incrCount 减少冻结佣金（负数）
     */
    default void updateFrozenPriceDecr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" frozen_price = frozen_price + " + incrCount) // 负数，所以使用 + 号
                .eq(BrokerageUserDO::getId, id);
        update(null, lambdaUpdateWrapper);
    }

    /**
     * 更新用户冻结佣金（减少）, 更新用户佣金（增加）
     *
     * @param id        用户编号
     * @param incrCount 减少冻结佣金（负数）
     * @return 更新条数
     */
    default int updateFrozenPriceDecrAndPriceIncr(Long id, Integer incrCount) {
        Assert.isTrue(incrCount < 0);
        LambdaUpdateWrapper<BrokerageUserDO> lambdaUpdateWrapper = new LambdaUpdateWrapper<BrokerageUserDO>()
                .setSql(" frozen_price = frozen_price + " + incrCount + // 负数，所以使用 + 号
                        ", brokerage_price = brokerage_price + " + -incrCount) // 负数，所以使用 - 号
                .eq(BrokerageUserDO::getId, id)
                .ge(BrokerageUserDO::getFrozenPrice, -incrCount); // cas 逻辑
        return update(null, lambdaUpdateWrapper);
    }

    default void updateBindUserIdAndBindUserTimeToNull(Long id) {
        update(null, new LambdaUpdateWrapper<BrokerageUserDO>()
                .eq(BrokerageUserDO::getId, id)
                .set(BrokerageUserDO::getBindUserId, null).set(BrokerageUserDO::getBindUserTime, null));
    }

    default void updateEnabledFalseAndBrokerageTimeToNull(Long id) {
        update(null, new LambdaUpdateWrapper<BrokerageUserDO>()
                .eq(BrokerageUserDO::getId, id)
                .set(BrokerageUserDO::getBrokerageEnabled, false).set(BrokerageUserDO::getBrokerageTime, null));
    }
    
    /**
     * 更新用户等级
     *
     * @param id      用户编号
     * @param levelId 等级编号
     */
    default void updateLevel(Long id, Long levelId) {
        update(null, new LambdaUpdateWrapper<BrokerageUserDO>()
                .eq(BrokerageUserDO::getId, id)
                .set(BrokerageUserDO::getLevelId, levelId));
    }

    @Select("SELECT bind_user_id AS id, COUNT(1) AS brokerageUserCount FROM trade_brokerage_user " +
            "WHERE bind_user_id IS NOT NULL AND deleted = FALSE " +
            "AND bind_user_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY bind_user_id " +
            "ORDER BY brokerageUserCount DESC")
    IPage<AppBrokerageUserRankByUserCountRespVO> selectCountPageGroupByBindUserId(Page<?> page,
                                                                                  @Param("beginTime") LocalDateTime beginTime,
                                                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 下级分销统计（分页）
     *
     * @param bizType      业务类型
     * @param status       状态
     * @param ids          用户编号列表
     * @param sortingField 排序字段
     * @return 下级分销统计分页列表
     */
    IPage<AppBrokerageUserChildSummaryRespVO> selectSummaryPageByUserId(Page<?> page,
                                                                        @Param("bizType") Integer bizType,
                                                                        @Param("status") Integer status,
                                                                        @Param("ids") Collection<Long> ids,
                                                                        @Param("sortingField") SortingField sortingField);

    /**
     * 获得被 bindUserIds 推广的用户编号数组
     *
     * @param bindUserIds 推广员编号数组
     * @return 用户编号数组
     */
    default List<Long> selectIdListByBindUserIdIn(Collection<Long> bindUserIds) {
        return Convert.toList(Long.class,
                selectObjs(new LambdaQueryWrapperX<BrokerageUserDO>()
                        .select(Collections.singletonList(BrokerageUserDO::getId)) // 只查询 id 字段，加速返回速度
                        .in(BrokerageUserDO::getBindUserId, bindUserIds)));
    }

    /**
     * 除自身外，获取上级链路中第一个分公司人员
     * @param userId
     * @return
     */
    Integer getFirstBossBrokerageUser(@Param("userId") Long userId);

    /**
     * 统计指定用户下特定等级的代理用户数量
     * @param userId 用户编号
     * @param levelId 等级编号
     * @return 用户数量
     */
    Integer countAgentUserByBindUserAndLevel(@Param("userId") Long userId, @Param("levelId") Long levelId);
    
    /**
     * 根据等级查询分销用户列表
     * 
     * @param levelId 等级编号
     * @return 分销用户列表
     */
    default List<BrokerageUserDO> selectListByLevelId(Long levelId) {
        return selectList(new LambdaQueryWrapperX<BrokerageUserDO>()
                .eq(BrokerageUserDO::getLevelId, levelId));
    }

    /**
     * 统计指定用户的所有下级用户数量（树形结构）
     * 
     * @param userId 用户编号
     * @return 下级用户数量
     */
    Long countByBindUserIdTree(@Param("userId") Long userId);

    /**
     * 统计指定用户的所有下级用户中在指定时间之后创建的用户数量（树形结构）
     * 
     * @param userId 用户编号
     * @param createTime 创建时间
     * @return 下级用户数量
     */
    Long countByBindUserIdTreeAndCreateTimeGte(@Param("userId") Long userId, @Param("createTime") LocalDateTime createTime);

    /**
     * 统计指定用户的直接下级中满足分销资格的用户数量
     * 
     * @param userId 用户编号
     * @param enabled 是否有分销资格
     * @return 下级用户数量
     */
    Long countByBindUserIdAndBrokerageEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * 统计指定用户的直接下级中满足分销资格且在指定时间之后创建的用户数量
     * 
     * @param userId 用户编号
     * @param enabled 是否有分销资格
     * @param createTime 创建时间
     * @return 下级用户数量
     */
    Long countByBindUserIdAndBrokerageEnabledAndCreateTimeGte(@Param("userId") Long userId,
                                                              @Param("enabled") Boolean enabled,
                                                              @Param("createTime") LocalDateTime createTime,
                                                              @Param("level") Integer level);

    /**
     * 统计指定用户的间接下级中满足分销资格的用户数量
     * 
     * @param userId 用户编号
     * @param enabled 是否有分销资格
     * @return 下级用户数量
     */
    Long countByIndirectBindUserIdAndBrokerageEnabled(@Param("userId") Long userId, @Param("enabled") Boolean enabled);

    /**
     * 统计指定用户的间接下级中满足分销资格且在指定时间之后创建的用户数量
     * 
     * @param userId 用户编号
     * @param enabled 是否有分销资格
     * @param createTime 创建时间
     * @return 下级用户数量
     */
    Long countByIndirectBindUserIdAndBrokerageEnabledAndCreateTimeGte(@Param("userId") Long userId,
                                                                      @Param("enabled") Boolean enabled,
                                                                      @Param("createTime") LocalDateTime createTime,
                                                                      @Param("level") Integer level);

    /**
     * 使用级联查询获取所有下级用户（社群成员）
     *
     * @param userId 用户ID
     * @param level 用户等级（可选）
     * @param nickname 用户昵称（可选）
     * @param pageSize 每页大小
     * @param offset 偏移量
     * @return 下级用户列表
     */
    List<BrokerageUserDO> selectCommunityMembersByUserId(@Param("userId") Long userId,
                                                         @Param("level") Integer level,
                                                         @Param("nickname") String nickname,
                                                         @Param("pageSize") Integer pageSize,
                                                         @Param("offset") Integer offset);

    /**
     * 使用级联查询获取所有下级用户总数（社群成员总数）
     *
     * @param userId 用户ID
     * @param level 用户等级（可选）
     * @param nickname 用户昵称（可选）
     * @return 下级用户总数
     */
    Long selectCommunityMemberCountByUserId(@Param("userId") Long userId, 
                                          @Param("level") Integer level,
                                          @Param("nickname") String nickname);

    /**
     * 使用级联查询获取直接代理成员列表
     *
     * @param userId 用户ID
     * @param level 用户等级（可选）
     * @param pageSize 每页大小
     * @param offset 偏移量
     * @return 直接代理成员列表
     */
    List<BrokerageUserDO> selectDirectAgentMembersByUserId(@Param("userId") Long userId,
                                                           @Param("level") Integer level,
                                                           @Param("nickname") String nickname,
                                                           @Param("pageSize") Integer pageSize,
                                                           @Param("offset") Integer offset);

    /**
     * 使用级联查询获取直接代理成员总数
     *
     * @param userId 用户ID
     * @param level 用户等级（可选）
     * @return 直接代理成员总数
     */
    Long selectDirectAgentMemberCountByUserId(@Param("userId") Long userId,
                                              @Param("level") Integer level,
                                              @Param("nickname") String nickname);

    /**
     * 使用级联查询获取间接代理成员列表
     *
     * @param userId 用户ID
     * @param level 用户等级（可选）
     * @param pageSize 每页大小
     * @param offset 偏移量
     * @return 间接代理成员列表
     */
    List<BrokerageUserDO> selectIndirectAgentMembersByUserId(@Param("userId") Long userId,
                                                             @Param("level") Integer level,
                                                             @Param("nickname") String nickname,
                                                             @Param("pageSize") Integer pageSize,
                                                             @Param("offset") Integer offset);

    /**
     * 使用级联查询获取间接代理成员总数
     *
     * @param userId 用户ID
     * @param level 用户等级（可选）
     * @return 间接代理成员总数
     */
    Long selectIndirectAgentMemberCountByUserId(@Param("userId") Long userId,
                                                @Param("level") Integer level,
                                                @Param("nickname") String nickname);

    /**
     * 向上查找分公司或总公司级别的上级用户
     *
     * @param userId 用户ID
     * @return 分公司或总公司级别的用户，如果没有找到则返回null
     */
    BrokerageUserDO selectCompanyLevelAncestor(@Param("userId") Long userId);

    /**
     * 获取整条链路的顶层用户（递归向上直到没有上级）
     *
     * @param userId 用户ID
     * @return 顶层用户，如果不存在则返回 null
     */
    BrokerageUserDO selectTopAncestor(@Param("userId") Long userId);

    /**
     * 获取指定用户的所有下级用户（用于构建团队树）
     *
     * @param rootUserId 根用户ID
     * @return 下级用户列表
     */
    List<BrokerageUserDO> selectTeamMembersByRootUserId(@Param("rootUserId") Long rootUserId);

    /**
     * 获取指定用户的直接下级人数
     *
     * @param userId 用户ID
     * @return 直接下级人数
     */
    Integer selectDirectChildrenCount(@Param("userId") Long userId);

    /**
     * 批量获取用户的直接下级人数
     *
     * @param userIds 用户ID集合
     * @return 查询结果列表
     */
    List<Map<String, Object>> selectDirectChildrenCountBatch(@Param("userIds") Set<Long> userIds);

    /**
     * 通过绑定时间（缺失则回退到创建时间）统计在当前用户之前的受邀人数，
     * 结合用户自身即可得出“我是第几个被邀请”的序号。
     */
    @Select("SELECT COUNT(1) FROM trade_brokerage_user " +
        "WHERE deleted = FALSE AND bind_user_id = #{bindUserId} AND brokerage_enabled = TRUE " +
        "AND (COALESCE(bind_user_time, create_time, '1970-01-01 00:00:00') < #{bindUserTime} " +
        "OR (COALESCE(bind_user_time, create_time, '1970-01-01 00:00:00') = #{bindUserTime} AND id <= #{userId}))")
    Integer selectInviteOrderIndex(@Param("bindUserId") Long bindUserId,
                                   @Param("bindUserTime") LocalDateTime bindUserTime,
                                   @Param("userId") Long userId);
}
