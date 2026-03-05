package com.hissp.distribution.module.trade.service.brokerage;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelRetryReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelTransferRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawExportReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceImportExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawFinanceImportRespBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 佣金提现 Service 接口
 *
 * @author 芋道源码
 */
public interface BrokerageWithdrawService {

    /**
     * 【管理员】审核佣金提现
     *
     * @param id          佣金编号
     * @param status      审核状态
     * @param auditReason 驳回原因
     * @param userIp 操作 IP
     */
    void auditBrokerageWithdraw(Long id, BrokerageWithdrawStatusEnum status, String auditReason, String userIp);

    /**
     * 获得佣金提现
     *
     * @param id 编号
     * @return 佣金提现
     */
    BrokerageWithdrawDO getBrokerageWithdraw(Long id);

    /**
     * 获得佣金提现分页
     *
     * @param pageReqVO 分页查询
     * @return 佣金提现分页
     */
    PageResult<BrokerageWithdrawDO> getBrokerageWithdrawPage(BrokerageWithdrawPageReqVO pageReqVO);

    /**
     * 获得佣金提现列表（用于导出）
     *
     * @param exportReqVO 查询条件
     * @return 佣金提现列表
     */
    List<BrokerageWithdrawDO> getBrokerageWithdrawList(BrokerageWithdrawExportReqVO exportReqVO);

    /**
     * 【会员】创建佣金提现
     *
     * @param userId      会员用户编号
     * @param createReqVO 创建信息
     * @return 佣金提现编号
     */
    Long createBrokerageWithdraw(Long userId, AppBrokerageWithdrawCreateReqVO createReqVO);

    /**
     * 【API】更新佣金提现的转账结果
     *
     * 目前用于支付回调，标记提现转账结果
     *
     * @param id 提现编号
     * @param payTransferId 转账订单编号
     */
    void updateBrokerageWithdrawTransferred(Long id, Long payTransferId);

    /**
     * 财务确认打款
     *
     * @param id     提现编号
     * @param remark 备注
     */
    void confirmFinancePay(Long id, String remark);

    /**
     * 发起渠道打款
     *
     * @param id 提现编号
     */
    void channelPay(Long id);

    /**
     * 判断用户在指定提现类型下，是否具备渠道打款能力
     *
     * @param userId 用户编号
     * @param withdrawType 提现类型
     * @return 是否可用
     */
    boolean isChannelPayEnabled(Long userId, Integer withdrawType);

    /**
     * 导入财务打款结果
     *
     * @param list Excel 数据
     * @return 导入结果
     */
    BrokerageWithdrawFinanceImportRespBO importFinanceResult(List<BrokerageWithdrawFinanceImportExcelVO> list);

    /**
     * 按照 userId，汇总每个用户的提现
     *
     * @param userIds 用户编号
     * @param status  提现状态
     * @return 用户提现汇总 List
     */
    List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                        Collection<BrokerageWithdrawStatusEnum> status);

    /**
     * 按照 userId，汇总每个用户的提现
     *
     * @param userIds 用户编号
     * @param status  提现状态
     * @return 用户提现汇总 Map
     */
    default Map<Long, BrokerageWithdrawSummaryRespBO> getWithdrawSummaryMapByUserId(Set<Long> userIds,
                                                                                    Collection<BrokerageWithdrawStatusEnum> status) {
        return convertMap(getWithdrawSummaryListByUserId(userIds, status), BrokerageWithdrawSummaryRespBO::getUserId);
    }

    /**
     * 【会员】获取最近使用的提现账户信息
     *
     * @param userId 会员用户编号
     * @return 最近使用的提现账户列表，按时间倒序去重
     */
    List<BrokerageWithdrawDO> getLatestWithdrawAccounts(Long userId);

    /**
     * 渠道打款详情
     *
     * @param id 提现编号
     */
    BrokerageWithdrawChannelTransferRespVO getChannelTransferDetail(Long id);

    /**
     * 手动触发渠道打款同步
     *
     * @param id 提现编号
     */
    BrokerageWithdrawChannelTransferRespVO syncChannelTransfer(Long id);

    /**
     * 重新发起渠道打款
     *
     * @param reqVO 请求
     */
    BrokerageWithdrawChannelTransferRespVO retryChannelPay(BrokerageWithdrawChannelRetryReqVO reqVO);

    /**
     * 是否已存在渠道打款记录
     *
     * @param id 提现编号
     */
    boolean hasChannelTransferRecord(Long id);

}
