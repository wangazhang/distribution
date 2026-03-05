package com.hissp.distribution.module.pay.service.refund;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.pay.core.client.PayClient;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundInnerRespDTO;
import com.hissp.distribution.framework.pay.core.client.dto.refund.PayRefundUnifiedInnerReqDTO;
import com.hissp.distribution.framework.pay.core.enums.refund.PayRefundStatusRespEnum;
import com.hissp.distribution.framework.tenant.core.util.TenantUtils;
import com.hissp.distribution.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.hissp.distribution.module.pay.controller.admin.refund.vo.PayRefundExportReqVO;
import com.hissp.distribution.module.pay.controller.admin.refund.vo.PayRefundPageReqVO;
import com.hissp.distribution.module.pay.convert.refund.PayRefundConvert;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.channel.PayChannelDO;
import com.hissp.distribution.module.pay.dal.dataobject.order.PayOrderDO;
import com.hissp.distribution.module.pay.dal.dataobject.refund.PayRefundDO;
import com.hissp.distribution.module.pay.dal.mysql.refund.PayRefundMapper;
import com.hissp.distribution.module.pay.dal.redis.no.PayNoRedisDAO;
import com.hissp.distribution.module.pay.enums.notify.PayNotifyTypeEnum;
import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import com.hissp.distribution.module.pay.enums.refund.PayRefundStatusEnum;
import com.hissp.distribution.module.pay.framework.pay.config.PayProperties;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.channel.PayChannelService;
import com.hissp.distribution.module.pay.service.notify.PayNotifyService;
import com.hissp.distribution.module.pay.service.order.PayOrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.util.json.JsonUtils.toJsonString;
import static com.hissp.distribution.module.pay.enums.ErrorCodeConstants.*;

/**
 * 退款订单 Service 实现类
 *
 * @author jason
 */
@Service
@Slf4j
@Validated
public class PayRefundServiceImpl implements PayRefundService {
    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Resource
    private PayProperties payProperties;

    @Resource
    private PayRefundMapper refundMapper;
    @Resource
    private PayNoRedisDAO noRedisDAO;

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayAppService appService;
    @Resource
    private PayChannelService channelService;
    @Resource
    private PayNotifyService notifyService;

    @Override
    public PayRefundDO getRefund(Long id) {
        return refundMapper.selectById(id);
    }

    @Override
    public PayRefundDO getRefundByNo(String no) {
        return refundMapper.selectByNo(no);
    }

    @Override
    public Long getRefundCountByAppId(Long appId) {
        return refundMapper.selectCountByAppId(appId);
    }

    @Override
    public PageResult<PayRefundDO> getRefundPage(PayRefundPageReqVO pageReqVO) {
        return refundMapper.selectPage(pageReqVO);
    }

    @Override
    public List<PayRefundDO> getRefundList(PayRefundExportReqVO exportReqVO) {
        return refundMapper.selectList(exportReqVO);
    }

    @Override
    public Long createPayRefund(PayRefundCreateReqDTO reqDTO) {
        // 1.1 校验 App
        PayAppDO app = appService.validPayApp(reqDTO.getAppKey());
        // 1.2 校验支付订单
        PayOrderDO order = validatePayOrderCanRefund(reqDTO, app.getId());
        // 1.3 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(order.getChannelId());
        PayClient client = channelService.getPayClient(channel.getId());
        if (client == null) {
            log.error("[refund][渠道编号({}) 找不到对应的支付客户端]", channel.getId());
            throw exception(CHANNEL_NOT_FOUND);
        }
        // 1.4 校验退款订单是否已经存在
        PayRefundDO refund = refundMapper.selectByAppIdAndMerchantRefundId(
                app.getId(), reqDTO.getMerchantRefundId());
        if (refund != null) {
            throw exception(REFUND_EXISTS);
        }

        // 2.1 插入退款单
        String no = noRedisDAO.generate(payProperties.getRefundNoPrefix());
        refund = PayRefundConvert.INSTANCE.convert(reqDTO)
                .setNo(no).setAppId(app.getId()).setOrderId(order.getId()).setOrderNo(order.getNo())
                .setChannelId(order.getChannelId()).setChannelCode(order.getChannelCode())
                // 商户相关的字段
                .setNotifyUrl(app.getRefundNotifyUrl())
                // 渠道相关字段
                .setChannelOrderNo(order.getChannelOrderNo())
                // 退款相关字段
                .setStatus(PayRefundStatusEnum.WAITING.getStatus())
                .setPayPrice(order.getPrice()).setRefundPrice(reqDTO.getPrice());
        refundMapper.insert(refund);
        try {
            // 2.2 向渠道发起退款申请
            PayRefundUnifiedInnerReqDTO unifiedReqDTO = new PayRefundUnifiedInnerReqDTO()
                    .setPayPrice(order.getPrice())
                    .setRefundPrice(reqDTO.getPrice())
                    .setOutTradeNo(order.getNo())
                    .setChannelOrderNo(order.getChannelOrderNo())
                    .setOutRefundNo(refund.getNo())
                    .setNotifyUrl(genChannelRefundNotifyUrl(channel))
                    .setReason(reqDTO.getReason());
            PayRefundInnerRespDTO refundRespDTO = client.unifiedRefund(unifiedReqDTO);
            // 2.3 处理退款返回
            getSelf().notifyRefund(channel, refundRespDTO);
        } catch (Throwable e) {
            // 注意：这里仅打印异常，不进行抛出。
            // 原因是：虽然调用支付渠道进行退款发生异常（网络请求超时），实际退款成功。这个结果，后续通过退款回调、或者退款轮询补偿可以拿到。
            // 最终，在异常的情况下，支付中心会异步回调业务的退款回调接口，提供退款结果

            log.error("[createPayRefund][退款 id({}) requestDTO({}) 发生异常]",
                    refund.getId(), reqDTO, e);
        }

        // 返回退款编号
        return refund.getId();
    }

