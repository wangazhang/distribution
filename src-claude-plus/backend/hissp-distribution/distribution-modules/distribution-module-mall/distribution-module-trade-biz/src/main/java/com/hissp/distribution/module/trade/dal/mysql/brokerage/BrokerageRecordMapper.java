package com.hissp.distribution.module.trade.dal.mysql.brokerage;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.yulichang.toolkit.MPJWrappers;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.service.brokerage.bo.UserBrokerageSummaryRespBO;

import cn.hutool.core.bean.BeanUtil;

/**
 * 佣金记录 Mapper
 *
 * @author owen
 */
@Mapper
public interface BrokerageRecordMapper extends BaseMapperX<BrokerageRecordDO> {

    /** 平台虚拟账户 ID。 */
    long PLATFORM_ACCOUNT_USER_ID = 999L;
    /** 总部虚拟账户 ID。 */
    long HQ_ACCOUNT_USER_ID = 1000L;

    default PageResult<BrokerageRecordDO> selectPage(BrokerageRecordPageReqVO reqVO) {
        LambdaQueryWrapperX<BrokerageRecordDO> wrapper = new LambdaQueryWrapperX<BrokerageRecordDO>()
                .eqIfPresent(BrokerageRecordDO::getUserId, reqVO.getUserId())
                .eqIfPresent(BrokerageRecordDO::getBizType, reqVO.getBizType())
                .eqIfPresent(BrokerageRecordDO::getBizId, reqVO.getBizId())
                .eqIfPresent(BrokerageRecordDO::getStatus, reqVO.getStatus())
                .eqIfPresent(BrokerageRecordDO::getSourceUserLevel, reqVO.getSourceUserLevel())
                .betweenIfPresent(BrokerageRecordDO::getCreateTime, reqVO.getCreateTime());
        Long queryUserId = reqVO.getUserId();
        if (!Boolean.TRUE.equals(reqVO.getIncludePlatformAccount())
                && !Objects.equals(queryUserId, PLATFORM_ACCOUNT_USER_ID)) {
            wrapper.ne(BrokerageRecordDO::getUserId, PLATFORM_ACCOUNT_USER_ID);
        }
        if (!Boolean.TRUE.equals(reqVO.getIncludeHqAccount())
                && !Objects.equals(queryUserId, HQ_ACCOUNT_USER_ID)) {
            wrapper.ne(BrokerageRecordDO::getUserId, HQ_ACCOUNT_USER_ID);
        }
        wrapper.orderByDesc(BrokerageRecordDO::getId);
        return selectPage(reqVO, wrapper);
    }

