package com.hissp.distribution.module.trade.service.brokerage;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.common.enums.UserTypeEnum;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.framework.common.util.number.MoneyUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.hissp.distribution.module.pay.api.settle.PaySettleAccountApi;
import com.hissp.distribution.module.pay.api.settle.dto.PaySettleAccountRespDTO;
import com.hissp.distribution.module.pay.api.transfer.PayTransferApi;
import com.hissp.distribution.module.pay.api.transfer.constant.PayeaseTransferConstants;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferRespDTO;
import com.hissp.distribution.module.pay.api.wallet.PayWalletApi;
import com.hissp.distribution.module.pay.api.wallet.dto.PayWalletAddBalanceReqDTO;
import com.hissp.distribution.module.pay.enums.transfer.PayTransferStatusEnum;
import com.hissp.distribution.module.pay.enums.transfer.PayTransferTypeEnum;
import com.hissp.distribution.module.pay.enums.wallet.PayWalletBizTypeEnum;
import com.hissp.distribution.module.system.api.notify.NotifyMessageSendApi;
import com.hissp.distribution.module.system.api.social.SocialUserApi;
import com.hissp.distribution.module.system.api.social.dto.SocialUserRespDTO;
import com.hissp.distribution.module.system.enums.social.SocialTypeEnum;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelRetryReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelTransferRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelTransferStageRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawExportReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceImportExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageWithdrawConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.hissp.distribution.module.trade.dal.dataobject.config.TradeConfigDO;
import com.hissp.distribution.module.trade.dal.mysql.brokerage.BrokerageWithdrawMapper;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import com.hissp.distribution.module.trade.framework.order.config.TradeOrderProperties;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawFinanceImportRespBO;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawSummaryRespBO;
import com.hissp.distribution.module.trade.service.config.TradeConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hissp.distribution.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.*;