    /**
     * 校验支付订单是否可以退款
     *
     * @param reqDTO 退款申请信息
     * @return 支付订单
     */
    private PayOrderDO validatePayOrderCanRefund(PayRefundCreateReqDTO reqDTO, Long appId) {
        PayOrderDO order = orderService.getOrder(appId, reqDTO.getMerchantOrderId());
        if (order == null) {
            throw exception(PAY_ORDER_NOT_FOUND);
        }
        // 校验状态，必须是已支付、或者已退款
        if (!PayOrderStatusEnum.isSuccessOrRefund(order.getStatus())) {
            throw exception(PAY_ORDER_REFUND_FAIL_STATUS_ERROR);
        }
        // 测试环境设置退款金额0.01
        // 如果是测试环境，将价格设置为1分钱
        if ("test".equals(activeProfile)||"local".equals(activeProfile)) {
            reqDTO.setPrice(1);
        }
        // 校验金额，退款金额不能大于原定的金额，
        if (reqDTO.getPrice() + order.getRefundPrice() > order.getPrice()) {
            throw exception(REFUND_PRICE_EXCEED);
        }
        // 是否有退款中的订单
        if (refundMapper.selectCountByAppIdAndOrderId(appId, order.getId(),
                PayRefundStatusEnum.WAITING.getStatus()) > 0) {
            throw exception(REFUND_HAS_REFUNDING);
        }
        return order;
    }

    /**
     * 根据支付渠道的编码，生成支付渠道的回调地址
     *
     * @param channel 支付渠道
     * @return 支付渠道的回调地址  配置地址 + "/" + channel id
     */
    private String genChannelRefundNotifyUrl(PayChannelDO channel) {
        return payProperties.getRefundNotifyUrl() + "/" + channel.getId();
    }

    @Override
    public void notifyRefund(Long channelId, PayRefundInnerRespDTO notify) {
        // 校验支付渠道是否有效
        PayChannelDO channel = channelService.validPayChannel(channelId);
        // 更新退款订单
        TenantUtils.execute(channel.getTenantId(), () -> getSelf().notifyRefund(channel, notify));
    }

    /**
     * 通知并更新订单的退款结果
     *
     * @param channel 支付渠道
     * @param notify  通知
     */
    // 注意，如果是方法内调用该方法，需要通过 getSelf().notifyRefund(channel, notify) 调用，否则事务不生效
    @Transactional(rollbackFor = Exception.class)
    public void notifyRefund(PayChannelDO channel, PayRefundInnerRespDTO notify) {
        // 情况一：退款成功
        if (PayRefundStatusRespEnum.isSuccess(notify.getStatus())) {
            notifyRefundSuccess(channel, notify);
            return;
        }
        // 情况二：退款失败
        if (PayRefundStatusRespEnum.isFailure(notify.getStatus())) {
            notifyRefundFailure(channel, notify);
            return;
        }
        // 情况三：退款中
        if (PayRefundStatusRespEnum.isWaiting(notify.getStatus())) {
            notifyRefundWaiting(channel, notify);
            return;
        }
    }

    private void notifyRefundSuccess(PayChannelDO channel, PayRefundInnerRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isSuccess(refund.getStatus())) { // 如果已经是成功，直接返回，不用重复更新
            log.info("[notifyRefundSuccess][退款订单({}) 已经是退款成功，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setSuccessTime(notify.getSuccessTime())
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.SUCCESS.getStatus())
                .setChannelNotifyData(toJsonString(notify));
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundSuccess][退款订单({}) 更新为退款成功]", refund.getId());

        // 2. 更新订单
        orderService.updateOrderRefundPrice(refund.getOrderId(), refund.getRefundPrice());

