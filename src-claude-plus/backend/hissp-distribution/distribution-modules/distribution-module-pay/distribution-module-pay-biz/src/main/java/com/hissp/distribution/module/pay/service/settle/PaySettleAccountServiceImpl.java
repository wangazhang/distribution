package com.hissp.distribution.module.pay.service.settle;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.hissp.distribution.framework.common.enums.UserTypeEnum;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.pay.core.client.PayClient;
import com.hissp.distribution.framework.pay.core.client.impl.payease.PayeaseAccountPayClient;
import com.hissp.distribution.framework.pay.core.client.impl.payease.PayeaseSftpService;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseDeclarationResult;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseModifyBankCardDTO;
import com.hissp.distribution.framework.pay.core.client.impl.payease.dto.PayeaseSubMerchantDeclareDTO;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountPageReqVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountChangeCardReqVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountSaveReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.mysql.account.PaySettleAccountMapper;
import com.hissp.distribution.module.pay.enums.payease.PayeaseSubMerchantStatusEnum;
import com.hissp.distribution.module.pay.framework.pay.config.PayProperties;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.channel.PayChannelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.pay.enums.ErrorCodeConstants.*;

@Service
@Slf4j
public class PaySettleAccountServiceImpl implements PaySettleAccountService {

    @Resource
    private PaySettleAccountMapper settleAccountMapper;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayProperties payProperties;
    @Resource
    private PayeaseSftpService payeaseSftpService;
    @Resource
    private PayAppService payAppService;

    @Resource
    PaySettleAccountService settleAccountService;

    @Override
    public PaySettleAccountDO get(Long id) {
        return settleAccountMapper.selectById(id);
    }

    @Override
    public PaySettleAccountDO getByUser(Long userId) {
        return settleAccountMapper.selectByUserId(userId);
    }