/**
 * 佣金提现 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
@Slf4j
public class BrokerageWithdrawServiceImpl implements BrokerageWithdrawService {

    @Resource
    private BrokerageWithdrawMapper brokerageWithdrawMapper;

    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private TradeConfigService tradeConfigService;
    @Resource
    private NotifyMessageSendApi notifyMessageSendApi;
    @Resource
    private PayTransferApi payTransferApi;
    @Resource
    private SocialUserApi socialUserApi;
    @Resource
    private PayWalletApi payWalletApi;

    @Resource
    private PaySettleAccountApi paySettleAccountApi;

    @Resource
    private Validator validator;

    @Resource
    private TradeOrderProperties tradeOrderProperties;

    private static final Set<BrokerageWithdrawStatusEnum> FINANCE_ALLOWED_TARGET_STATUSES =
            new HashSet<>(Arrays.asList(
                    BrokerageWithdrawStatusEnum.WITHDRAW_SUBMIT_SUCCESS,
                    BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS,
                    BrokerageWithdrawStatusEnum.WITHDRAW_FAIL
            ));
    private static final Set<Integer> ACCOUNT_RECORD_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    BrokerageWithdrawTypeEnum.BANK.getType(),
                    BrokerageWithdrawTypeEnum.WECHAT.getType(),
                    BrokerageWithdrawTypeEnum.ALIPAY.getType()
            )));
    private static final String PAYEASE_DEFAULT_EMAIL = "wangzhang367@example.com";
    private static final TypeReference<Map<String, Object>> STAGE_TYPE_REFERENCE =
            new TypeReference<Map<String, Object>>() {};

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditBrokerageWithdraw(Long id, BrokerageWithdrawStatusEnum status, String auditReason, String userIp) {
        // 1.1 校验存在
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        // 1.2 校验状态为审核中
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.AUDITING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 2. 更新状态
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(id, BrokerageWithdrawStatusEnum.AUDITING.getStatus(),
                new BrokerageWithdrawDO().setStatus(status.getStatus()).setAuditReason(auditReason).setAuditTime(LocalDateTime.now()));
        if (rows == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_AUDITING);
        }

        // 3.1 审批通过的后续处理
        if (BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.equals(status)) {
            auditBrokerageWithdrawSuccess(withdraw);
            // 3.2 审批不通过的后续处理
        } else if (BrokerageWithdrawStatusEnum.AUDIT_FAIL.equals(status)) {
            brokerageRecordService.updateBrokerageImmediately(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        } else {
            throw new IllegalArgumentException("不支持的提现状态：" + status);
        }
    }

    private void auditBrokerageWithdrawSuccess(BrokerageWithdrawDO withdraw) {
        // 1.1 钱包
        if (BrokerageWithdrawTypeEnum.WALLET.getType().equals(withdraw.getType())) {
            payWalletApi.addWalletBalance(new PayWalletAddBalanceReqDTO()
                    .setUserId(withdraw.getUserId()).setUserType(UserTypeEnum.MEMBER.getValue())
                    .setBizType(PayWalletBizTypeEnum.BROKERAGE_WITHDRAW.getType()).setBizId(withdraw.getId().toString())
                    .setPrice(withdraw.getPrice()));
            // 1.2 微信 API：自动发起渠道打款
        } else if (BrokerageWithdrawTypeEnum.WECHAT_API.getType().equals(withdraw.getType())) {
            createPayTransfer(withdraw);
            brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(),
                    BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                    new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus()));
            // 1.3 银行卡：默认进入财务打款流程，可在页面发起渠道打款
        } else if (BrokerageWithdrawTypeEnum.BANK.getType().equals(withdraw.getType())) {
            brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(),
                    BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
                    new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus()));
            // 1.3 剩余类型，都是手动打款，所以不处理
        } else {
            // TODO 可优化：未来可以考虑，接入支付宝、银联等 API 转账，实现自动打款
            log.info("[auditBrokerageWithdrawSuccess][withdraw({}) 类型({}) 手动打款，无需处理]", withdraw.getId(), withdraw.getType());
        }
        // 2. 非支付 API，则直接体现成功
        //if (!BrokerageWithdrawTypeEnum.isApi(withdraw.getType())) {
        //    brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus(),
        //            new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus()));
        //}
    }

    private Long createPayTransfer(BrokerageWithdrawDO withdraw) {
        String channelCode = StrUtil.blankToDefault(tradeOrderProperties.getWithdrawTransferChannelCode(), "wx_lite");
        PayTransferTypeEnum transferType = determineTransferType(channelCode);
        PayTransferCreateReqDTO payTransferCreateReqDTO = new PayTransferCreateReqDTO()
                .setAppKey(tradeOrderProperties.getPayAppKey())
                .setChannelCode(channelCode).setType(transferType.getType())
                .setMerchantTransferId(withdraw.getId().toString())
                .setPrice(withdraw.getPrice())
                .setSubject("佣金提现")
                .setUserIp(getClientIP());
        if (PayTransferTypeEnum.WX_BALANCE.equals(transferType)) {
            SocialUserRespDTO socialUser = socialUserApi.getSocialUserByUserId(
                    UserTypeEnum.MEMBER.getValue(), withdraw.getUserId(), SocialTypeEnum.WECHAT_MINI_APP.getType());
            if (socialUser == null) {
                throw exception(BROKERAGE_WITHDRAW_ACCOUNT_REQUIRED);
            }
            payTransferCreateReqDTO.setOpenid(socialUser.getOpenid());
        } else if (PayTransferTypeEnum.PAYEASE_ACCOUNT.equals(transferType)) {
            PaySettleAccountRespDTO account = getApprovedAccountOrThrow(withdraw.getUserId());
            // 使用子商户号作为收款方编码，便于渠道直接定位账户
            payTransferCreateReqDTO.setReceiverId(account.getSubMerchantId())
                    .setReceiverType("MERCHANT_ACC")
                    .setCurrency("CNY")
                    .setRemark("佣金提现")
                    .setChannelExtras(buildPayeaseExtras(account));
        }
        return payTransferApi.createTransfer(payTransferCreateReqDTO);
    }

    private PayTransferTypeEnum determineTransferType(String channelCode) {
        if (StrUtil.isNotBlank(channelCode) && channelCode.startsWith("payease")) {
            return PayTransferTypeEnum.PAYEASE_ACCOUNT;
        }
        if (StrUtil.isNotBlank(channelCode) && channelCode.startsWith("alipay")) {
            return PayTransferTypeEnum.ALIPAY_BALANCE;
        }
        return PayTransferTypeEnum.WX_BALANCE;
    }

    private boolean supportsChannelPay(Integer withdrawType) {
        return Objects.equals(withdrawType, BrokerageWithdrawTypeEnum.BANK.getType())
                || Objects.equals(withdrawType, BrokerageWithdrawTypeEnum.WECHAT_API.getType());
    }

    private Map<String, String> buildPayeaseExtras(PaySettleAccountRespDTO account) {
        Map<String, String> extras = new HashMap<>(12);
        extras.put(PayeaseTransferConstants.SUB_MERCHANT_ID, account.getSubMerchantId());
        extras.put(PayeaseTransferConstants.BANK_ACCOUNT_NO, account.getBankAccountNo());
        extras.put(PayeaseTransferConstants.BANK_ACCOUNT_NAME, account.getBankAccountName());
        extras.put(PayeaseTransferConstants.BANK_NAME, account.getBankName());
        extras.put(PayeaseTransferConstants.BANK_BRANCH_NAME, account.getBankBranchName());
        extras.put(PayeaseTransferConstants.PROVINCE_CODE, account.getProvinceCode());
        extras.put(PayeaseTransferConstants.CITY_CODE, account.getCityCode());
        extras.put(PayeaseTransferConstants.AREA_CODE, account.getAreaCode());
        extras.put(PayeaseTransferConstants.ADDRESS, account.getAddress());
        extras.put(PayeaseTransferConstants.RECEIVER_ADDRESS,
                StrUtil.blankToDefault(account.getReceiverAddress(), account.getAddress()));
        extras.put(PayeaseTransferConstants.MOBILE, account.getMobile());
        extras.put(PayeaseTransferConstants.EMAIL,
                StrUtil.blankToDefault(account.getEmail(), PAYEASE_DEFAULT_EMAIL));
        return extras;
    }

    private PaySettleAccountRespDTO getApprovedAccountOrThrow(Long userId) {
        PaySettleAccountRespDTO account = paySettleAccountApi.getApprovedAccount(userId);
        if (account == null || StrUtil.isBlank(account.getSubMerchantId())) {
            throw exception(BROKERAGE_WITHDRAW_ACCOUNT_REQUIRED);
        }
        return account;
    }

    private BrokerageWithdrawDO validateBrokerageWithdrawExists(Long id) {
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(id);
        if (withdraw == null) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return withdraw;
    }

    @Override
    public BrokerageWithdrawDO getBrokerageWithdraw(Long id) {
        return brokerageWithdrawMapper.selectById(id);
    }

    @Override
    public PageResult<BrokerageWithdrawDO> getBrokerageWithdrawPage(BrokerageWithdrawPageReqVO pageReqVO) {
        return brokerageWithdrawMapper.selectPage(pageReqVO);
    }

    @Override
    public List<BrokerageWithdrawDO> getBrokerageWithdrawList(BrokerageWithdrawExportReqVO exportReqVO) {
        return brokerageWithdrawMapper.selectList(exportReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrokerageWithdraw(Long userId, AppBrokerageWithdrawCreateReqVO createReqVO) {
        // 1.1 校验提现金额
        TradeConfigDO tradeConfig = validateWithdrawPrice(createReqVO.getPrice());

        // 1.2 校验提现参数
        createReqVO.validate(validator);

        // 2.1 计算手续费
        Integer feePrice = calculateFeePrice(createReqVO.getPrice(), tradeConfig.getBrokerageWithdrawFeePercent());
        // 2.2 创建佣金提现记录
        BrokerageWithdrawDO withdraw = BrokerageWithdrawConvert.INSTANCE.convert(createReqVO, userId, feePrice);
        brokerageWithdrawMapper.insert(withdraw);

        // 3. 创建用户佣金记录
        // 注意，佣金是否充足，reduceBrokerage 已经进行校验
        brokerageRecordService.reduceBrokerage(userId, BrokerageRecordBizTypeEnum.WITHDRAW, String.valueOf(withdraw.getId()),
                createReqVO.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW.getTitle());
        return withdraw.getId();
    }

    /**
     * 计算提现手续费
     *
     * @param withdrawPrice 提现金额
     * @param percent       手续费百分比
     * @return 提现手续费
     */
    private Integer calculateFeePrice(Integer withdrawPrice, Integer percent) {
        Integer feePrice = 0;
        if (percent != null && percent > 0) {
            feePrice = MoneyUtils.calculateRatePrice(withdrawPrice, Double.valueOf(percent));
        }
        return feePrice;
    }

    /**
     * 校验提现金额要求
     *
     * @param withdrawPrice 提现金额
     * @return 分销配置
     */
    private TradeConfigDO validateWithdrawPrice(Integer withdrawPrice) {
        TradeConfigDO tradeConfig = tradeConfigService.getTradeConfig();
        if (tradeConfig.getBrokerageWithdrawMinPrice() != null && withdrawPrice < tradeConfig.getBrokerageWithdrawMinPrice()) {
            throw exception(BROKERAGE_WITHDRAW_MIN_PRICE, MoneyUtils.fenToYuanStr(tradeConfig.getBrokerageWithdrawMinPrice()));
        }
        return tradeConfig;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrokerageWithdrawTransferred(Long id, Long payTransferId) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        PayTransferRespDTO transfer = payTransferApi.getTransfer(payTransferId);
        // TODO @luchi：建议参考支付那，即使成功的情况下，也要各种校验；金额是否匹配、转账单号是否匹配、是否重复调用；
        if (PayTransferStatusEnum.isSuccess(transfer.getStatus())) {
            withdraw.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUCCESS.getStatus());
            // TODO @luchi：发送站内信
        } else if (PayTransferStatusEnum.isPendingStatus(transfer.getStatus())) {
            // TODO @luchi：这里，是不是不用更新哈？
            withdraw.setStatus(BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus());
        } else {
            withdraw.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.getStatus());
            // 3.2 驳回时需要退还用户佣金
            brokerageRecordService.updateBrokerageImmediately(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        }
        brokerageWithdrawMapper.updateById(withdraw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmFinancePay(Long id, String remark) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus(), withdraw.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_FINANCE_PAYING);
        }
        BrokerageWithdrawDO updateObj = new BrokerageWithdrawDO()
                .setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_SUBMIT_SUCCESS.getStatus());
        if (StrUtil.isNotBlank(remark)) {
            updateObj.setRemark(remark);
        }
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(id,
                BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus(), updateObj);
        if (rows == 0) {
            throw exception(BROKERAGE_WITHDRAW_STATUS_NOT_FINANCE_PAYING);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void channelPay(Long id) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        if (!supportsChannelPay(withdraw.getType())) {
            throw exception(BROKERAGE_WITHDRAW_CHANNEL_PAY_UNSUPPORTED);
        }
        Integer status = withdraw.getStatus();
        if (!Objects.equals(status, BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus())
                && !Objects.equals(status, BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus())) {
            throw exception(BROKERAGE_WITHDRAW_CHANNEL_PAY_STATUS_INVALID);
        }
        createPayTransfer(withdraw);
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(withdraw.getId(), status,
                new BrokerageWithdrawDO().setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus()));
        if (rows == 0) {
            throw exception(BROKERAGE_WITHDRAW_CHANNEL_PAY_STATUS_INVALID);
        }
    }

    @Override
    public boolean isChannelPayEnabled(Long userId, Integer withdrawType) {
        if (!supportsChannelPay(withdrawType)) {
            return false;
        }
        PaySettleAccountRespDTO account = paySettleAccountApi.getApprovedAccount(userId);
        return account != null && StrUtil.isNotBlank(account.getSubMerchantId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BrokerageWithdrawFinanceImportRespBO importFinanceResult(List<BrokerageWithdrawFinanceImportExcelVO> list) {
        BrokerageWithdrawFinanceImportRespBO respBO = BrokerageWithdrawFinanceImportRespBO.builder()
                .successCount(0).failureCount(0).failureReasons(new ArrayList<>()).build();
        if (CollUtil.isEmpty(list)) {
            return respBO;
        }
        for (int i = 0; i < list.size(); i++) {
            BrokerageWithdrawFinanceImportExcelVO row = list.get(i);
            String error = handleFinanceImportRow(row);
            if (error != null) {
                respBO.addFailure(String.format("第 %d 行：%s", i + 2, error));
                continue;
            }
            respBO.incrementSuccess();
        }
        return respBO;
    }

    private String handleFinanceImportRow(BrokerageWithdrawFinanceImportExcelVO row) {
        if (row.getId() == null) {
            return "提现编号不能为空";
        }
        if (row.getFinanceStatus() == null) {
            return "打款状态不能为空";
        }
        BrokerageWithdrawStatusEnum targetStatus = getByStatus(row.getFinanceStatus());
        if (targetStatus == null) {
            return "打款状态不正确";
        }
        if (!FINANCE_ALLOWED_TARGET_STATUSES.contains(targetStatus)) {
            return "该打款状态暂不支持批量导入";
        }
        BrokerageWithdrawDO withdraw = brokerageWithdrawMapper.selectById(row.getId());
        if (withdraw == null) {
            return "提现记录不存在";
        }
        if (ObjectUtil.notEqual(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus(), withdraw.getStatus())) {
            return "提现记录状态不是财务打款中";
        }
        BrokerageWithdrawDO updateObj = new BrokerageWithdrawDO()
                .setStatus(targetStatus.getStatus());
        if (StrUtil.isNotBlank(row.getRemark())) {
            updateObj.setRemark(row.getRemark());
        }
        int rows = brokerageWithdrawMapper.updateByIdAndStatus(row.getId(),
                BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus(), updateObj);
        if (rows == 0) {
            return "提现状态已变化，请刷新后重试";
        }
        if (BrokerageWithdrawStatusEnum.WITHDRAW_FAIL.equals(targetStatus)) {
            brokerageRecordService.updateBrokerageImmediately(withdraw.getUserId(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT,
                    String.valueOf(withdraw.getId()), withdraw.getPrice(), BrokerageRecordBizTypeEnum.WITHDRAW_REJECT.getTitle());
        }
        return null;
    }

    private BrokerageWithdrawStatusEnum getByStatus(Integer status) {
        for (BrokerageWithdrawStatusEnum value : BrokerageWithdrawStatusEnum.values()) {
            if (ObjectUtil.equal(value.getStatus(), status)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public List<BrokerageWithdrawSummaryRespBO> getWithdrawSummaryListByUserId(Collection<Long> userIds,
                                                                               Collection<BrokerageWithdrawStatusEnum> statuses) {
        if (CollUtil.isEmpty(userIds) || CollUtil.isEmpty(statuses)) {
            return Collections.emptyList();
        }
        return brokerageWithdrawMapper.selectCountAndSumPriceByUserIdAndStatus(userIds,
                convertSet(statuses, BrokerageWithdrawStatusEnum::getStatus));
    }

    @Override
    public BrokerageWithdrawChannelTransferRespVO getChannelTransferDetail(Long id) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        BrokerageWithdrawChannelTransferRespVO respVO = new BrokerageWithdrawChannelTransferRespVO();
        respVO.setWithdrawId(withdraw.getId());
        respVO.setWithdrawStatus(withdraw.getStatus());
        respVO.setWithdrawType(withdraw.getType());
        respVO.setWithdrawPrice(withdraw.getPrice());
        respVO.setChannelPayEnabled(isChannelPayEnabled(withdraw.getUserId(), withdraw.getType()));
        // 前端需要知道是否需要输入口令，直接透出配置开关
        respVO.setChannelRetryPasswordRequired(StrUtil.isNotBlank(tradeOrderProperties.getAdminRefundPassword()));
        PayTransferRespDTO transfer = payTransferApi.getTransferByMerchantTransferId(
                tradeOrderProperties.getPayAppKey(), withdraw.getId().toString());
        if (transfer != null) {
            respVO.setTransfer(buildTransferDetail(transfer));
        }
        return respVO;
    }

    @Override
    /**
     * 立即同步渠道转账状态：调用 pay 模块查询渠道，并返回最新详情。
     */
    public BrokerageWithdrawChannelTransferRespVO syncChannelTransfer(Long id) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(id);
        PayTransferRespDTO transfer = payTransferApi.getTransferByMerchantTransferId(
                tradeOrderProperties.getPayAppKey(), withdraw.getId().toString());
        if (transfer == null) {
            throw exception(BROKERAGE_WITHDRAW_CHANNEL_TRANSFER_NOT_FOUND);
        }
        // 触发 pay 模块的同步后，再次查询最新数据返回给前端
        payTransferApi.syncTransfer(transfer.getId());
        return getChannelTransferDetail(id);
    }

    @Override
    /**
     * 重新发起渠道打款：可选口令校验，通过后再次调用 channelPay。
     */
    public BrokerageWithdrawChannelTransferRespVO retryChannelPay(BrokerageWithdrawChannelRetryReqVO reqVO) {
        BrokerageWithdrawDO withdraw = validateBrokerageWithdrawExists(reqVO.getId());
        validateChannelRetryPassword(reqVO.getPassword());
        channelPay(withdraw.getId());
        return getChannelTransferDetail(withdraw.getId());
    }

    @Override
    /**
     * 判断是否存在渠道转账记录，用于前端隐藏再发入口。
     */
    public boolean hasChannelTransferRecord(Long id) {
        PayTransferRespDTO transfer = payTransferApi.getTransferByMerchantTransferId(
                tradeOrderProperties.getPayAppKey(), id.toString());
        return transfer != null;
    }

    private void validateChannelRetryPassword(String password) {
        String pwd = tradeOrderProperties.getAdminRefundPassword();
        if (StrUtil.isBlank(pwd)) {
            return;
        }
        if (StrUtil.isBlank(password) || !StrUtil.equals(pwd, password)) {
            throw exception(BROKERAGE_WITHDRAW_CHANNEL_PASSWORD_INVALID);
        }
    }

    /**
     * 将 pay-transfer 聚合成渠道打款视图，便于前端展示。
     */
    private BrokerageWithdrawChannelTransferRespVO.TransferDetail buildTransferDetail(PayTransferRespDTO transfer) {
        BrokerageWithdrawChannelTransferRespVO.TransferDetail detail =
                new BrokerageWithdrawChannelTransferRespVO.TransferDetail();
        detail.setId(transfer.getId());
        detail.setStatus(transfer.getStatus());
        detail.setStatusName(getTransferStatusName(transfer.getStatus()));
        detail.setPrice(transfer.getPrice());
        detail.setChannelTransferNo(transfer.getChannelTransferNo());
        detail.setChannelErrorCode(transfer.getChannelErrorCode());
        detail.setChannelErrorMsg(transfer.getChannelErrorMsg());
        detail.setSuccessTime(transfer.getSuccessTime());
        detail.setCreateTime(transfer.getCreateTime());
        detail.setUpdateTime(transfer.getUpdateTime());
        detail.setChannelExtras(transfer.getChannelExtras());
        detail.setChannelNotifyData(transfer.getChannelNotifyData());
        detail.setTransferStage(buildStage(transfer.getChannelNotifyData(), "transfer", "账户划拨"));
        detail.setWithdrawStage(buildStage(transfer.getChannelNotifyData(), "withdraw", "银行卡出款"));
        return detail;
    }

    private String getTransferStatusName(Integer status) {
        for (PayTransferStatusEnum value : PayTransferStatusEnum.values()) {
            if (Objects.equals(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return "未知";
    }

    @SuppressWarnings("unchecked")
    /**
     * 解析渠道 notify JSON 中的阶段信息。
     */
    private BrokerageWithdrawChannelTransferStageRespVO buildStage(
            String notifyData, String key, String stageLabel) {
        if (StrUtil.isBlank(notifyData)) {
            return null;
        }
        Map<String, Object> root = JsonUtils.parseObjectQuietly(notifyData, STAGE_TYPE_REFERENCE);
        if (root == null) {
            return null;
        }
        Object stageObj = root.get(key);
        if (!(stageObj instanceof Map)) {
            return null;
        }
        // 渠道回调阶段原文为 Map，直接透传字段便于可视化
        Map<String, Object> stageMap = (Map<String, Object>) stageObj;
        BrokerageWithdrawChannelTransferStageRespVO vo = new BrokerageWithdrawChannelTransferStageRespVO();
        String status = MapUtil.getStr(stageMap, "status");
        vo.setStage(stageLabel);
        vo.setStatus(status);
        vo.setStatusName(buildStageStatusName(status, stageLabel));
        vo.setChannelTransferNo(StrUtil.emptyToDefault(MapUtil.getStr(stageMap, "serialNumber"),
                MapUtil.getStr(stageMap, "orderId")));
        vo.setRequestId(MapUtil.getStr(stageMap, "requestId"));
        String errorCode = StrUtil.emptyToDefault(MapUtil.getStr(stageMap, "errorCode"),
                MapUtil.getStr(stageMap, "code"));
        vo.setErrorCode(errorCode);
        vo.setErrorMessage(StrUtil.emptyToDefault(MapUtil.getStr(stageMap, "errorMessage"),
                MapUtil.getStr(stageMap, "message")));
        vo.setSuccessTime(parseChannelTime(MapUtil.getStr(stageMap, "completeDateTime")));
        vo.setRaw(stageMap);
        return vo;
    }

    private String buildStageStatusName(String status, String stageLabel) {
        if (StrUtil.equalsIgnoreCase("SUCCESS", status)) {
            return stageLabel + "成功";
        }
        if (StrUtil.equalsAnyIgnoreCase(status, "FAILED", "ERROR", "CANCEL")) {
            return stageLabel + "失败";
        }
        return stageLabel + "处理中";
    }

    private LocalDateTime parseChannelTime(String timeStr) {
        if (StrUtil.isBlank(timeStr)) {
            return null;
        }
        try {
            return LocalDateTimeUtil.parse(timeStr, DatePattern.NORM_DATETIME_PATTERN);
        } catch (Exception ex) {
            log.warn("[parseChannelTime][解析渠道时间({})失败]", timeStr, ex);
            return null;
        }
    }

    @Override
    public List<BrokerageWithdrawDO> getLatestWithdrawAccounts(Long userId) {
        // 账号信息仅对手动打款方式有效
        List<BrokerageWithdrawDO> withdrawList =
                brokerageWithdrawMapper.selectLatestListByUserId(userId, ACCOUNT_RECORD_TYPES);
        if (CollUtil.isEmpty(withdrawList)) {
            return Collections.emptyList();
        }
        // 取每种提现方式最近一条记录
        Map<Integer, BrokerageWithdrawDO> latestMap = new LinkedHashMap<>();
        for (BrokerageWithdrawDO withdraw : withdrawList) {
            latestMap.putIfAbsent(withdraw.getType(), withdraw);
        }
        return new ArrayList<>(latestMap.values());
    }
}
