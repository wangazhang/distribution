package com.hissp.distribution.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.*;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.number.MoneyUtils;
import com.hissp.distribution.framework.ip.core.utils.AreaUtils;
import com.hissp.distribution.framework.mybatis.core.util.MyBatisUtils;
import com.hissp.distribution.module.mb.api.order.MbOrderApi;
import com.hissp.distribution.module.mb.api.order.dto.MbOrderSimpleRespDTO;
import com.hissp.distribution.module.product.api.sku.ProductSkuApi;
import com.hissp.distribution.module.product.api.sku.dto.ProductSkuRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordBizDetailRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageProductPriceRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankPageReqVO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageRecordConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.dal.dataobject.config.TradeConfigDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.mysql.brokerage.BrokerageRecordMapper;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderItemMapper;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderMapper;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.enums.delivery.DeliveryTypeEnum;
import com.hissp.distribution.module.trade.enums.order.TradeOrderItemAfterSaleStatusEnum;
import com.hissp.distribution.module.trade.enums.order.TradeOrderRefundStatusEnum;
import com.hissp.distribution.module.trade.enums.order.TradeOrderStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageAddReqBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageStatisticsRespBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.UserBrokerageSummaryRespBO;
import com.hissp.distribution.module.trade.service.config.TradeConfigService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.getMaxValue;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.getMinValue;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.BROKERAGE_RECORD_NOT_EXISTS;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH;