    @Override
    public PageResult<PaySettleAccountDO> getPage(SettleAccountPageReqVO pageReqVO) {
        return settleAccountMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveDraft(Long userId, AppSettleAccountSaveReqVO reqVO) {
        PaySettleAccountDO exist = settleAccountMapper.selectByUserId(userId);
        PaySettleAccountDO data = buildDO(userId, reqVO);
        syncAttachments(data, exist);
        if (exist == null) {
            settleAccountMapper.insert(data.setStatus(PayeaseSubMerchantStatusEnum.DRAFT.getStatus()));
            return data.getId();
        }
        data.setId(exist.getId());
        data.setStatus(PayeaseSubMerchantStatusEnum.DRAFT.getStatus());
        data.setRequestId(exist.getRequestId());
        data.setSubMerchantId(exist.getSubMerchantId());
        settleAccountMapper.updateById(data);
        return exist.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submit(Long userId) {
        PaySettleAccountDO account = getRequired(userId);
        submitInternal(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminSubmit(Long id) {
        PaySettleAccountDO account = settleAccountMapper.selectById(id);
        if (account == null) {
            throw new ServiceException(CHANNEL_NOT_FOUND.getCode(), "记录不存在");
        }
        submitInternal(account);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveChangeCardDraft(Long userId, AppSettleAccountChangeCardReqVO reqVO) {
        PaySettleAccountDO account = getApprovedRequired(userId);
        PaySettleAccountDO latest = BeanUtil.copyProperties(account, PaySettleAccountDO.class);
        applyBankInfo(latest, reqVO);
        syncAttachments(latest, account);
        settleAccountMapper.updateById(latest);
        return latest.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitChangeCard(Long userId) {
        PaySettleAccountDO account = getApprovedRequired(userId);
        if (StrUtil.isBlank(account.getSubMerchantId())) {
            throw exception(SETTLE_ACCOUNT_CHANNEL_LOST);
        }
        String requestId = IdUtil.getSnowflakeNextIdStr();
        PayeaseModifyBankCardDTO dto = toModifyBankCardDTO(account, requestId);
        PayeaseDeclarationResult result = getClient().modifyBankCard(dto);
        if (!result.isSuccess()) {
            throw exception(CHANNEL_DECLARE_FAIL,
                    StrUtil.blankToDefault(maskPersonalKeyword(result.getErrorMessage()), "渠道换卡失败"));
        }
        settleAccountService.recordStatus(account.getId(), PayeaseSubMerchantStatusEnum.PASS,
                null, StrUtil.blankToDefault(result.getSubMerchantId(), account.getSubMerchantId()),
                requestId);
    }

    @Override
    public void syncStatus(Long id) {
        PaySettleAccountDO account = settleAccountMapper.selectById(id);
        if (account == null || StrUtil.isBlank(account.getRequestId())) {
            return;
        }
        PayeaseAccountPayClient client = getClient();
        PayeaseDeclarationResult result = client.querySubMerchant(account.getRequestId(), account.getSubMerchantId());
        applyResult(account, result);
    }

    @Override
    public PaySettleAccountDO getApprovedAccount(Long userId) {
        PaySettleAccountDO account = settleAccountMapper.selectByUserId(userId);
        if (account == null || !PayeaseSubMerchantStatusEnum.PASS.getStatus().equals(account.getStatus())) {
            return null;
        }
        return account;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleDeclarationNotify(Long channelId, Map<String, String> headers,
                                        Map<String, String> params, String body) {
        PayeaseAccountPayClient client = getClient(channelId);
        PayeaseDeclarationResult result = client.parseDeclarationNotify(headers, params, body);
        processDeclarationReview(result);
    }

    private void submitInternal(PaySettleAccountDO account) {
        //确保资料齐全
        ensureRequired(account);
        //确保附件信息已经设置  应该挪到payease
        syncAttachments(account, account);
        Long channelId = resolveAccountChannelId();
        PayeaseAccountPayClient client = getClient(channelId);
        //有三方ID，或者审核结果是faild,那么在三方ID基础上操作
        boolean isNewApply = PayeaseSubMerchantStatusEnum.REJECT.getStatus().equals(account.getStatus())||
                StrUtil.isBlank(account.getSubMerchantId());
        String operationType = isNewApply ? "CREATE" : "MODIFY";
        String requestId = account.getRequestId();
        if (isNewApply) {
            //清理上一轮问题数据
            settleAccountService.clearProblemRecord(account.getId());
            account.setSubMerchantId(null);
            account.setRequestId(null);
            // 这里每次使用新的来提交，确保不出问题；
            requestId = IdUtil.getSnowflakeNextIdStr();
        }
        log.info("商户【{}】申请【{}】requestId【{}】", account.getUserId(), operationType, requestId);
        // 构建请求参数
        PayeaseSubMerchantDeclareDTO dto = toDeclareDTO(account, requestId, operationType)
                .setNotifyUrl(resolveDeclarationNotifyUrl(channelId));
        PayeaseDeclarationResult result = client.declareSubMerchant(dto);
        if (!result.isSuccess()) {
            //成功失败都需要记录状态
            applyResult(account, result);
            throw exception(CHANNEL_DECLARE_FAIL,
                    StrUtil.blankToDefault(maskPersonalKeyword(result.getErrorMessage()), "渠道申报失败"));
        }
        // 成功申请，必须设置 requestId
        account.setRequestId(requestId);
        //成功失败都需要记录状态
        applyResult(account, result);

    }

    private void applyResult(PaySettleAccountDO account, PayeaseDeclarationResult result) {
        PayeaseSubMerchantStatusEnum targetStatus = resolveDeclareStatus(result);
        String rejectReason = targetStatus == PayeaseSubMerchantStatusEnum.REJECT
                ? StrUtil.blankToDefault(maskPersonalKeyword(result.getErrorMessage()), "渠道申报失败")
                : null;
        settleAccountService.recordStatus(account.getId(), targetStatus, rejectReason, result.getSubMerchantId(),
                result.getRequestId());
    }

    private PaySettleAccountDO getRequired(Long userId) {
        PaySettleAccountDO account = settleAccountMapper.selectByUserId(userId);
        if (account == null) {
            throw exception(SETTLE_ACCOUNT_NOT_FOUND);
        }
        return account;
    }

    private PaySettleAccountDO getApprovedRequired(Long userId) {
        PaySettleAccountDO account = getRequired(userId);
        if (!PayeaseSubMerchantStatusEnum.PASS.getStatus().equals(account.getStatus())) {
            throw exception(SETTLE_ACCOUNT_NOT_APPROVED);
        }
        return account;
    }

    private void ensureRequired(PaySettleAccountDO account) {
        if (StrUtil.hasBlank(account.getSignedName(), account.getMobile(), account.getIdCardNo(),
                account.getBankAccountNo(), account.getBankAccountName(), account.getBankName(),
                account.getProvinceCode(), account.getCityCode(), account.getAreaCode(),
                account.getAddress(), account.getReceiverAddress())) {
            throw new ServiceException(SETTLE_ACCOUNT_NOT_FOUND.getCode(), "资料不完整，请重新保存后提交");
        }
    }

    private PaySettleAccountDO buildDO(Long userId, AppSettleAccountSaveReqVO reqVO) {
        return PaySettleAccountDO.builder()
                .userId(userId)
                .userType(UserTypeEnum.MEMBER.getValue())
                .signedName(reqVO.getSignedName())
                .mobile(reqVO.getMobile())
                .email(StrUtil.blankToDefault(reqVO.getEmail(), "wangzhang367@example.com"))
                .idCardNo(reqVO.getIdCardNo())
                .idCardValidStart(parseDate(reqVO.getIdCardValidStart()))
                .idCardValidEnd(parseDate(reqVO.getIdCardValidEnd()))
                .idCardFrontLocalUrl(reqVO.getIdCardFrontUrl())
                .idCardBackLocalUrl(reqVO.getIdCardBackUrl())
                .bankAccountNo(reqVO.getBankAccountNo())
                .bankAccountName(reqVO.getBankAccountName())
                .bankName(reqVO.getBankName())
                .bankBranchName(reqVO.getBankBranchName())
                .bankCardFrontLocalUrl(reqVO.getBankCardFrontUrl())
                .provinceCode(reqVO.getProvinceCode())
                .cityCode(reqVO.getCityCode())
                .areaCode(reqVO.getAreaCode())
                .address(reqVO.getAddress())
                .receiverAddress(StrUtil.blankToDefault(reqVO.getReceiverAddress(), reqVO.getAddress()))
                .extra(reqVO.getExtra())
                .build();
    }

    private void applyBankInfo(PaySettleAccountDO target, AppSettleAccountChangeCardReqVO reqVO) {
        target.setBankAccountNo(reqVO.getBankAccountNo());
        target.setBankAccountName(reqVO.getBankAccountName());
        target.setBankName(reqVO.getBankName());
        target.setBankBranchName(reqVO.getBankBranchName());
        target.setBankCardFrontLocalUrl(reqVO.getBankCardFrontUrl());
        target.setProvinceCode(reqVO.getProvinceCode());
        target.setCityCode(reqVO.getCityCode());
        target.setAreaCode(reqVO.getAreaCode());
        target.setReceiverAddress(reqVO.getReceiverAddress());
        if (StrUtil.isBlank(target.getEmail())) {
            target.setEmail("wangzhang367@example.com");
        }
    }

    private PayeaseModifyBankCardDTO toModifyBankCardDTO(PaySettleAccountDO account, String requestId) {
        return PayeaseModifyBankCardDTO.builder()
                .requestId(requestId)
                .signedRequestId(account.getRequestId())
                .subMerchantId(account.getSubMerchantId())
                .bankAccountNo(account.getBankAccountNo())
                .bankAccountName(account.getBankAccountName())
                .bankName(account.getBankName())
                .bankBranchName(account.getBankBranchName())
                .provinceCode(account.getProvinceCode())
                .cityCode(account.getCityCode())
                .areaCode(account.getAreaCode())
                .build();
    }

    private LocalDate parseDate(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private void syncAttachments(PaySettleAccountDO latest, PaySettleAccountDO exist) {
        // 同步证件/银行卡附件时，使用并行上传提升性能，避免顺序网络 IO 导致草稿请求超时
        CompletableFuture<String> idFrontFuture = CompletableFuture.supplyAsync(() ->
                uploadIfNeed(latest.getUserId(), latest.getIdCardFrontLocalUrl(),
                        exist == null ? null : exist.getIdCardFrontLocalUrl(),
                        exist == null ? null : exist.getIdCardFrontUrl(),
                        "id_front", latest.getIdCardNo()));
        CompletableFuture<String> idBackFuture = CompletableFuture.supplyAsync(() ->
                uploadIfNeed(latest.getUserId(), latest.getIdCardBackLocalUrl(),
                        exist == null ? null : exist.getIdCardBackLocalUrl(),
                        exist == null ? null : exist.getIdCardBackUrl(),
                        "id_back", latest.getIdCardNo()));
        CompletableFuture<String> bankFuture = CompletableFuture.supplyAsync(() ->
                uploadIfNeed(latest.getUserId(), latest.getBankCardFrontLocalUrl(),
                        exist == null ? null : exist.getBankCardFrontLocalUrl(),
                        exist == null ? null : exist.getBankCardFrontUrl(),
                        "bank_front", latest.getBankAccountNo()));
        try {
            latest.setIdCardFrontUrl(idFrontFuture.get());
            latest.setIdCardBackUrl(idBackFuture.get());
            latest.setBankCardFrontUrl(bankFuture.get());
        } catch (Exception e) {
            log.error("[syncAttachments][upload failed userId={}]", latest.getUserId(), e);
            throw new ServiceException(CHANNEL_NOT_FOUND.getCode(), "附件上传失败，请稍后重试");
        }
    }

    private String uploadIfNeed(Long userId, String localUrl, String existLocalUrl,
                                String existChannelUrl, String tag, String identifier) {
        // localUrl 为 OSS/本地存储地址，existChannelUrl 为首信易已保留的 SFTP 路径；
        // 如果本地地址无变化则沿用旧的渠道地址，避免重复下载上传
        if (StrUtil.isBlank(localUrl)) {
            return existChannelUrl;
        }
        if (StrUtil.equals(localUrl, existLocalUrl) && StrUtil.isNotBlank(existChannelUrl)) {
            return existChannelUrl;
        }
        if (localUrl.startsWith("/")) {
            return localUrl;
        }
        byte[] bytes = HttpUtil.downloadBytes(localUrl);
        String ext = FileUtil.extName(localUrl);
        String safeId = sanitizeIdentifier(identifier, tag);
        String path = StrUtil.format("serviceprovider/{}/{}_{}_{}.{}", userId, tag, safeId,
                System.currentTimeMillis(), StrUtil.blankToDefault(ext, "jpg"));
        return payeaseSftpService.upload(bytes, path);
    }

    private String sanitizeIdentifier(String identifier, String fallback) {
        String value = StrUtil.blankToDefault(identifier, fallback);
        value = value.replaceAll("[^0-9A-Za-z]", "");
        if (value.length() > 32) {
            value = value.substring(0, 32);
        }
        if (StrUtil.isBlank(value)) {
            value = fallback;
        }
        return value;
    }

    /**
     * 创建商户入驻申请核心构建
     */
    private PayeaseSubMerchantDeclareDTO toDeclareDTO(PaySettleAccountDO account, String requestId, String operationType) {
        return PayeaseSubMerchantDeclareDTO.builder()
                .requestId(requestId)
                .operationType(operationType)
                .signedType("BY_SPLIT_BILL")
                .registerRole("NATURAL_PERSON")
                .merchantType("PERSONAL")
                .cerType("ID_CARD")
                .signedName(account.getSignedName())
                .signedShortName(account.getSignedName())
                .mobile(account.getMobile())
                .email(account.getEmail())
                .idCardNo(account.getIdCardNo())
                .idCardValidStart(formatDate(account.getIdCardValidStart()))
                .idCardValidEnd(formatDate(account.getIdCardValidEnd()))
                .idCardFrontPath(account.getIdCardFrontUrl())
                .idCardBackPath(account.getIdCardBackUrl())
                .bankAccountNo(account.getBankAccountNo())
                .bankAccountName(account.getBankAccountName())
                .bankName(account.getBankName())
                .bankBranchName(account.getBankBranchName())
                .bankCardFrontPath(account.getBankCardFrontUrl())
                .provinceCode(account.getProvinceCode())
                .cityCode(account.getCityCode())
                .areaCode(account.getAreaCode())
                .address(account.getAddress())
                .contractReceiverName(account.getSignedName())
                .contractReceiverPhone(account.getMobile())
                .contractReceiverAddress(account.getReceiverAddress())
                .extra(account.getExtra())
                .bankReservedPhone(account.getMobile())
                .build();
    }

    private String resolveDeclarationNotifyUrl() {
        return StrUtil.blankToDefault(payProperties.getAccountDeclarationNotifyUrl(), payProperties.getOrderNotifyUrl());
    }

    private PayeaseAccountPayClient getClient() {
        return getClient(resolveAccountChannelId());
    }

    private PayeaseAccountPayClient getClient(Long channelId) {
        PayClient client = channelService.getPayClient(channelId);
        if (!(client instanceof PayeaseAccountPayClient payeaseAccountPayClient)) {
            throw new ServiceException(CHANNEL_NOT_FOUND.getCode(), "账户服务渠道类型不支持首信易能力");
        }
        return payeaseAccountPayClient;
    }

    private Long resolveAccountChannelId() {
        if (payProperties.getPayeaseAccountChannelId() != null) {
            return payProperties.getPayeaseAccountChannelId();
        }
        String appKey = StrUtil.blankToDefault(payProperties.getDefaultAccountAppKey(), payProperties.getWalletPayAppKey());
        if (StrUtil.isBlank(appKey)) {
            throw new ServiceException(CHANNEL_NOT_FOUND.getCode(), "未设置默认账户服务应用");
        }
        PayAppDO app = payAppService.validPayApp(appKey);
        if (app.getAccountChannelId() == null) {
            throw new ServiceException(APP_ACCOUNT_CHANNEL_NOT_CONFIG.getCode(), "应用未配置账户服务渠道");
        }
        return app.getAccountChannelId();
    }

    private String resolveDeclarationNotifyUrl(Long channelId) {
        String base = StrUtil.blankToDefault(payProperties.getAccountDeclarationNotifyUrl(), payProperties.getOrderNotifyUrl());
        if (StrUtil.isBlank(base)) {
            return null;
        }
        if (StrUtil.contains(base, "{channelId}")) {
            return StrUtil.replace(base, "{channelId}", String.valueOf(channelId));
        }
        return base.endsWith("/") ? base + channelId : base + "/" + channelId;
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : date.toString();
    }

    private void processDeclarationReview(PayeaseDeclarationResult result) {
        if (result == null) {
            return;
        }
        PaySettleAccountDO account = null;
        if (StrUtil.isNotBlank(result.getRequestId())) {
            account = settleAccountMapper.selectByRequestId(result.getRequestId());
        }
        if (account == null && StrUtil.isNotBlank(result.getSubMerchantId())) {
            account = settleAccountMapper.selectBySubMerchantId(result.getSubMerchantId());
        }
        if (account == null) {
            log.warn("[processDeclarationReview][未找到匹配的提现资料记录 requestId={}, subMerchantId={}]", result.getRequestId(), result.getSubMerchantId());
            return;
        }
        PayeaseSubMerchantStatusEnum targetStatus = resolveReviewStatus(result);
        String rejectReason = buildChannelTips(result, targetStatus);
        settleAccountService.recordStatus(account.getId(), targetStatus, rejectReason,
                result.getSubMerchantId(), result.getRequestId());
    }

    private PayeaseSubMerchantStatusEnum resolveDeclareStatus(PayeaseDeclarationResult result) {
        String status = StrUtil.blankToDefault(result.getStatus(), "PROCESSING");
        if (result.isSuccess() || StrUtil.equalsIgnoreCase(status, "PROCESSING")) {
            return PayeaseSubMerchantStatusEnum.AUDITING;
        }
        return PayeaseSubMerchantStatusEnum.REJECT;
    }

    /**
     * 只根据 subMerchantReviewStatus 判定是否通过，postReviewStatus 直接忽略。
     */
    private PayeaseSubMerchantStatusEnum resolveReviewStatus(PayeaseDeclarationResult result) {
        String reviewStatus = StrUtil.blankToDefault(result.getReviewStatus(), result.getStatus());
        if (StrUtil.equalsAnyIgnoreCase(reviewStatus, "PASS", "SUCCESS", "SUCCEED", "ESIGN_SUCCESS")) {
            return PayeaseSubMerchantStatusEnum.PASS;
        }
        if (StrUtil.equalsAnyIgnoreCase(reviewStatus,
                "NO_PASS", "REFUSE", "FAILED", "FAIL", "ESIGN_FAIL")) {
            return PayeaseSubMerchantStatusEnum.REJECT;
        }
        return PayeaseSubMerchantStatusEnum.AUDITING;
    }

    /**
     * 封装渠道提示信息，方便后台查看具体原因。
     */
    private String buildChannelTips(PayeaseDeclarationResult result, PayeaseSubMerchantStatusEnum statusEnum) {
        if (statusEnum == PayeaseSubMerchantStatusEnum.PASS) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        if (StrUtil.isNotBlank(result.getReviewRemark())) {
            sb.append(maskPersonalKeyword(result.getReviewRemark()));
        }
        if (StrUtil.isNotBlank(result.getPostReviewRemark())) {
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append("核查：").append(maskPersonalKeyword(result.getPostReviewRemark()));
        }
        if (statusEnum == PayeaseSubMerchantStatusEnum.AUDITING
                && StrUtil.isNotBlank(result.getElectronicContractingUrl())) {
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append("签约链接：").append(result.getElectronicContractingUrl());
        }
        if (StrUtil.isNotBlank(result.getCertificateSupplementUrl())) {
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append("补充链接：").append(result.getCertificateSupplementUrl());
        }
        if (sb.length() == 0 && StrUtil.isNotBlank(result.getErrorMessage())) {
            sb.append(maskPersonalKeyword(result.getErrorMessage()));
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void recordStatus(Long id, PayeaseSubMerchantStatusEnum statusEnum,
                             String rejectReason, String subMerchantId, String requestId) {
        PaySettleAccountDO update = new PaySettleAccountDO();
        update.setId(id);
        if (statusEnum != null) {
            update.setStatus(statusEnum.getStatus());
        }
        update.setRejectReason(statusEnum == PayeaseSubMerchantStatusEnum.PASS ? null : rejectReason);
        if (StrUtil.isNotBlank(subMerchantId)) {
            update.setSubMerchantId(subMerchantId);
        }
        if (StrUtil.isNotBlank(requestId)) {
            update.setRequestId(requestId);
        }
        settleAccountMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void clearProblemRecord(Long id) {
        PaySettleAccountDO update = new PaySettleAccountDO();
        update.setId(id);
        update.setRequestId(null);
        update.setSubMerchantId(null);
        settleAccountMapper.updateById(update);
    }

    private String maskPersonalKeyword(String text) {
        if (StrUtil.isBlank(text)) {
            return text;
        }
        return StrUtil.replace(text, "法人", "个人");
    }
}