        // 3. 插入退款通知记录
        notifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
                refund.getId());
    }

    private void notifyRefundFailure(PayChannelDO channel, PayRefundInnerRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }
        if (PayRefundStatusEnum.isFailure(refund.getStatus())) { // 如果已经是失败，直接返回，不用重复更新
            log.info("[notifyRefundFailure][退款订单({}) 已经是退款失败，无需更新]", refund.getId());
            return;
        }
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        // 1.2 更新 PayRefundDO
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setStatus(PayRefundStatusEnum.FAILURE.getStatus())
                .setChannelNotifyData(toJsonString(notify))
                .setChannelErrorCode(notify.getChannelErrorCode()).setChannelErrorMsg(notify.getChannelErrorMsg());
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) { // 校验状态，必须是等待状态
            throw exception(REFUND_STATUS_IS_NOT_WAITING);
        }
        log.info("[notifyRefundFailure][退款订单({}) 更新为退款失败，原因: {}]", refund.getId(), notify.getChannelErrorMsg());

        //// 2. 插入退款通知记录
        //notifyService.createPayNotifyTask(PayNotifyTypeEnum.REFUND.getType(),
        //        refund.getId());
    }

    /**
     * 处理退款处理中的情况
     */
    private void notifyRefundWaiting(PayChannelDO channel, PayRefundInnerRespDTO notify) {
        // 1.1 查询 PayRefundDO
        PayRefundDO refund = refundMapper.selectByAppIdAndNo(
                channel.getAppId(), notify.getOutRefundNo());
        if (refund == null) {
            throw exception(REFUND_NOT_FOUND);
        }

        // 1.2 如果已经不是等待状态，说明状态已经变更，无需处理
        if (!PayRefundStatusEnum.WAITING.getStatus().equals(refund.getStatus())) {
            log.debug("[notifyRefundWaiting][退款订单({}) 状态已变更为: {}，无需处理]", refund.getId(), refund.getStatus());
            return;
        }

        // 1.3 更新渠道通知数据，但保持等待状态
        PayRefundDO updateRefundObj = new PayRefundDO()
                .setChannelRefundNo(notify.getChannelRefundNo())
                .setChannelNotifyData(toJsonString(notify));
        int updateCounts = refundMapper.updateByIdAndStatus(refund.getId(), refund.getStatus(), updateRefundObj);
        if (updateCounts == 0) {
            log.info("[notifyRefundWaiting][退款订单({}) 状态已变更，无需更新]", refund.getId());
            return;
        }

        // 1.4 使用 debug 级别记录处理中状态，避免频繁打印
        log.info("[notifyRefundWaiting][退款订单({}) 仍在处理中，等待最终结果]", refund.getId());
    }

    @Override
    public int syncRefund() {
        // 1. 查询指定创建时间内的待退款订单
        List<PayRefundDO> refunds = refundMapper.selectListByStatus(PayRefundStatusEnum.WAITING.getStatus());
        if (CollUtil.isEmpty(refunds)) {
            return 0;
        }
        // 2. 遍历执行
        int count = 0;
        for (PayRefundDO refund : refunds) {
            count += syncRefund(refund) ? 1 : 0;
        }
        return count;
    }

    /**
     * 同步单个退款订单
     *
     * @param refund 退款订单
     * @return 是否同步到
     */
    private boolean syncRefund(PayRefundDO refund) {
        try {
            // 1.1 查询退款订单信息
            PayClient payClient = channelService.getPayClient(refund.getChannelId());
            if (payClient == null) {
                log.error("[syncRefund][渠道编号({}) 找不到对应的支付客户端]", refund.getChannelId());
                return false;
            }

            // 1.2 查询退款状态
            PayRefundInnerRespDTO respDTO = payClient.getRefund(refund.getOrderNo(), refund.getNo());

            // 1.3 记录同步日志（根据状态决定日志级别）
            if (PayRefundStatusRespEnum.isSuccess(respDTO.getStatus())) {
                log.info("[syncRefund][退款订单({}) 同步成功，状态: 退款成功]", refund.getId());
            } else if (PayRefundStatusRespEnum.isFailure(respDTO.getStatus())) {
                log.info("[syncRefund][退款订单({}) 同步成功，状态: 退款失败，原因: {}]",
                        refund.getId(), respDTO.getChannelErrorMsg());
            } else {
                // 处理中状态使用 debug 级别，避免频繁打印
                log.debug("[syncRefund][退款订单({}) 仍在处理中，继续等待]", refund.getId());
            }

            // 1.4 回调退款结果
            notifyRefund(refund.getChannelId(), respDTO);

            // 2. 如果同步到最终状态（成功或失败），则返回 true
            return PayRefundStatusRespEnum.isSuccess(respDTO.getStatus())
                    || PayRefundStatusRespEnum.isFailure(respDTO.getStatus());
        } catch (Throwable e) {
            log.error("[syncRefund][refund({}) 同步退款状态异常]", refund.getId(), e);
            return false;
        }
    }

    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private PayRefundServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }

}
