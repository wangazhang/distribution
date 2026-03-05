package com.hissp.distribution.module.pay.service.transfer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.framework.pay.core.client.PayClient;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferStatusRespEnum;
import com.hissp.distribution.framework.pay.core.enums.transfer.PayTransferTypeEnum;
import com.hissp.distribution.framework.tenant.core.util.TenantUtils;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.channel.PayChannelDO;
import com.hissp.distribution.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.hissp.distribution.module.pay.dal.mysql.transfer.PayTransferMapper;
import com.hissp.distribution.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hissp.distribution.module.pay.enums.notify.PayNotifyTypeEnum;
import com.hissp.distribution.module.pay.enums.transfer.PayTransferStatusEnum;
import com.hissp.distribution.module.pay.framework.pay.config.PayProperties;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.channel.PayChannelService;
import com.hissp.distribution.module.pay.service.notify.PayNotifyService;
import jakarta.annotation.Resource;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.pay.convert.transfer.PayTransferConvert.INSTANCE;
import static com.hissp.distribution.module.pay.enums.ErrorCodeConstants.*;
import static com.hissp.distribution.module.pay.enums.transfer.PayTransferStatusEnum.*;

// TODO @jason：等彻底实现完，单测写写；

/**
 * 转账 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
public class PayTransferServiceImpl implements PayTransferService {

    private static final String TRANSFER_NO_PREFIX = "T";

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayTransferMapper transferMapper;
    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;
    @Resource
    private PayNoRedisDAO noRedisDAO;
    @Resource
    private Validator validator;

    @Override
    public PayTransferDO createTransfer(PayTransferCreateReqVO reqVO, String userIp) {
        // 1. 校验参数
        reqVO.validate(validator);

        // 2. 创建转账单，发起转账
        PayTransferCreateReqDTO req = INSTANCE.convert(reqVO).setUserIp(userIp);
        Long transferId = createTransfer(req);

        // 3. 返回转账单
        return getTransfer(transferId);
    }

    @Override
    public Long createTransfer(PayTransferCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayAppDO payApp = appService.validPayApp(reqDTO.getAppKey());
        // 1.2 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(payApp.getId(), reqDTO.getChannelCode());
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[createTransfer][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 1.3 校验转账单已经发起过转账。
        PayTransferDO transfer = validateTransferCanCreate(reqDTO, payApp.getId());

        if (transfer == null) {
            // 2.不存在创建转账单. 否则允许使用相同的 no 再次发起转账
            String no = noRedisDAO.generate(TRANSFER_NO_PREFIX);
            transfer = INSTANCE.convert(reqDTO)
                    .setChannelId(channel.getId())
                    .setNo(no).setStatus(WAITING.getStatus())
                    .setNotifyUrl(payApp.getTransferNotifyUrl())
                    .setAppId(channel.getAppId());
            // 首次创建时记录请求快照，确保渠道参数完整写入数据库
            applyRequestSnapshot(transfer, reqDTO);
            transferMapper.insert(transfer);
        } else {
            // 复用等待中的转账单时，需要把最新的打款参数覆盖回数据库，保证渠道可获得完整信息
            applyRequestSnapshot(transfer, reqDTO);
            transferMapper.updateById(transfer);
        }
        try {
            // 3. 调用三方渠道发起转账
            PayTransferUnifiedInnerReqDTO transferUnifiedReq = INSTANCE.convert2(transfer)
                    .setOutTransferNo(transfer.getNo());
            transferUnifiedReq.setNotifyUrl(genChannelTransferNotifyUrl(channel));
            PayTransferInnerRespDTO unifiedTransferResp = client.unifiedTransfer(transferUnifiedReq);
            // 4. 通知转账结果
            getSelf().notifyTransfer(channel, unifiedTransferResp);
        } catch (ServiceException ex) {
            log.error("[createTransfer][转账 id({}) requestDTO({}) 发生业务异常]", transfer.getId(), reqDTO, ex);
            recordChannelError(transfer, channel, String.valueOf(ex.getCode()), ex.getMessage());
            throw ex;
        } catch (Throwable e) {
            log.error("[createTransfer][转账 id({}) requestDTO({}) 发生异常]", transfer.getId(), reqDTO, e);
            recordChannelError(transfer, channel,
                    String.valueOf(PAY_TRANSFER_CHANNEL_REQUEST_FAILED.getCode()),
                    ExceptionUtil.getRootCauseMessage(e));
            throw exception(PAY_TRANSFER_CHANNEL_REQUEST_FAILED);
        }

        return transfer.getId();
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelTransferNotifyUrl(PayChannelDO channel) {
        return payProperties.getTransferNotifyUrl() + "/" + channel.getId();
    }

    private PayTransferDO validateTransferCanCreate(PayTransferCreateReqDTO dto, Long appId) {
        PayTransferDO transfer = transferMapper.selectByAppIdAndMerchantTransferId(appId, dto.getMerchantTransferId());
        if (transfer != null) {
            // 已经存在,并且状态不为等待状态。说明已经调用渠道转账并返回结果.
            if (PayTransferStatusEnum.isClosed(transfer.getStatus())) {
                return resetTransferForRetry(transfer);
            }
            if (!PayTransferStatusEnum.isWaiting(transfer.getStatus())) {
                throw exception(PAY_MERCHANT_TRANSFER_EXISTS);
            }
            if (ObjectUtil.notEqual(dto.getPrice(), transfer.getPrice())) {
                throw exception(PAY_SAME_MERCHANT_TRANSFER_PRICE_NOT_MATCH);
            }
            if (ObjectUtil.notEqual(dto.getType(), transfer.getType())) {
                throw exception(PAY_SAME_MERCHANT_TRANSFER_TYPE_NOT_MATCH);
            }
        }
        // 如果状态为等待状态。不知道渠道转账是否发起成功。 允许使用相同的 no 再次发起转账，渠道会保证幂等
        return transfer;
    }

    /**
     * 渠道打款失败后重新生成一条“待转账”记录。
     *
     * <p>场景：渠道打款失败返回 CLOSED，需要允许财务“重新发起”。此时我们复用业务侧的
     * {@code merchantTransferId}，但是换一个新的渠道转账单号，避免渠道认为重复请求。</p>
     */
    private PayTransferDO resetTransferForRetry(PayTransferDO transfer) {
        String newNo = noRedisDAO.generate(TRANSFER_NO_PREFIX);
        transferMapper.updateById(new PayTransferDO()
                .setId(transfer.getId())
                .setNo(newNo)
                .setStatus(WAITING.getStatus())
                .setChannelTransferNo(null)
                .setChannelErrorCode(null)
                .setChannelErrorMsg(null)
                .setChannelNotifyData(null)
                .setSuccessTime(null));
        transfer.setNo(newNo);
        transfer.setStatus(WAITING.getStatus());
        transfer.setChannelTransferNo(null);
        transfer.setChannelErrorCode(null);
        transfer.setChannelErrorMsg(null);
        transfer.setChannelNotifyData(null);
        transfer.setSuccessTime(null);
        return transfer;
    }

    /**
     * 将请求中的渠道参数复制到 DB，便于后续重试/同步/展示。
     */
    private void applyRequestSnapshot(PayTransferDO transfer, PayTransferCreateReqDTO reqDTO) {
        // 渠道扩展参数（如首信易银行卡信息）直接保存在转账单上，便于后续重试
        transfer.setChannelExtras(reqDTO.getChannelExtras());
        transfer.setReceiverId(reqDTO.getReceiverId());
        transfer.setReceiverType(reqDTO.getReceiverType());
        transfer.setCurrency(reqDTO.getCurrency());
        transfer.setRemark(reqDTO.getRemark());
        transfer.setUserName(reqDTO.getUserName());
        transfer.setAlipayLogonId(reqDTO.getAlipayLogonId());
        transfer.setOpenid(reqDTO.getOpenid());
        transfer.setUserIp(reqDTO.getUserIp());
    }

    /**
     * 渠道请求失败时记录错误信息，便于后台及前端查看。
     *
     * @param transfer 转账单
     * @param channel  渠道（可能为 null，异常发生在获取渠道之前）
     * @param errorCode 渠道错误码
     * @param errorMsg  渠道错误信息
     */
    private void recordChannelError(PayTransferDO transfer, PayChannelDO channel,
                                    String errorCode, String errorMsg) {
        PayTransferDO update = new PayTransferDO()
                .setId(transfer.getId())
                .setChannelId(channel.getId());
        if (channel != null) {
            update.setChannelCode(channel.getCode());
        }
        update.setChannelErrorCode(errorCode);
        update.setChannelErrorMsg(StrUtil.maxLength(errorMsg, 255));
        transferMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyTransfer(channel, notify) 调用，否则事务不生效
    public void notifyTransfer(PayChannelDO channel, PayTransferInnerRespDTO notify) {
        // 转账成功的回调
        if (PayTransferStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyTransferSuccess(channel, notify);
        }
        // 转账关闭的回调
        if (PayTransferStatusRespEnum.isClosed(notify.getStatus())) {
            notifyTransferClosed(channel, notify);
        }
        // 转账处理中的回调
        if (PayTransferStatusRespEnum.isInProgress(notify.getStatus())) {
            notifyTransferInProgress(channel, notify);
        }
        // WAITING 状态无需处理
    }

    private void notifyTransferInProgress(PayChannelDO channel, PayTransferInnerRespDTO notify) {
        // 1.校验
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (isInProgress(transfer.getStatus())) { // 如果已经是转账中，直接返回，不用重复更新
            return;
        }
        if (!isWaiting(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_WAITING);
        }
        // 2.更新
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(WAITING.getStatus()),
                new PayTransferDO().setStatus(IN_PROGRESS.getStatus()));
        if (updateCounts == 0) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyTransferInProgress][transfer({}) 更新为转账进行中状态]", transfer.getId());
    }


    private void notifyTransferSuccess(PayChannelDO channel, PayTransferInnerRespDTO notify) {
        // 1.校验
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (isSuccess(transfer.getStatus())) { // 如果已成功，直接返回，不用重复更新
            return;
        }
        if (!isPendingStatus(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        // 2.更新
        int updateCounts = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferDO().setStatus(SUCCESS.getStatus()).setSuccessTime(notify.getSuccessTime())
                        .setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCounts == 0) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferSuccess][transfer({}) 更新为已转账]", transfer.getId());

        // 3. 插入转账通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.TRANSFER.getType(),
                transfer.getId());
    }

    private void notifyTransferClosed(PayChannelDO channel, PayTransferInnerRespDTO notify) {
        // 1.校验
        PayTransferDO transfer = transferMapper.selectByAppIdAndNo(channel.getAppId(), notify.getOutTransferNo());
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        if (isClosed(transfer.getStatus())) { // 如果已是关闭状态，仅更新渠道错误信息
            transferMapper.updateById(new PayTransferDO().setId(transfer.getId())
                    .setChannelId(channel.getId()).setChannelCode(channel.getCode())
                    .setChannelTransferNo(notify.getChannelTransferNo())
                    .setChannelErrorCode(notify.getChannelErrorCode())
                    .setChannelErrorMsg(notify.getChannelErrorMsg())
                    .setChannelNotifyData(JsonUtils.toJsonString(notify)));
            log.info("[updateTransferClosed][transfer({}) 已是关闭状态，更新渠道错误信息]", transfer.getId());
            return;
        }
        if (!isPendingStatus(transfer.getStatus())) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }

        // 2.更新
        int updateCount = transferMapper.updateByIdAndStatus(transfer.getId(),
                CollUtil.newArrayList(WAITING.getStatus(), IN_PROGRESS.getStatus()),
                new PayTransferDO().setStatus(CLOSED.getStatus()).setChannelId(channel.getId())
                        .setChannelCode(channel.getCode()).setChannelTransferNo(notify.getChannelTransferNo())
                        .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg())
                        .setChannelNotifyData(JsonUtils.toJsonString(notify)));
        if (updateCount == 0) {
            throw exception(PAY_TRANSFER_STATUS_IS_NOT_PENDING);
        }
        log.info("[updateTransferClosed][transfer({}) 更新为关闭状态]", transfer.getId());

        // 3. 插入转账通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.TRANSFER.getType(),
                transfer.getId());

    }

    @Override
    public PayTransferDO getTransfer(Long id) {
        return transferMapper.selectById(id);
    }

    @Override
    public PayTransferDO getTransferByAppIdAndMerchantTransferId(Long appId, String merchantTransferId) {
        // 业务侧通常只知道 appKey + merchantTransferId，因此提供查询能力供渠道打款等场景复用
        return transferMapper.selectByAppIdAndMerchantTransferId(appId, merchantTransferId);
    }

    @Override
    public PageResult<PayTransferDO> getTransferPage(PayTransferPageReqVO pageReqVO) {
        return transferMapper.selectPage(pageReqVO);
    }

    @Override
    public int syncTransfer() {
        List<PayTransferDO> list = transferMapper.selectListByStatus(WAITING.getStatus());
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        int count = 0;
        for (PayTransferDO transfer : list) {
            count += syncTransfer(transfer) ? 1 : 0;
        }
        return count;
    }

    @Override
    public boolean syncTransfer(Long id) {
        PayTransferDO transfer = transferMapper.selectById(id);
        if (transfer == null) {
            throw exception(PAY_TRANSFER_NOT_FOUND);
        }
        return syncTransfer(transfer);
    }

    private boolean syncTransfer(PayTransferDO transfer) {
        try {
            // 1. 查询转账订单信息
            PayClient payClient = channelService.getPayClient(transfer.getChannelId());
            if (payClient == null) {
                log.error("[syncTransfer][渠道编号({}) 找不到对应的支付客户端]", transfer.getChannelId());
                return false;
            }
            PayTransferInnerRespDTO resp = payClient.getTransfer(transfer.getNo(),
                    PayTransferTypeEnum.typeOf(transfer.getType()));

            // 2. 回调转账结果
            notifyTransfer(transfer.getChannelId(), resp);
            return true;
        } catch (Throwable ex) {
            log.error("[syncTransfer][transfer({}) 同步转账单状态异常]", transfer.getId(), ex);
            return false;
        }
    }

    public void notifyTransfer(Long channelId, PayTransferInnerRespDTO notify) {
        // 校验渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 通知转账结果给对应的业务
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyTransfer(channel, notify));
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayTransferServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }
}