/**
 * 佣金记录 Service 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class BrokerageRecordServiceImpl implements BrokerageRecordService {

    @Resource
    private BrokerageRecordMapper brokerageRecordMapper;
    @Resource
    private TradeConfigService tradeConfigService;
    @Resource
    private BrokerageUserService brokerageUserService;
    @Resource
    @Lazy
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    @Lazy
    private MbOrderApi mbOrderApi;

    // ====== public 方法（接口实现、对外服务） ======
    @Override
    public BrokerageRecordDO getBrokerageRecord(Long id) {
        return brokerageRecordMapper.selectById(id);
    }

    @Override
    public PageResult<BrokerageRecordDO> getBrokerageRecordPage(BrokerageRecordPageReqVO pageReqVO) {
        return brokerageRecordMapper.selectPage(pageReqVO);
    }

    @Override
    public BrokerageRecordBizDetailRespVO getBrokerageRecordBizDetail(Long id) {
        BrokerageRecordDO record = brokerageRecordMapper.selectById(id);
        if (record == null) {
            throw exception(BROKERAGE_RECORD_NOT_EXISTS);
        }
        BrokerageRecordBizDetailRespVO respVO = buildBizDetailBase(record);
        BrokerageRecordBizCategoryEnum category = BrokerageRecordBizCategoryEnum.fromType(record.getBizCategory());
        if (category == BrokerageRecordBizCategoryEnum.MALL_ORDER) {
            respVO.setMallOrder(buildMallOrderDetail(record));
        } else if (category == BrokerageRecordBizCategoryEnum.MB_ORDER) {
                respVO.setMbOrder(buildMbOrderDetail(record));
        }
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrokerage4Orders(Long userId, BrokerageRecordBizTypeEnum bizType, List<BrokerageAddReqBO> list) {
        TradeConfigDO memberConfig = tradeConfigService.getTradeConfig();
        // 0 未启用分销功能
        if (memberConfig == null || !BooleanUtil.isTrue(memberConfig.getBrokerageEnabled())) {
            log.error("[addBrokerage][增加佣金失败：brokerageEnabled 未配置，userId({}) bizType({}) list({})", userId, bizType,
                list);
            return;
        }

        // 1.1 获得一级推广人
        BrokerageUserDO firstUser = brokerageUserService.getBindBrokerageUser(userId);
        if (firstUser == null || !BooleanUtil.isTrue(firstUser.getBrokerageEnabled())) {
            return;
        }
        // 1.2 计算一级分佣
        addBrokerage4Order(firstUser, list, memberConfig.getBrokerageFrozenDays(),
            memberConfig.getBrokerageFirstPercent(), bizType, 1);

        // 2.1 获得二级推广员
        if (firstUser.getBindUserId() == null) {
            return;
        }
        BrokerageUserDO secondUser = brokerageUserService.getBrokerageUser(firstUser.getBindUserId());
        if (secondUser == null || !BooleanUtil.isTrue(secondUser.getBrokerageEnabled())) {
            return;
        }
        // 2.2 计算二级分佣
        addBrokerage4Order(secondUser, list, memberConfig.getBrokerageFrozenDays(),
            memberConfig.getBrokerageSecondPercent(), bizType, 2);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBrokerage(BrokerageRecordBizTypeEnum bizType, String bizId) {
        List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByBizTypeAndBizId(bizType.getType(), bizId);
        if (CollUtil.isEmpty(records)) {
            log.error("[cancelBrokerage][bizId({}) bizType({}) 更新为已失效失败：记录不存在]", bizId, bizType);
            return;
        }

        cancelBrokerageRecords(records, bizId, String.valueOf(bizType.getType()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBrokerage(String bizId) {
        // 根据bizId查找所有相关的分佣记录，不限制业务类型
        List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByBizId(bizId);
        if (CollUtil.isEmpty(records)) {
            log.error("[cancelBrokerage][bizId({}) 更新为已失效失败：记录不存在]", bizId);
            return;
        }

        log.info("[cancelBrokerage][根据bizId({})找到{}条分佣记录，开始取消]", bizId, records.size());
        cancelBrokerageRecords(records, bizId, "ALL");
    }

    /**
     * 取消分佣记录的通用方法
     */
    private void cancelBrokerageRecords(List<BrokerageRecordDO> records, String bizId, String bizType) {
        records.forEach(record -> {
            // 幂等保护：退款会同时触发同步逻辑与消息消费，已取消的记录不需要重复扣回
            if (BrokerageRecordStatusEnum.CANCEL.getStatus().equals(record.getStatus())) {
                log.info("[cancelBrokerageRecords][recordId={} 已处于取消状态，跳过重复处理]", record.getId());
                return;
            }
            // 1. 更新佣金记录为已失效
            BrokerageRecordDO updateObj =
                new BrokerageRecordDO().setStatus(BrokerageRecordStatusEnum.CANCEL.getStatus());
            int updateRows = brokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
            if (updateRows == 0) {
                log.error("[cancelBrokerageRecords][record({}) 更新为已失效失败]", record.getId());
                return;
            }

            // 2. 更新用户的佣金
            if (BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus().equals(record.getStatus())) {
                brokerageUserService.updateUserFrozenPrice(record.getUserId(), -record.getPrice());
            } else if (BrokerageRecordStatusEnum.SETTLEMENT.getStatus().equals(record.getStatus())) {
                brokerageUserService.updateUserPrice(record.getUserId(), -record.getPrice());
            }

            log.info("[cancelBrokerageRecords][成功取消分佣记录: recordId={}, userId={}, bizId={}, bizType={}, price={}]",
                record.getId(), record.getUserId(), bizId, bizType, record.getPrice());
        });
    }
    @Override
    public boolean existsSettledByBizId(String bizId) {
        return brokerageRecordMapper.selectCountByBizIdAndStatus(bizId, BrokerageRecordStatusEnum.SETTLEMENT.getStatus()) > 0;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrokerageImmediately(Long userId, BrokerageRecordBizTypeEnum bizType, String bizId,
        Integer brokeragePrice, String title) {
        // 1. 校验佣金余额
        BrokerageUserDO user = brokerageUserService.getBrokerageUser(userId);
        int balance = Optional.of(user).map(BrokerageUserDO::getBrokeragePrice).orElse(0);
        if (balance + brokeragePrice < 0) {
            throw exception(BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH, MoneyUtils.fenToYuanStr(balance));
        }
        // 3. 新增记录
        BrokerageRecordDO record = BrokerageRecordConvert.INSTANCE.convert(user, bizType, bizId, 0, brokeragePrice,
            LocalDateTime.now(), title, null, null);
        record.setBizCategory(BrokerageRecordBizCategoryEnum.UNKNOWN.getType());

        // 2. 更新佣金余额
        boolean success = brokerageUserService.updateUserPrice(userId, brokeragePrice);
        if (!success) {
            // 失败时，则抛出异常。只会出现扣减佣金时，余额不足的情况
            throw exception(BROKERAGE_WITHDRAW_USER_BALANCE_NOT_ENOUGH, MoneyUtils.fenToYuanStr(balance));
        }
        brokerageRecordMapper.insert(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addBrokerage(BrokerageRecordDO brokerageRecordDO) {
        if (brokerageRecordDO.getBizCategory() == null) {
            brokerageRecordDO.setBizCategory(BrokerageRecordBizCategoryEnum.UNKNOWN.getType());
        }
        // 统一在此处确定记录的冻结与解冻策略。
        Integer recordStatus = brokerageRecordDO.getStatus();
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        Integer defaultFrozenDays = tradeConfig != null ? tradeConfig.getBrokerageFrozenDays() : 0;

        if (recordStatus == null) {
            boolean needWaitSettlement = defaultFrozenDays != null && defaultFrozenDays > 0;
            brokerageRecordDO.setStatus(
                needWaitSettlement ? BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus()
                    : BrokerageRecordStatusEnum.SETTLEMENT.getStatus());
            brokerageRecordDO.setFrozenDays(defaultFrozenDays);
        }

        Integer brokerageFrozenDays = brokerageRecordDO.getFrozenDays();
        if (brokerageFrozenDays == null) {
            brokerageFrozenDays = defaultFrozenDays;
            brokerageRecordDO.setFrozenDays(brokerageFrozenDays);
        }

        boolean waitSettlement =
            Objects.equals(brokerageRecordDO.getStatus(), BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        if (waitSettlement && brokerageFrozenDays != null && brokerageFrozenDays > 0
            && brokerageRecordDO.getUnfreezeTime() == null) {
            brokerageRecordDO.setUnfreezeTime(LocalDateTime.now().plusDays(brokerageFrozenDays));
        }
        if (!waitSettlement) {
            brokerageRecordDO.setFrozenDays(ObjectUtil.defaultIfNull(brokerageFrozenDays, 0));
            brokerageRecordDO.setUnfreezeTime(null);
            brokerageRecordDO.setStatus(BrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        }

        brokerageRecordMapper.insert(brokerageRecordDO);
        if (waitSettlement) {
            Integer price = brokerageRecordDO.getTotalPrice();
            int freezeAmount = price >= 0 ? price : Math.abs(price);
            brokerageUserService.updateUserFrozenPrice(brokerageRecordDO.getUserId(), freezeAmount);
        } else {
            brokerageUserService.updateUserPrice(brokerageRecordDO.getUserId(), brokerageRecordDO.getTotalPrice());
        }
    }

    @Override
    public void addBrokerageBatch(List<BrokerageRecordDO> brokerageRecords) {
        brokerageRecords.forEach(record -> {
            brokerageRecordService.addBrokerage(record);
        });
    }

    @Override
    public List<BrokerageRecordDO> getBrokerageRecordsByBizId(String bizId) {
        return brokerageRecordMapper.selectListByBizId(bizId);
    }



    @Override
    public int unfreezeRecord() {
        // 1. 查询待结算的佣金记录
        List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByStatusAndUnfreezeTimeLt(
            BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus(), LocalDateTime.now());
        if (CollUtil.isEmpty(records)) {
            return 0;
        }

        // 2. 遍历执行
        int count = 0;
        for (BrokerageRecordDO record : records) {
            try {
                boolean success = getSelf().unfreezeRecord(record);
                if (success) {
                    count++;
                }
            } catch (Exception e) {
                log.error("[unfreezeRecord][record({}) 更新为已结算失败]", record.getId(), e);
            }
        }
        return count;
    }

    @Override
    public void settleWaitUnfreezeByBizIds(Collection<String> bizIds) {
        if (CollUtil.isEmpty(bizIds)) {
            return;
        }
        // 每个订单项可对应多条佣金记录（正向收入 + 反向出资），因此这里逐条行级解冻。
        Set<String> distinct = CollUtil.newHashSet(bizIds);
        for (String bizId : distinct) {
            List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByBizId(bizId);
            if (CollUtil.isEmpty(records)) {
                continue;
            }
            for (BrokerageRecordDO record : records) {
                if (!Objects.equals(record.getStatus(), BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus())) {
                    continue;
                }
                if (record.getPrice() == null ) {
                    continue;
                }
                try {
                    getSelf().unfreezeRecord(record);
                } catch (Exception ex) {
                    log.error("[settleWaitUnfreezeByBizIds][bizId={}] 解冻记录({})失败", bizId, record.getId(), ex);
                }
            }
        }
    }

    @Override
    public void scheduleWaitSettlementByBizIds(Collection<String> bizIds, Integer frozenDays, LocalDateTime unfreezeTime) {
        if (CollUtil.isEmpty(bizIds)) {
            return;
        }
        int safeFrozenDays = ObjectUtil.defaultIfNull(frozenDays, 0);
        if (safeFrozenDays <= 0) {
            settleWaitUnfreezeByBizIds(bizIds);
            return;
        }
        LocalDateTime targetUnfreezeTime = unfreezeTime != null
            ? unfreezeTime
            : LocalDateTime.now().plusDays(safeFrozenDays);
        Set<String> distinct = CollUtil.newHashSet(bizIds);
        for (String bizId : distinct) {
            List<BrokerageRecordDO> records = brokerageRecordMapper.selectListByBizId(bizId);
            if (CollUtil.isEmpty(records)) {
                continue;
            }
            for (BrokerageRecordDO record : records) {
                if (!Objects.equals(record.getStatus(), BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus())) {
                    continue;
                }
                BrokerageRecordDO updateObj = new BrokerageRecordDO()
                    .setFrozenDays(safeFrozenDays)
                    .setUnfreezeTime(targetUnfreezeTime);
                brokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
            }
        }
    }

    /**
     * 解冻单条佣金记录
     *
     * @param record 佣金记录
     * @return 解冻是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean unfreezeRecord(BrokerageRecordDO record) {
        // 更新记录状态
        BrokerageRecordDO updateObj = new BrokerageRecordDO()
            .setStatus(BrokerageRecordStatusEnum.SETTLEMENT.getStatus()).setUnfreezeTime(LocalDateTime.now());
        int updateRows = brokerageRecordMapper.updateByIdAndStatus(record.getId(), record.getStatus(), updateObj);
        if (updateRows == 0) {
            log.error("[unfreezeRecord][record({}) 更新为已结算失败]", record.getId());
            return false;
        }

        // 更新用户冻结佣金
        if (record.getPrice() >= 0) {
            brokerageUserService.updateFrozenPriceDecrAndPriceIncr(record.getUserId(), -record.getPrice());
        } else {
            int abs = Math.abs(record.getPrice());
            brokerageUserService.updateUserFrozenPrice(record.getUserId(), -abs);
            brokerageUserService.updateUserPrice(record.getUserId(), record.getPrice());
        }
        log.info("[unfreezeRecord][record({}) 更新为已结算成功]", record.getId());
        return true;
    }

    @Override
    public List<UserBrokerageSummaryRespBO> getUserBrokerageSummaryListByUserId(Collection<Long> userIds,
        Integer bizType, Integer status) {
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return brokerageRecordMapper.selectCountAndSumPriceByUserIdInAndBizTypeAndStatus(userIds, bizType, status);
    }

    @Override
    public BrokerageStatisticsRespBO getUserBrokerageStatistics(Long userId) {
        BrokerageStatisticsRespBO statistics = new BrokerageStatisticsRespBO();
        if (userId == null) {
            return statistics;
        }
        Map<String, Object> rawStatistics = brokerageRecordMapper.selectStatisticsByUserId(userId,
                BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus(),
                BrokerageRecordStatusEnum.SETTLEMENT.getStatus());
        if (rawStatistics == null || rawStatistics.isEmpty()) {
            return statistics;
        }
        statistics.setTotalIncome(Convert.toInt(rawStatistics.getOrDefault("totalIncome", 0)));
        statistics.setTotalExpense(Convert.toInt(rawStatistics.getOrDefault("totalExpense", 0)));
        statistics.setTotalCount(Convert.toInt(rawStatistics.getOrDefault("totalCount", 0)));
        statistics.setPendingCount(Convert.toInt(rawStatistics.getOrDefault("pendingCount", 0)));
        statistics.setPendingAmount(Convert.toInt(rawStatistics.getOrDefault("pendingAmount", 0)));
        statistics.setSettledCount(Convert.toInt(rawStatistics.getOrDefault("settledCount", 0)));
        statistics.setSettledAmount(Convert.toInt(rawStatistics.getOrDefault("settledAmount", 0)));
        return statistics;
    }

    @Override
    public Integer getSummaryPriceByUserId(Long userId, BrokerageRecordBizTypeEnum bizType,
        BrokerageRecordStatusEnum status, LocalDateTime beginTime, LocalDateTime endTime) {
        return brokerageRecordMapper.selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(userId, bizType.getType(),
            status.getStatus(), beginTime, endTime);
    }

    @Override
    public Integer getSummaryAmountByUserIdAndBizTypesBetween(Long userId, List<BrokerageRecordBizTypeEnum> bizTypes,
        BrokerageRecordStatusEnum status, LocalDateTime beginTime, LocalDateTime endTime) {
        if (CollUtil.isEmpty(bizTypes)) {
            return 0;
        }

        // 转换业务类型为整数类型列表
        List<Integer> bizTypeList =
            bizTypes.stream().map(BrokerageRecordBizTypeEnum::getType).collect(Collectors.toList());

        return brokerageRecordMapper.selectSummaryPriceByUserIdAndBizTypesAndCreateTimeBetween(userId, bizTypeList,
            status.getStatus(), beginTime, endTime);
    }

    @Override
    public PageResult<AppBrokerageUserRankByPriceRespVO>
        getBrokerageUserChildSummaryPageByPrice(AppBrokerageUserRankPageReqVO pageReqVO) {
        IPage<AppBrokerageUserRankByPriceRespVO> pageResult =
            brokerageRecordMapper.selectSummaryPricePageGroupByUserId(MyBatisUtils.buildPage(pageReqVO),
                BrokerageRecordBizTypeEnum.ORDER_ADD.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
                ArrayUtil.get(pageReqVO.getTimes(), 0), ArrayUtil.get(pageReqVO.getTimes(), 1));
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal());
    }

    @Override
    public Integer getUserRankByPrice(Long userId, LocalDateTime[] times) {
        // 用户的推广金额
        Integer price = brokerageRecordMapper.selectSummaryPriceByUserIdAndBizTypeAndCreateTimeBetween(userId,
            BrokerageRecordBizTypeEnum.ORDER_ADD.getType(), BrokerageRecordStatusEnum.SETTLEMENT.getStatus(),
            ArrayUtil.get(times, 0), ArrayUtil.get(times, 1));
        // 排在用户前面的人数
        Integer greaterCount =
            brokerageRecordMapper.selectCountByPriceGt(price, BrokerageRecordBizTypeEnum.ORDER_ADD.getType(),
                BrokerageRecordStatusEnum.SETTLEMENT.getStatus(), ArrayUtil.get(times, 0), ArrayUtil.get(times, 1));
        // 获得排名
        return ObjUtil.defaultIfNull(greaterCount, 0) + 1;
    }

    @Override
    public AppBrokerageProductPriceRespVO calculateProductBrokeragePrice(Long userId, Long spuId) {
        // 1. 构建默认的返回值
        AppBrokerageProductPriceRespVO respVO =
            new AppBrokerageProductPriceRespVO().setEnabled(false).setBrokerageMinPrice(0).setBrokerageMaxPrice(0);

        // 2.1 校验分销功能是否开启
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig == null || BooleanUtil.isFalse(tradeConfig.getBrokerageEnabled())) {
            return respVO;
        }
        // 2.2 校验用户是否有分销资格
        respVO.setEnabled(brokerageUserService.getUserBrokerageEnabled(getLoginUserId()));
        if (BooleanUtil.isFalse(respVO.getEnabled())) {
            return respVO;
        }
        // 2.3 校验商品是否存在
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            return respVO;
        }

        // 3.1 商品单独分佣模式
        Integer fixedMinPrice = 0;
        Integer fixedMaxPrice = 0;
        Integer spuMinPrice = 0;
        Integer spuMaxPrice = 0;
        List<ProductSkuRespDTO> skuList = productSkuApi.getSkuListBySpuId(ListUtil.of(spuId));
        if (BooleanUtil.isTrue(spu.getSubCommissionType())) {
            fixedMinPrice = getMinValue(skuList, ProductSkuRespDTO::getFirstBrokeragePrice);
            fixedMaxPrice = getMaxValue(skuList, ProductSkuRespDTO::getFirstBrokeragePrice);
            // 3.2 全局分佣模式（根据商品价格比例计算）
        } else {
            spuMinPrice = getMinValue(skuList, ProductSkuRespDTO::getPrice);
            spuMaxPrice = getMaxValue(skuList, ProductSkuRespDTO::getPrice);
        }
        respVO.setBrokerageMinPrice(calculatePrice(spuMinPrice, tradeConfig.getBrokerageFirstPercent(), fixedMinPrice));
        respVO.setBrokerageMaxPrice(calculatePrice(spuMaxPrice, tradeConfig.getBrokerageFirstPercent(), fixedMaxPrice));
        return respVO;
    }

    // ====== private 方法（内部工具、辅助） ======
    /**
     * 计算佣金
     *
     * @param basePrice 佣金基数
     * @param percent 佣金比例
     * @param fixedPrice 固定佣金
     * @return 佣金
     */
    int calculatePrice(Integer basePrice, Integer percent, Integer fixedPrice) {
        // 1. 优先使用固定佣金
        if (fixedPrice != null && fixedPrice > 0) {
            return ObjectUtil.defaultIfNull(fixedPrice, 0);
        }
        // 2. 根据比例计算佣金
        if (basePrice != null && basePrice > 0 && percent != null && percent > 0) {
            return MoneyUtils.calculateRatePriceFloor(basePrice, Double.valueOf(percent));
        }
        return 0;
    }

    /**
     * 增加用户佣金并添加记录- 带冻结时间判断
     *
     * @param user 用户
     * @param list 佣金增加参数列表
     * @param brokerageFrozenDays 冻结天数
     * @param brokeragePercent 佣金比例
     * @param bizType 业务类型
     * @param sourceUserLevel 来源用户等级
     */
    private void addBrokerage4Order(BrokerageUserDO user, List<BrokerageAddReqBO> list, Integer brokerageFrozenDays,
        Integer brokeragePercent, BrokerageRecordBizTypeEnum bizType, Integer sourceUserLevel) {
        // 1.1 处理冻结时间
        LocalDateTime unfreezeTime = null;
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) {
            unfreezeTime = LocalDateTime.now().plusDays(brokerageFrozenDays);
        }
        // 1.2 计算分佣
        int totalBrokerage = 0;
        List<BrokerageRecordDO> records = new ArrayList<>();
        for (BrokerageAddReqBO item : list) {
            // 计算金额
            Integer fixedPrice;
            if (Objects.equals(sourceUserLevel, 1)) {
                fixedPrice = item.getFirstFixedPrice();
            } else if (Objects.equals(sourceUserLevel, 2)) {
                fixedPrice = item.getSecondFixedPrice();
            } else {
                throw new IllegalArgumentException(StrUtil.format("用户等级({}) 不合法", sourceUserLevel));
            }
            int brokeragePrice = calculatePrice(item.getBasePrice(), brokeragePercent, fixedPrice);
            if (brokeragePrice <= 0) {
                continue;
            }
            totalBrokerage += brokeragePrice;
            // 创建记录实体
            records.add(BrokerageRecordConvert.INSTANCE.convert(user, bizType, item.getBizId(), brokerageFrozenDays,
                brokeragePrice, unfreezeTime, item.getTitle(), item.getSourceUserId(), sourceUserLevel));
        }
        if (CollUtil.isEmpty(records)) {
            return;
        }
        // 1.3 保存佣金记录
        brokerageRecordMapper.insertBatch(records);

        // 2. 更新用户佣金
        if (brokerageFrozenDays != null && brokerageFrozenDays > 0) { // 更新用户冻结佣金
            brokerageUserService.updateUserFrozenPrice(user.getId(), totalBrokerage);
        } else { // 更新用户可用佣金
            brokerageUserService.updateUserPrice(user.getId(), totalBrokerage);
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private BrokerageRecordServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

    private BrokerageRecordBizDetailRespVO.MallOrderBizDetail buildMallOrderDetail(BrokerageRecordDO record) {
        Long itemId = parseBizId(record.getBizId());
        if (itemId == null) {
            return null;
        }
        TradeOrderItemDO item = tradeOrderItemMapper.selectById(itemId);
        if (item == null) {
            log.warn("[buildMallOrderDetail][recordId={}] 未找到订单项 bizId={}", record.getId(), record.getBizId());
            return null;
        }
        TradeOrderDO order = tradeOrderMapper.selectById(item.getOrderId());
        if (order == null) {
            log.warn("[buildMallOrderDetail][recordId={}] 未找到订单 orderId={}", record.getId(), item.getOrderId());
            return null;
        }
        BrokerageRecordBizDetailRespVO.MallOrderBizDetail detail =
            new BrokerageRecordBizDetailRespVO.MallOrderBizDetail();
        detail.setOrderId(order.getId());
        detail.setOrderNo(order.getNo());
        detail.setOrderStatus(order.getStatus());
        detail.setOrderStatusName(getOrderStatusName(order.getStatus()));
        detail.setRefundStatus(order.getRefundStatus());
        detail.setRefundStatusName(getRefundStatusName(order.getRefundStatus()));
        detail.setOrderPayPrice(order.getPayPrice());
        detail.setPayFinished(Boolean.TRUE.equals(order.getPayStatus()));
        detail.setPayChannelCode(order.getPayChannelCode());
        detail.setPayTime(order.getPayTime());
        detail.setDeliveryType(order.getDeliveryType());
        detail.setDeliveryTypeName(getDeliveryTypeName(order.getDeliveryType()));

        BrokerageRecordBizDetailRespVO.MallOrderReceiver receiver =
            new BrokerageRecordBizDetailRespVO.MallOrderReceiver();
        receiver.setName(order.getReceiverName());
        receiver.setMobile(order.getReceiverMobile());
        receiver.setAreaName(AreaUtils.format(order.getReceiverAreaId()));
        receiver.setDetailAddress(order.getReceiverDetailAddress());
        detail.setReceiver(receiver);

        BrokerageRecordBizDetailRespVO.MallOrderItem orderItem =
            new BrokerageRecordBizDetailRespVO.MallOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setSpuId(item.getSpuId());
        orderItem.setSkuId(item.getSkuId());
        orderItem.setSpuName(item.getSpuName());
        orderItem.setPicUrl(item.getPicUrl());
        orderItem.setCount(item.getCount());
        orderItem.setUnitPrice(item.getPrice());
        orderItem.setPayPrice(item.getPayPrice());
        orderItem.setAfterSaleStatus(item.getAfterSaleStatus());
        orderItem.setAfterSaleStatusName(getAfterSaleStatusName(item.getAfterSaleStatus()));
        orderItem.setPropertiesText(formatSkuProperties(item.getProperties()));
        detail.setItem(orderItem);
        return detail;
    }

    private BrokerageRecordBizDetailRespVO.MbOrderBizDetail buildMbOrderDetail(BrokerageRecordDO record) {
        Long orderId = parseBizId(record.getBizId());
        if (orderId == null) {
            return null;
        }
        MbOrderSimpleRespDTO order = mbOrderApi.getOrderSummary(orderId);
        if (order == null) {
            log.warn("[buildMbOrderDetail][recordId={}] 未找到 MB 订单 bizId={}", record.getId(), record.getBizId());
            return null;
        }
        BrokerageRecordBizDetailRespVO.MbOrderBizDetail detail =
            new BrokerageRecordBizDetailRespVO.MbOrderBizDetail();
        detail.setOrderId(order.getId());
        detail.setOrderNo(order.getOrderNo());
        detail.setStatus(order.getStatus());
        detail.setStatusName(order.getStatusName());
        detail.setBizType(order.getBizType());
        detail.setBizTypeName(order.getBizTypeName());
        detail.setProductId(order.getProductId());
        detail.setProductName(order.getProductName());
        detail.setProductImage(order.getProductImage());
        detail.setQuantity(order.getQuantity());
        detail.setUnitPrice(order.getUnitPrice());
        detail.setTotalPrice(order.getTotalPrice());
        detail.setPayPrice(order.getPayPrice());
        detail.setPayChannelCode(order.getPayChannelCode());
        detail.setPayStatus(order.getPayStatus());
        detail.setPayStatusName(order.getPayStatusName());
        detail.setPaymentId(order.getPaymentId());
        detail.setPaySuccessTime(order.getPaySuccessTime());
        detail.setAgentUserId(order.getAgentUserId());
        detail.setAgentUserNickname(order.getAgentUserNickname());
        detail.setAgentUserMobile(order.getAgentUserMobile());
        detail.setCanRetryVirtualDelivery(order.getCanRetryVirtualDelivery());
        detail.setCanReceive(order.getCanReceive());
        detail.setCreateTime(order.getCreateTime());
        detail.setUpdateTime(order.getUpdateTime());
        detail.setDeliveryTime(order.getDeliveryTime());
        detail.setReceiveTime(order.getReceiveTime());
        return detail;
    }

    private BrokerageRecordBizDetailRespVO buildBizDetailBase(BrokerageRecordDO record) {
        BrokerageRecordBizDetailRespVO respVO = new BrokerageRecordBizDetailRespVO();
        respVO.setRecordId(record.getId() != null ? record.getId().longValue() : null);
        respVO.setUserId(record.getUserId());
        respVO.setBizId(record.getBizId());
        respVO.setBizType(record.getBizType());
        respVO.setBizCategory(record.getBizCategory());
        respVO.setTitle(record.getTitle());
        respVO.setDescription(record.getDescription());
        respVO.setPrice(record.getPrice());
        respVO.setTotalPrice(record.getTotalPrice());
        respVO.setStatus(record.getStatus());
        respVO.setCreateTime(record.getCreateTime());
        return respVO;
    }

    private boolean isMbRestockBiz(Integer bizType) {
        return Objects.equals(bizType, BrokerageRecordBizTypeEnum.MB_MEMBER_RESTOCK_INCOME.getType())
            || Objects.equals(bizType, BrokerageRecordBizTypeEnum.MB_HQ_RESTOCK_FUND.getType());
    }

    private Long parseBizId(String bizId) {
        if (StrUtil.isBlank(bizId)) {
            return null;
        }
        try {
            return Long.parseLong(bizId);
        } catch (NumberFormatException ex) {
            log.warn("[parseBizId][bizId({}) 非法]", bizId);
            return null;
        }
    }

    private String getOrderStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        for (TradeOrderStatusEnum value : TradeOrderStatusEnum.values()) {
            if (Objects.equals(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return "未知";
    }

    private String getRefundStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        for (TradeOrderRefundStatusEnum value : TradeOrderRefundStatusEnum.values()) {
            if (Objects.equals(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return "未知";
    }

    private String getAfterSaleStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        for (TradeOrderItemAfterSaleStatusEnum value : TradeOrderItemAfterSaleStatusEnum.values()) {
            if (Objects.equals(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return "未知";
    }

    private String getDeliveryTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        for (DeliveryTypeEnum value : DeliveryTypeEnum.values()) {
            if (Objects.equals(value.getType(), type)) {
                return value.getName();
            }
        }
        return "未知";
    }

    private String formatSkuProperties(List<TradeOrderItemDO.Property> properties) {
        if (CollUtil.isEmpty(properties)) {
            return null;
        }
        return properties.stream()
            .filter(Objects::nonNull)
            .map(property -> {
                String name = StrUtil.nullToDefault(property.getPropertyName(), "");
                String value = StrUtil.nullToDefault(property.getValueName(), "");
                if (StrUtil.isBlank(name) && StrUtil.isBlank(value)) {
                    return null;
                }
                if (StrUtil.isBlank(name)) {
                    return value;
                }
                if (StrUtil.isBlank(value)) {
                    return name;
                }
                return name + ":" + value;
            })
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.joining(" / "));
    }
}
