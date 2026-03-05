package com.hissp.distribution.module.trade.service.brokerage;

import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertMap;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordBizDetailRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageProductPriceRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageStatisticsRespBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.UserBrokerageSummaryRespBO;

import jakarta.validation.Valid;

/**
 * 佣金记录 Service 接口
 *
 * @author owen
 */
public interface BrokerageRecordService {

    /**
     * 获得佣金记录
     *
     * @param id 编号
     * @return 佣金记录
     */
    BrokerageRecordDO getBrokerageRecord(Long id);

    /**
     * 获得佣金记录分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金记录分页
     */
    PageResult<BrokerageRecordDO> getBrokerageRecordPage(BrokerageRecordPageReqVO pageReqVO);

    /**
     * 获得佣金记录关联业务详情
     *
     * @param id 佣金记录编号
     * @return 业务详情
     */
    BrokerageRecordBizDetailRespVO getBrokerageRecordBizDetail(Long id);

    /**
     * 增加佣金【含多级分佣逻辑】
     *
     * @param userId  会员编号
     * @param bizType 业务类型
     * @param list    请求参数列表
     */
    void addBrokerage4Orders(Long userId, BrokerageRecordBizTypeEnum bizType, @Valid List<BrokerageAddReqBO> list);

    /**
     * 即时 增加（减少）佣金及记录【只针对自己】
     *
     * @param userId         会员编号
     * @param bizType        业务类型
     * @param bizId          业务编号
     * @param brokeragePrice 佣金
     * @param title          标题
     */
    void updateBrokerageImmediately(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId, Integer brokeragePrice, String title);

    /**
     * 添加分佣（分佣记录、分佣余额、延迟判断）
     * @param brokerageRecordDO 分佣对象
     */
    void addBrokerage(BrokerageRecordDO brokerageRecordDO);

    /**
     * 减少佣金【只针对自己】
     *
     * @param userId         会员编号
     * @param bizType        业务类型
     * @param bizId          业务编号
     * @param brokeragePrice 佣金
     * @param title          标题
     */
    default void reduceBrokerage(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId, Integer brokeragePrice, String title) {
        updateBrokerageImmediately(userId, bizType, bizId, -brokeragePrice, title);
    }

    /**
     * 取消佣金：将佣金记录，状态修改为已失效
     *
     * @param bizType 业务类型
     * @param bizId   业务编号
     */
    void cancelBrokerage(BrokerageRecordBizTypeEnum bizType, String bizId);

    /**
     * 取消佣金：根据业务编号查找并取消所有相关的分佣记录
     *
     * @param bizId 业务编号
     */
    void cancelBrokerage(String bizId);

    /**
     * 是否存在该业务编号对应的“已结算”佣金记录
     * @param bizId 业务编号
     * @return true 存在已结算佣金
     */
    boolean existsSettledByBizId(String bizId);

    /**
     * 解冻佣金：将待结算的佣金记录，状态修改为已结算
     *
     * @return 解冻佣金的数量
     */
    int unfreezeRecord();

    /**
     * 根据业务编号列表，解冻对应的待结算佣金
     *
     * @param bizIds 业务编号集合
     */
    void settleWaitUnfreezeByBizIds(Collection<String> bizIds);

    /**
     * 为指定业务编号的待结算佣金记录补齐冻结信息
     *
     * @param bizIds       业务编号集合
     * @param frozenDays   冻结天数，小于等于 0 表示立即解冻
     * @param unfreezeTime 解冻时间，为空时由实现自行推算
     */
    void scheduleWaitSettlementByBizIds(Collection<String> bizIds, Integer frozenDays, LocalDateTime unfreezeTime);

    /**
     * 根据业务编号获取佣金记录列表
     *
     * @param bizId 业务编号
     * @return 佣金记录列表
     */
    List<BrokerageRecordDO> getBrokerageRecordsByBizId(String bizId);

    /**
     * 按照 userId，汇总每个用户的佣金
     *
     * @param userIds 用户编号
     * @param bizType 业务类型
     * @param status  佣金状态
     * @return 用户佣金汇总 List
     */
    List<UserBrokerageSummaryRespBO> getUserBrokerageSummaryListByUserId(Collection<Long> userIds,
                                                                         Integer bizType, Integer status);

    /**
     * 获取指定用户的佣金统计
     *
     * @param userId 用户编号
     * @return 佣金统计
     */
    BrokerageStatisticsRespBO getUserBrokerageStatistics(Long userId);

    /**
     * 按照 userId，汇总每个用户的佣金
     *
     * @param userIds 用户编号
     * @param bizType 业务类型
     * @param status  佣金状态
     * @return 用户佣金汇总 Map
     */
    default Map<Long, UserBrokerageSummaryRespBO> getUserBrokerageSummaryMapByUserId(Collection<Long> userIds,
                                                                                     Integer bizType, Integer status) {
        return convertMap(getUserBrokerageSummaryListByUserId(userIds, bizType, status),
                UserBrokerageSummaryRespBO::getUserId);
    }

    /**
     * 获得用户佣金合计
     *
     * @param userId    用户编号
     * @param bizType   业务类型
     * @param status    状态
     * @param beginTime 开始时间
     * @param endTime   截止时间
     * @return 用户佣金合计
     */
    Integer getSummaryPriceByUserId(Long userId, BrokerageRecordBizTypeEnum bizType, BrokerageRecordStatusEnum status,
                                    LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得用户佣金合计(多业务类型)
     *
     * @param userId    用户编号
     * @param bizTypes  业务类型列表
     * @param status    状态
     * @param beginTime 开始时间
     * @param endTime   截止时间
     * @return 用户佣金合计
     */
    Integer getSummaryAmountByUserIdAndBizTypesBetween(Long userId, List<BrokerageRecordBizTypeEnum> bizTypes, BrokerageRecordStatusEnum status,
                                                       LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获得用户佣金排行分页列表（基于佣金总数）
     *
     * @param pageReqVO 分页查询
     * @return 排行榜分页
     */
    PageResult<AppBrokerageUserRankByPriceRespVO> getBrokerageUserChildSummaryPageByPrice(
            AppBrokerageUserRankPageReqVO pageReqVO);

    /**
     * 获取用户的排名（基于佣金总数）
     *
     * @param userId 用户编号
     * @param times  时间范围
     * @return 用户的排名
     */
    Integer getUserRankByPrice(Long userId, LocalDateTime[] times);

    /**
     * 计算商品被购买后，推广员可以得到的佣金
     *
     * @param userId 用户编号
     * @param spuId  商品编号
     * @return 用户佣金
     */
    AppBrokerageProductPriceRespVO calculateProductBrokeragePrice(Long userId, Long spuId);

    /**
     *  批量添加记录，默认使用全局统一延迟结算处理，如给定了冻结时间，按冻结时间否则按系统
     * @param brokerageRecords 记录
     */
    void addBrokerageBatch(List<BrokerageRecordDO> brokerageRecords);

}