    default List<BrokerageRecordDO> selectListByStatusAndUnfreezeTimeLt(Integer status, LocalDateTime unfreezeTime) {
        return selectList(new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getStatus, status)
                .lt(BrokerageRecordDO::getUnfreezeTime, unfreezeTime));
    }

    default int updateByIdAndStatus(Integer id, Integer status, BrokerageRecordDO updateObj) {
        return update(updateObj, new LambdaQueryWrapper<BrokerageRecordDO>()
                .eq(BrokerageRecordDO::getId, id)
                .eq(BrokerageRecordDO::getStatus, status));
    }

    default List<BrokerageRecordDO> selectListByBizTypeAndBizId(Integer bizType, String bizId) {
        return selectList(BrokerageRecordDO::getBizType, bizType,
                BrokerageRecordDO::getBizId, bizId);
    }

    default List<BrokerageRecordDO> selectListByBizId(String bizId) {
        return selectList(BrokerageRecordDO::getBizId, bizId);
    }

    default List<UserBrokerageSummaryRespBO> selectCountAndSumPriceByUserIdInAndBizTypeAndStatus(Collection<Long> userIds,
                                                                                                 Integer bizType,
                                                                                                 Integer status) {
        var wrapper = MPJWrappers.lambdaJoin(BrokerageRecordDO.class)
                .select(BrokerageRecordDO::getUserId)
                .selectCount(BrokerageRecordDO::getId, UserBrokerageSummaryRespBO::getCount)
                .selectSum(BrokerageRecordDO::getPrice)
                .in(BrokerageRecordDO::getUserId, userIds)
                .groupBy(BrokerageRecordDO::getUserId); // 按照 userId 聚合
        if (bizType != null) {
            wrapper.eq(BrokerageRecordDO::getBizType, bizType);
        }
        if (status != null) {
            wrapper.eq(BrokerageRecordDO::getStatus, status);
        }
        List<Map<String, Object>> list = selectMaps(wrapper);
        return BeanUtil.copyToList(list, UserBrokerageSummaryRespBO.class);
        // selectJoinList有BUG，会与租户插件冲突：解析SQL时，发生异常 https://gitee.com/best_handsome/mybatis-plus-join/issues/I84GYW
//            return selectJoinList(UserBrokerageSummaryBO.class, MPJWrappers.lambdaJoin(BrokerageRecordDO.class)
//                    .select(BrokerageRecordDO::getUserId)
//                    .selectCount(BrokerageRecordDO::getId, UserBrokerageSummaryBO::getCount)
//                    .selectSum(BrokerageRecordDO::getPrice)
//                    .in(BrokerageRecordDO::getUserId, userIds)
//                    .eq(BrokerageRecordDO::getBizType, bizType)
//                    .eq(BrokerageRecordDO::getStatus, status)
//                    .groupBy(BrokerageRecordDO::getUserId));
    }

    @Select({
            "SELECT",
            "    SUM(CASE WHEN price > 0 THEN price ELSE 0 END) AS totalIncome,",
            "    SUM(CASE WHEN price < 0 THEN -price ELSE 0 END) AS totalExpense,",
            "    COUNT(1) AS totalCount,",
            "    SUM(CASE WHEN status = #{pendingStatus} THEN 1 ELSE 0 END) AS pendingCount,",
            "    SUM(CASE WHEN status = #{pendingStatus} THEN price ELSE 0 END) AS pendingAmount,",
            "    SUM(CASE WHEN status = #{settledStatus} THEN 1 ELSE 0 END) AS settledCount,",
            "    SUM(CASE WHEN status = #{settledStatus} THEN price ELSE 0 END) AS settledAmount",
            "FROM trade_brokerage_record",
            "WHERE user_id = #{userId}",
            "  AND deleted = FALSE"
    })
    Map<String, Object> selectStatisticsByUserId(@Param("userId") Long userId,
                                                 @Param("pendingStatus") Integer pendingStatus,
                                                 @Param("settledStatus") Integer settledStatus);

    @Select("SELECT SUM(price) FROM trade_brokerage_record " +
            "WHERE user_id = #{userId} AND biz_type = #{bizType} AND status = #{status} " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} AND deleted = FALSE")
    Integer selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(@Param("userId") Long userId,
                                                                     @Param("bizType") Integer bizType,
                                                                     @Param("status") Integer status,
                                                                     @Param("beginTime") LocalDateTime beginTime,
                                                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询用户指定多个业务类型和状态下的佣金合计
     *
     * @param userId    用户编号
     * @param bizTypes  业务类型列表
     * @param status    状态
     * @param beginTime 开始时间
     * @param endTime   截止时间
     * @return 佣金合计
     */

    Integer selectSummaryPriceByUserIdAndBizTypesAndCreateTimeBetween(@Param("userId") Long userId,
                                                              @Param("bizTypes") List<Integer> bizTypes,
                                                              @Param("status") Integer status,
                                                              @Param("beginTime") LocalDateTime beginTime,
                                                              @Param("endTime") LocalDateTime endTime);

    // TODO @芋艿：收敛掉 @Select 注解操作，统一成 MyBatis-Plus 的方式，或者 xml
    @Select("SELECT user_id AS id, SUM(price) AS brokeragePrice FROM trade_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id " +
            "ORDER BY brokeragePrice DESC")
    IPage<AppBrokerageUserRankByPriceRespVO> selectSummaryPricePageGroupByUserId(IPage<?> page,
                                                                                 @Param("bizType") Integer bizType,
                                                                                 @Param("status") Integer status,
                                                                                 @Param("beginTime") LocalDateTime beginTime,
                                                                                 @Param("endTime") LocalDateTime endTime);

    @Select("SELECT COUNT(1) FROM trade_brokerage_record " +
            "WHERE biz_type = #{bizType} AND status = #{status} AND deleted = FALSE " +
            "AND unfreeze_time BETWEEN #{beginTime} AND #{endTime} " +
            "GROUP BY user_id HAVING SUM(price) > #{brokeragePrice}")
    Integer selectCountByPriceGt(@Param("brokeragePrice") Integer brokeragePrice,
                                 @Param("bizType") Integer bizType,
                                 @Param("status") Integer status,
                                 @Param("beginTime") LocalDateTime beginTime,
                                 @Param("endTime") LocalDateTime endTime);

    Long selectCountByBizIdAndStatus(@Param("bizId") String bizId, @Param("status") Integer status);

}
