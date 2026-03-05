package com.hissp.distribution.module.trade.service.order;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.hissp.distribution.framework.common.enums.UserTypeEnum;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.common.util.id.IdGeneratorUtil;
import com.hissp.distribution.framework.common.util.json.JsonUtils;
import com.hissp.distribution.framework.common.util.number.MoneyUtils;
import com.hissp.distribution.framework.common.util.servlet.ServletUtils;
import com.hissp.distribution.module.fulfillmentchannel.api.ChannelShippingApi;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO.ChannelShippingConfirmStatus;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.enums.ChannelShippingLogisticsTypeEnum;
import com.hissp.distribution.module.member.api.address.MemberAddressApi;
import com.hissp.distribution.module.member.api.address.dto.MemberAddressRespDTO;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.pay.api.order.PayOrderApi;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import com.hissp.distribution.module.pay.api.refund.PayRefundApi;
import com.hissp.distribution.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import com.hissp.distribution.module.product.api.comment.ProductCommentApi;
import com.hissp.distribution.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import com.hissp.distribution.module.product.api.sku.ProductSkuApi;
import com.hissp.distribution.module.product.api.sku.dto.ProductSkuRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.enums.spu.ProductSpuTypeEnum;
import com.hissp.distribution.module.system.api.social.SocialClientApi;
import com.hissp.distribution.module.system.api.social.dto.SocialWxaSubscribeMessageSendReqDTO;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderChannelReceiveReqDTO;
import com.hissp.distribution.module.trade.controller.admin.order.vo.TradeOrderDeliveryReqVO;
import com.hissp.distribution.module.trade.controller.admin.order.vo.TradeOrderRemarkReqVO;
import com.hissp.distribution.module.trade.controller.admin.order.vo.TradeOrderUpdateAddressReqVO;
import com.hissp.distribution.module.trade.controller.admin.order.vo.TradeOrderUpdatePriceReqVO;
import com.hissp.distribution.module.trade.controller.app.order.vo.AppTradeOrderCreateReqVO;
import com.hissp.distribution.module.trade.controller.app.order.vo.AppTradeOrderSettlementReqVO;
import com.hissp.distribution.module.trade.controller.app.order.vo.AppTradeOrderSettlementRespVO;
import com.hissp.distribution.module.trade.controller.app.order.vo.item.AppTradeOrderItemCommentCreateReqVO;
import com.hissp.distribution.module.trade.convert.TradeOrderEventConvert;
import com.hissp.distribution.module.trade.convert.order.TradeOrderConvert;
import com.hissp.distribution.module.trade.dal.dataobject.cart.CartDO;
import com.hissp.distribution.module.trade.dal.dataobject.delivery.DeliveryExpressDO;
import com.hissp.distribution.module.trade.dal.dataobject.delivery.DeliveryPickUpStoreDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderItemMapper;
import com.hissp.distribution.module.trade.dal.mysql.order.TradeOrderMapper;
import com.hissp.distribution.module.trade.dal.redis.no.TradeNoRedisDAO;
import com.hissp.distribution.module.trade.enums.delivery.DeliveryTypeEnum;
import com.hissp.distribution.module.trade.enums.order.*;
import com.hissp.distribution.module.trade.framework.order.config.TradeOrderProperties;
import com.hissp.distribution.module.trade.framework.order.core.annotations.TradeOrderLog;
import com.hissp.distribution.module.trade.framework.order.core.utils.TradeOrderLogUtils;
import com.hissp.distribution.module.trade.message.TradeOrderChannelShippingMessage;
import com.hissp.distribution.module.trade.message.TradeOrderPaymentSuccessMessage;
import com.hissp.distribution.module.trade.mq.producer.TradeOrderEventProducer;
import com.hissp.distribution.module.trade.service.cart.CartService;
import com.hissp.distribution.module.trade.service.delivery.DeliveryExpressService;
import com.hissp.distribution.module.trade.service.delivery.DeliveryPickUpStoreService;
import com.hissp.distribution.module.trade.service.message.TradeMessageService;
import com.hissp.distribution.module.trade.service.message.bo.TradeOrderMessageWhenDeliveryOrderReqBO;
import com.hissp.distribution.module.trade.service.order.handler.TradeOrderHandler;
import com.hissp.distribution.module.trade.service.price.TradePriceService;
import com.hissp.distribution.module.trade.service.price.bo.TradePriceCalculateReqBO;
import com.hissp.distribution.module.trade.service.price.bo.TradePriceCalculateRespBO;
import com.hissp.distribution.module.trade.service.price.calculator.TradePriceCalculatorHelper;
import com.hissp.distribution.module.trade.service.refund.RefundPreCheckService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.*;
import static com.hissp.distribution.framework.common.util.date.LocalDateTimeUtils.minusTime;
import static com.hissp.distribution.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getTerminal;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.*;
import static com.hissp.distribution.module.trade.enums.MessageTemplateConstants.WXA_ORDER_DELIVERY;

/**
 * 交易订单【写】Service 实现类
 *
 * @author LeeYan9
 * @since 2022-08-26
 */
@Service
@Slf4j
public class TradeOrderUpdateServiceImpl implements TradeOrderUpdateService {

    private static final int ROCKETMQ_DELAY_LEVEL_1_MINUTE = 5;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private TradeOrderItemMapper tradeOrderItemMapper;
    @Resource
    private TradeNoRedisDAO tradeNoRedisDAO;

    @Resource
    private List<TradeOrderHandler> tradeOrderHandlers;

    @Resource
    private CartService cartService;
    @Resource
    private TradePriceService tradePriceService;
    @Resource
    private DeliveryExpressService deliveryExpressService;
    @Resource
    private TradeMessageService tradeMessageService;
    @Resource
    private DeliveryPickUpStoreService pickUpStoreService;

    @Resource
    private PayOrderApi payOrderApi;
    @Resource
    private MemberAddressApi addressApi;
    @Resource
    private ProductCommentApi productCommentApi;
    @Resource
    public SocialClientApi socialClientApi;
    @Resource
    public PayRefundApi payRefundApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private ProductSkuApi productSkuApi;

    @Resource
    private BrokerageRecordService brokerageRecordService;

    @Resource
    private TradeOrderProperties tradeOrderProperties;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;

    @Resource
    private BrokerageUserApi brokerageUserApi;

    @Resource
    private ChannelShippingApi channelShippingApi;

    @Resource
    private TradeOrderEventProducer tradeOrderEventProducer;
    @Resource
    private TradeOrderEventConvert tradeOrderEventConvert;
    @Resource
    private RefundPreCheckService refundPreCheckService;

    // =================== Order ===================

    @Override
    public AppTradeOrderSettlementRespVO settlementOrder(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        // 1. 获得收货地址
        MemberAddressRespDTO address = getAddress(userId, settlementReqVO.getAddressId());
        if (address != null) {
            settlementReqVO.setAddressId(address.getId());
        }

        // 2. 计算价格
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(userId, settlementReqVO);

        // 3. 拼接返回
        return TradeOrderConvert.INSTANCE.convert(calculateRespBO, address);
    }

    @Override
    public void processOrderPaidNotify(Long id, Long payOrderId, String deliveryStatus) {
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            log.warn("[processOrderPaidNotify][order({}) 不存在，忽略回调]", id);
            return;
        }

        order = recordPaymentIfNecessary(order, payOrderId);

        String normalizedStatus = normalizeDeliveryStatus(deliveryStatus);
        if (!isDeliveredStatus(normalizedStatus) || order == null || TradeOrderStatusEnum.isCompleted(order.getStatus())) {
            return;
        }

        if (!ensureDeliveredStatus(order)) {
            log.info("[processOrderPaidNotify][order({}) 状态({}) 非待收货，暂不确认收货]", id, order.getStatus());
            return;
        }

        TradeOrderChannelReceiveReqDTO reqDTO = new TradeOrderChannelReceiveReqDTO();
        reqDTO.setOrderId(order.getId());
        reqDTO.setPayOrderId(order.getPayOrderId());
        reqDTO.setMerchantTradeNo(order.getNo());
        reqDTO.setChannelCode(order.getPayChannelCode());
        reqDTO.setEvent(normalizedStatus);
        reqDTO.setConfirmReceiveTime(LocalDateTime.now());
        try {
            getSelf().receiveOrderByChannel(reqDTO);
        } catch (ServiceException ex) {
            if (ex.getCode().equals(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED.getCode())) {
                log.info("[processOrderPaidNotify][order({}) 状态已变化，忽略渠道确认]", order.getId());
                return;
            }
            throw ex;
        }
    }

    @Override
    public boolean checkOrderReceiveStatus(Long orderId) {
        TradeOrderDO order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (TradeOrderStatusEnum.isCompleted(order.getStatus())) {
            return true;
        }

        ChannelShippingQueryRequestDTO request = new ChannelShippingQueryRequestDTO();
        request.setOrderId(order.getId());
        request.setOrderNo(order.getNo());
        request.setPayOrderId(order.getPayOrderId());
        request.setDelivered(TradeOrderStatusEnum.haveDelivered(order.getStatus()));

        ChannelShippingQueryRespDTO resp;
        try {
            resp = channelShippingApi.query(request);
        } catch (Exception ex) {
            log.warn("[checkOrderReceiveStatus][order({}) 渠道查询失败]", orderId, ex);
            return false;
        }
        if (resp == null || !resp.isShipped()) {
            return false;
        }

        ChannelShippingConfirmStatus confirmStatus =
            ObjectUtil.defaultIfNull(resp.getConfirmStatus(), ChannelShippingConfirmStatus.UNKNOWN);
        if (!ChannelShippingConfirmStatus.CONFIRMED.equals(confirmStatus)) {
            if (ChannelShippingConfirmStatus.CANCELED.equals(confirmStatus)) {
                log.info("[checkOrderReceiveStatus][order({}) 渠道返回取消确认状态，保持待收货]", orderId);
            } else {
                log.debug("[checkOrderReceiveStatus][order({}) 渠道返回状态({})，暂不确认收货]", orderId, confirmStatus);
            }
            return false;
        }

        if (!ensureDeliveredStatus(order)) {
            order = tradeOrderMapper.selectById(orderId);
            if (order == null || !TradeOrderStatusEnum.isDelivered(order.getStatus())) {
                return false;
            }
        }

        TradeOrderChannelReceiveReqDTO reqDTO = new TradeOrderChannelReceiveReqDTO();
        reqDTO.setOrderId(order.getId());
        reqDTO.setPayOrderId(order.getPayOrderId());
        reqDTO.setMerchantTradeNo(order.getNo());
        reqDTO.setChannelCode(order.getPayChannelCode());
        reqDTO.setEvent("CHANNEL_SYNC");
        reqDTO.setConfirmReceiveTime(LocalDateTime.now());
        try {
            getSelf().receiveOrderByChannel(reqDTO);
            return true;
        } catch (ServiceException ex) {
            if (ex.getCode().equals(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED.getCode())) {
                TradeOrderDO latest = tradeOrderMapper.selectById(orderId);
                return latest != null && TradeOrderStatusEnum.isCompleted(latest.getStatus());
            }
            log.warn("[checkOrderReceiveStatus][order({}) 渠道确认失败]", orderId, ex);
        }
        return false;
    }

    /**
     * 获得用户地址
     *
     * @param userId 用户编号
     * @param addressId 地址编号
     * @return 地址
     */
    private MemberAddressRespDTO getAddress(Long userId, Long addressId) {
        if (addressId != null) {
            return addressApi.getAddress(addressId, userId);
        }
        return addressApi.getDefaultAddress(userId);
    }

    /**
     * 计算订单价格
     *
     * @param userId 用户编号
     * @param settlementReqVO 结算信息
     * @return 订单价格
     */
    private TradePriceCalculateRespBO calculatePrice(Long userId, AppTradeOrderSettlementReqVO settlementReqVO) {
        // 1. 如果来自购物车，则获得购物车的商品
        List<CartDO> cartList = cartService.getCartList(userId,
            convertSet(settlementReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId));

        // 2. 计算价格
        TradePriceCalculateReqBO calculateReqBO = TradeOrderConvert.INSTANCE.convert(userId, settlementReqVO, cartList);
        calculateReqBO.getItems().forEach(item -> Assert.isTrue(item.getSelected(), // 防御性编程，保证都是选中的
            "商品({}) 未设置为选中", item.getSkuId()));
        return tradePriceService.calculateOrderPrice(calculateReqBO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_CREATE)
    public TradeOrderDO createOrder(Long userId, AppTradeOrderCreateReqVO createReqVO) {
        // pre 购买资格判断
        privilegeCheck(userId, createReqVO);
        // 1.1 价格计算
        TradePriceCalculateRespBO calculateRespBO = calculatePrice(userId, createReqVO);
        // 1.2 构建订单
        TradeOrderDO order = buildTradeOrder(userId, createReqVO, calculateRespBO);
        List<TradeOrderItemDO> orderItems = buildTradeOrderItems(order, calculateRespBO);

        // 2. 订单创建前的逻辑
        tradeOrderHandlers.forEach(handler -> handler.beforeOrderCreate(order, orderItems));

        // 3. 保存订单
        tradeOrderMapper.insert(order);
        orderItems.forEach(orderItem -> orderItem.setOrderId(order.getId()));
        tradeOrderItemMapper.insertBatch(orderItems);

        // 4. 订单创建后的逻辑
        afterCreateTradeOrder(order, orderItems, createReqVO);
        return order;
    }

    /**
     * 资格校验
     *
     * @param userId 用户Id
     * @param createReqVO 创建对象
     */
    private void privilegeCheck(Long userId, AppTradeOrderCreateReqVO createReqVO) {
        // 资格校验
        List<Long> skuIds = createReqVO.getItems().stream().map(AppTradeOrderSettlementReqVO.Item::getSkuId).toList();
        List<Long> spuIds = productSkuApi.getSkuList(skuIds).stream().map(ProductSkuRespDTO::getSpuId).toList();
        if (productSpuApi.isCareerProduct(spuIds)) {
            // 1.1 已经是代理商了不能再次购买 生涯产品(等级过高)
            MemberUserRespDTO user = memberUserApi.getUser(userId);
            MemberLevelRespDTO memberLevel = memberLevelApi.getMemberLevel(user.getLevelId());
            if (memberLevel != null && memberLevel.getLevel() >= 3) {
                throw exception(ORDER_CREATE_FAIL_PRIVILEGE_USER_LEVEL_BEYOND);
            }
            // 1.2 没有推广人，直接购买，归到总公司；
            BrokerageUserRespDTO bindBrokerageUser = brokerageUserApi.getBindBrokerageUser(userId);
            if (null == bindBrokerageUser) {
                return;
            }
            // 1.3 上级等级不够
            MemberUserRespDTO l1MemberUser = memberUserApi.getUser(bindBrokerageUser.getId());
            Long l1LevelId = l1MemberUser.getLevelId();
            MemberLevelRespDTO l1MemberLevel = memberLevelApi.getMemberLevel(l1LevelId);
            if (l1MemberLevel == null || l1MemberLevel.getLevel() < 3) {
                throw exception(ORDER_CREATE_FAIL_PRIVILEGE_INVITER_LEVEL_LOWER);
            }
        }

    }

    private TradeOrderDO buildTradeOrder(Long userId, AppTradeOrderCreateReqVO createReqVO,
        TradePriceCalculateRespBO calculateRespBO) {
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(userId, createReqVO, calculateRespBO);
        order.setId(IdGeneratorUtil.nextId());
        order.setType(calculateRespBO.getType());
        order.setNo(tradeNoRedisDAO.generate(TradeNoRedisDAO.TRADE_ORDER_NO_PREFIX));
        order.setStatus(TradeOrderStatusEnum.UNPAID.getStatus());
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus());
        order.setProductCount(
            getSumValue(calculateRespBO.getItems(), TradePriceCalculateRespBO.OrderItem::getCount, Integer::sum));
        order.setUserIp(getClientIP()).setTerminal(getTerminal());
        // 使用 + 赠送优惠券
        order.setGiveCouponTemplateCounts(calculateRespBO.getGiveCouponTemplateCounts());
        // 支付 + 退款信息
        order.setAdjustPrice(0).setPayStatus(false);
        order.setRefundStatus(TradeOrderRefundStatusEnum.NONE.getStatus()).setRefundPrice(0);
        // 物流信息
        order.setDeliveryType(createReqVO.getDeliveryType());
        if (Objects.equals(createReqVO.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            MemberAddressRespDTO address = addressApi.getAddress(createReqVO.getAddressId(), userId);
            Assert.notNull(address, "地址({}) 不能为空", createReqVO.getAddressId()); // 价格计算时，已经计算
            order.setReceiverName(address.getName()).setReceiverMobile(address.getMobile())
                .setReceiverAreaId(address.getAreaId()).setReceiverDetailAddress(address.getDetailAddress());
        } else if (Objects.equals(createReqVO.getDeliveryType(), DeliveryTypeEnum.PICK_UP.getType())) {
            order.setReceiverName(createReqVO.getReceiverName()).setReceiverMobile(createReqVO.getReceiverMobile());
            order.setPickUpVerifyCode(RandomUtil.randomNumbers(8)); // 随机一个核销码，长度为 8 位
        }
        return order;
    }

    private List<TradeOrderItemDO> buildTradeOrderItems(TradeOrderDO tradeOrderDO,
        TradePriceCalculateRespBO calculateRespBO) {
        return TradeOrderConvert.INSTANCE.convertList(tradeOrderDO, calculateRespBO);
    }

    /**
     * 订单创建后，执行后置逻辑
     * <p>
     * 例如说：优惠劵的扣减、积分的扣减、支付单的创建等等
     *
     * @param order 订单
     * @param orderItems 订单项
     * @param createReqVO 创建订单请求
     */
    private void afterCreateTradeOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems,
        AppTradeOrderCreateReqVO createReqVO) {
        // 1. 执行订单创建后置处理器
        tradeOrderHandlers.forEach(handler -> handler.afterOrderCreate(order, orderItems));

        // 2. 删除购物车商品
        Set<Long> cartIds = convertSet(createReqVO.getItems(), AppTradeOrderSettlementReqVO.Item::getCartId);
        if (CollUtil.isNotEmpty(cartIds)) {
            cartService.deleteCart(order.getUserId(), cartIds);
        }

        // 3. 生成预支付
        // 特殊情况：积分兑换时，可能支付金额为零
        if (order.getPayPrice() > 0) {
            createPayOrder(order, orderItems);
        }

        // 4. 插入订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), null, order.getStatus());

        // TODO @LeeYan9: 是可以思考下, 订单的营销优惠记录, 应该记录在哪里, 微信讨论起来!
    }

    private void createPayOrder(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        // 创建支付单，用于后续的支付
        PayOrderCreateReqDTO payOrderCreateReqDTO =
            TradeOrderConvert.INSTANCE.convert(order, orderItems, tradeOrderProperties);
        Long payOrderId = payOrderApi.createOrder(payOrderCreateReqDTO);

        // 更新到交易单上
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setPayOrderId(payOrderId));
        order.setPayOrderId(payOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_PAY)
    public void updateOrderPaid(Long id, Long payOrderId) {
        // 1.1 校验订单是否存在
        TradeOrderDO order = validateOrderExists(id);
        // 1.2 校验订单已支付
        if (!TradeOrderStatusEnum.isUnpaid(order.getStatus()) || order.getPayStatus()) {
            // 特殊：如果订单已支付，且支付单号相同，直接返回，说明重复回调
            if (ObjectUtil.equals(order.getPayOrderId(), payOrderId)) {
                log.warn("[updateOrderPaid][order({}) 已支付，且支付单号相同({})，直接返回]", order, payOrderId);
                return;
            }
            log.error("[updateOrderPaid][order({}) 支付单不匹配({})，请进行处理！order 数据是：{}]", id, payOrderId,
                JsonUtils.toJsonString(order));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }

        // 2. 校验支付订单的合法性
        PayOrderRespDTO payOrder = validatePayOrderPaid(order, payOrderId);

        // 3. 更新 TradeOrderDO 状态为已支付，等待发货
        int updateCount = tradeOrderMapper.updateByIdAndStatus(id, order.getStatus(),
            new TradeOrderDO().setStatus(TradeOrderStatusEnum.UNDELIVERED.getStatus()).setPayStatus(true)
                .setPayTime(LocalDateTime.now()).setPayChannelCode(payOrder.getChannelCode()));
        if (updateCount == 0) {
            throw exception(ORDER_UPDATE_PAID_STATUS_NOT_UNPAID);
        }

        // 4. 发送订单支付成功MQ消息
        sendOrderPaymentSuccessMessage(order, LocalDateTime.now());

        // 5. 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.UNDELIVERED.getStatus());
        TradeOrderLogUtils.setUserInfo(order.getUserId(), UserTypeEnum.MEMBER.getValue());
    }

    @Override
    public void syncOrderPayStatusQuietly(Long id, Long payOrderId) {
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            return;
        }
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            return;
        }
        try {
            getSelf().updateOrderPaid(id, payOrderId);
        } catch (Throwable e) {
            log.warn("[syncOrderPayStatusQuietly][id({}) payOrderId({}) 同步支付状态失败]", id, payOrderId, e);
        }
    }

    /**
     * 校验支付订单的合法性
     *
     * @param order 交易订单
     * @param payOrderId 支付订单编号
     * @return 支付订单
     */
    private PayOrderRespDTO validatePayOrderPaid(TradeOrderDO order, Long payOrderId) {
        // 1. 校验支付单是否存在
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 不存在，请进行处理！]", order.getId(), payOrderId);
            throw exception(ORDER_NOT_FOUND);
        }

        // 2.1 校验支付单已支付
        if (!PayOrderStatusEnum.isSuccess(payOrder.getStatus())) {
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 未支付，请进行处理！payOrder 数据是：{}]", order.getId(),
                payOrderId, JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_STATUS_NOT_SUCCESS);
        }
        // 2.2 校验支付金额一致
        ifBlock:
        if (ObjectUtil.notEqual(payOrder.getPrice(), order.getPayPrice())) {
            if ("test".equals(activeProfile)||"local".equals(activeProfile)) {
                // 如果是测试环境，将价格设置为1分钱
                break ifBlock;
            }
            log.error("[validatePayOrderPaid][order({}) payOrder({}) 支付金额不匹配，请进行处理！order 数据是：{}，payOrder 数据是：{}]",
                order.getId(), payOrderId, JsonUtils.toJsonString(order), JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_PRICE_NOT_MATCH);
        }
        // 2.2 校验支付订单匹配（二次）
        if (ObjectUtil.notEqual(payOrder.getMerchantOrderId(), order.getId().toString())) {
            log.error("[validatePayOrderPaid][order({}) 支付单不匹配({})，请进行处理！payOrder 数据是：{}]", order.getId(), payOrderId,
                JsonUtils.toJsonString(payOrder));
            throw exception(ORDER_UPDATE_PAID_FAIL_PAY_ORDER_ID_ERROR);
        }
        return payOrder;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_DELIVERY)
    public void deliveryOrder(TradeOrderDeliveryReqVO deliveryReqVO) {
        // 1.1 校验并获得交易订单（可发货）
        TradeOrderDO order = validateOrderDeliverable(deliveryReqVO.getId());
        // 1.2 校验 deliveryType 是否为快递，是快递才可以发货
        if (ObjectUtil.notEqual(order.getDeliveryType(), DeliveryTypeEnum.EXPRESS.getType())) {
            throw exception(ORDER_DELIVERY_FAIL_DELIVERY_TYPE_NOT_EXPRESS);
        }

        // 2. 更新订单为已发货
        TradeOrderDO updateOrderObj = new TradeOrderDO();
        // 2.1 快递发货
        DeliveryExpressDO express = null;
        if (ObjectUtil.notEqual(deliveryReqVO.getLogisticsId(), TradeOrderDO.LOGISTICS_ID_NULL)) {
            express = deliveryExpressService.validateDeliveryExpress(deliveryReqVO.getLogisticsId());
            updateOrderObj.setLogisticsId(deliveryReqVO.getLogisticsId())
                .setLogisticsNo(deliveryReqVO.getLogisticsNo());
        } else {
            // 2.2 无需发货
            updateOrderObj.setLogisticsId(0L).setLogisticsNo("");
        }
        // 执行更新
        updateOrderObj.setStatus(TradeOrderStatusEnum.DELIVERED.getStatus()).setDeliveryTime(LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateOrderObj);
        if (updateCount == 0) {
            throw exception(ORDER_DELIVERY_FAIL_STATUS_NOT_UNDELIVERED);
        }

        // 3. 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.DELIVERED.getStatus(),
            MapUtil.<String, Object>builder().put("deliveryName", express != null ? express.getName() : "")
                .put("logisticsNo", express != null ? deliveryReqVO.getLogisticsNo() : "").build());

        // 4.1 发送站内信
        tradeMessageService.sendMessageWhenDeliveryOrder(new TradeOrderMessageWhenDeliveryOrderReqBO()
            .setOrderId(order.getId()).setUserId(order.getUserId()).setMessage(null));
        // 4.2 发送订阅消息
        getSelf().sendDeliveryOrderMessage(order, deliveryReqVO);

        scheduleChannelShipping(order.getId(), true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliveryVirtualOrder(Long orderId) {
        TradeOrderDO order = validateOrderExists(orderId);
        if (!TradeOrderStatusEnum.isUndelivered(order.getStatus())) {
            return;
        }
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(orderItems)) {
            log.warn("[deliveryVirtualOrder][orderId={}] 订单项为空，跳过虚拟发货", orderId);
            return;
        }
        boolean allVirtual = orderItems.stream()
                .allMatch(item -> ProductSpuTypeEnum.isVirtual(item.getProductType()));
        if (!allVirtual) {
            log.warn("[deliveryVirtualOrder][orderId={}] 存在非虚拟商品，跳过自动发货", orderId);
            return;
        }

        TradeOrderDO updateOrder = new TradeOrderDO()
                .setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                .setDeliveryTime(LocalDateTime.now())
                .setLogisticsId(TradeOrderDO.LOGISTICS_ID_NULL)
                .setLogisticsNo("");
        int updateCount = tradeOrderMapper.updateByIdAndStatus(orderId, order.getStatus(), updateOrder);
        if (updateCount == 0) {
            log.info("[deliveryVirtualOrder][orderId={}] 状态已变更，忽略重复处理", orderId);
            return;
        }

        Integer originalStatus = order.getStatus();
        order.setStatus(updateOrder.getStatus())
                .setDeliveryTime(updateOrder.getDeliveryTime())
                .setLogisticsId(updateOrder.getLogisticsId())
                .setLogisticsNo(updateOrder.getLogisticsNo());

        TradeOrderLogUtils.setOrderInfo(orderId, originalStatus, TradeOrderStatusEnum.DELIVERED.getStatus(),
                MapUtil.<String, Object>builder()
                        .put("deliveryName", "虚拟发货")
                        .put("logisticsNo", "-")
                        .build());

        tradeMessageService.sendMessageWhenDeliveryOrder(new TradeOrderMessageWhenDeliveryOrderReqBO()
                .setOrderId(orderId).setUserId(order.getUserId()).setMessage(null));

        TradeOrderDeliveryReqVO deliveryReqVO = new TradeOrderDeliveryReqVO();
        deliveryReqVO.setId(orderId);
        deliveryReqVO.setLogisticsId(TradeOrderDO.LOGISTICS_ID_NULL);
        deliveryReqVO.setLogisticsNo("");
        getSelf().sendDeliveryOrderMessage(order, deliveryReqVO);
    }

    @Async
    public void sendDeliveryOrderMessage(TradeOrderDO order, TradeOrderDeliveryReqVO deliveryReqVO) {
        // 构建并发送模版消息
        Long orderId = order.getId();
        socialClientApi.sendWxaSubscribeMessage(new SocialWxaSubscribeMessageSendReqDTO().setUserId(order.getUserId())
            .setUserType(UserTypeEnum.MEMBER.getValue()).setTemplateTitle(WXA_ORDER_DELIVERY)
            .setPage("pages/order/detail?id=" + orderId) // 订单详情页
            .addMessage("character_string3", String.valueOf(orderId)) // 订单编号
            .addMessage("phrase6", TradeOrderStatusEnum.DELIVERED.getName()) // 订单状态
            .addMessage("date4", LocalDateTimeUtil.formatNormal(LocalDateTime.now()))// 发货时间
            .addMessage("character_string5", StrUtil.blankToDefault(deliveryReqVO.getLogisticsNo(), "-")) // 快递单号
            .addMessage("thing9", order.getReceiverDetailAddress())); // 收货地址
    }

    @Override
    public void triggerChannelShipping(Long orderId) {
        scheduleChannelShipping(orderId, true);
    }

    @Override
    public void pushChannelShipping(Long orderId) {
        scheduleChannelShipping(orderId, false);
    }

    /**
     * 校验交易订单满足被发货的条件
     * <p>
     * 1. 交易订单未发货
     *
     * @param id 交易订单编号
     * @return 交易订单
     */
    private TradeOrderDO validateOrderDeliverable(Long id) {
        TradeOrderDO order = validateOrderExists(id);
        // 1. 校验订单是否未发货
        if (ObjectUtil.notEqual(TradeOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_DELIVERY_FAIL_REFUND_STATUS_NOT_NONE);
        }

        // 2. 执行 TradeOrderHandler 前置处理
        tradeOrderHandlers.forEach(handler -> handler.beforeDeliveryOrder(order));
        return order;
    }

    private void scheduleChannelShipping(Long orderId, boolean allowDelay) {
        TradeOrderDO currentOrder = tradeOrderMapper.selectById(orderId);
        if (currentOrder == null) {
            log.warn("[scheduleChannelShipping][orderId={}] 订单不存在，跳过渠道发货调度", orderId);
            return;
        }
        if (currentOrder.getPayOrderId() == null) {
            log.warn("[scheduleChannelShipping][orderId={}] 缺少支付单编号，跳过渠道发货调度", orderId);
            return;
        }

        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(orderItems)) {
            log.warn("[scheduleChannelShipping][orderId={}] 订单项为空，跳过渠道发货调度", orderId);
            return;
        }

        PayOrderRespDTO payOrder = payOrderApi.getOrder(currentOrder.getPayOrderId());
        if (payOrder == null) {
            log.error("[scheduleChannelShipping][orderId={}] 未查询到支付单({})，无法推送渠道发货",
                    orderId, currentOrder.getPayOrderId());
            return;
        }

        if (isChannelAlreadyShipped(currentOrder, payOrder)) {
            if (allowDelay) {
                log.info("[scheduleChannelShipping][orderId={}] 渠道已存在发货记录，跳过重复推送", orderId);
                return;
            }
            throw exception(ORDER_CHANNEL_SHIPPING_ALREADY);
        }

        if (needsDelay(payOrder)) {
            if (!allowDelay) {
                throw exception(ORDER_CHANNEL_SHIPPING_WAITING);
            }
            TradeOrderChannelShippingMessage message = new TradeOrderChannelShippingMessage()
                    .setOrderId(orderId)
                    .setPayOrderId(currentOrder.getPayOrderId());
            tradeOrderEventProducer.sendChannelShippingMessage(message, ROCKETMQ_DELAY_LEVEL_1_MINUTE);
            log.info("[scheduleChannelShipping][orderId={}] 支付完成未满一分钟，提交延迟渠道发货消息", orderId);
            return;
        }

        doChannelShipping(currentOrder, orderItems);
    }

    private void doChannelShipping(TradeOrderDO order, List<TradeOrderItemDO> orderItems) {
        ChannelShippingRequestDTO reqDTO = new ChannelShippingRequestDTO();
        reqDTO.setOrderId(order.getId());
        reqDTO.setOrderNo(order.getNo());
        reqDTO.setPayOrderId(order.getPayOrderId());
        reqDTO.setUserId(order.getUserId());

        boolean allVirtual = orderItems.stream()
                .allMatch(item -> ProductSpuTypeEnum.isVirtual(item.getProductType()));
        reqDTO.setLogisticsType(allVirtual
                ? ChannelShippingLogisticsTypeEnum.VIRTUAL_GOODS.getType()
                : ChannelShippingLogisticsTypeEnum.EXPRESS.getType());
        reqDTO.setAllDelivered(allVirtual);

        if (order.getLogisticsId() != null
                && ObjectUtil.notEqual(order.getLogisticsId(), TradeOrderDO.LOGISTICS_ID_NULL)) {
            DeliveryExpressDO express = deliveryExpressService.getDeliveryExpress(order.getLogisticsId());
            reqDTO.setLogisticsCompanyCode(express != null ? express.getCode() : null);
        }
        reqDTO.setLogisticsNo(order.getLogisticsNo());
        reqDTO.setItemDesc(buildItemDesc(orderItems));
        reqDTO.setReceiverContact(order.getReceiverMobile());
        reqDTO.setItems(orderItems.stream().map(this::convertShippingItem).collect(Collectors.toList()));
        reqDTO.setRemark("后台触发渠道发货");

        try {
            channelShippingApi.ship(reqDTO);
        } catch (Exception ex) {
            log.error("[doChannelShipping][orderId={}] 渠道发货失败", order.getId(), ex);
        }
    }

    private String buildItemDesc(List<TradeOrderItemDO> orderItems) {
        String desc = orderItems.stream()
                .map(TradeOrderItemDO::getSpuName)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.joining("、"));
        if (StrUtil.length(desc) > 120) {
            desc = StrUtil.sub(desc, 0, 120);
        }
        return desc;
    }

    private boolean isChannelAlreadyShipped(TradeOrderDO order, PayOrderRespDTO payOrder) {
        if (payOrder == null) {
            return false;
        }
        String channelCode = payOrder.getChannelCode();
        if (StrUtil.isBlank(channelCode) || !StrUtil.containsIgnoreCase(channelCode, "wx")) {
            return false;
        }
        ChannelShippingQueryRequestDTO request = new ChannelShippingQueryRequestDTO();
        request.setOrderId(order.getId());
        request.setOrderNo(order.getNo());
        request.setPayOrderId(order.getPayOrderId());
        request.setDelivered(TradeOrderStatusEnum.haveDelivered(order.getStatus()));
        try {
            ChannelShippingQueryRespDTO resp = channelShippingApi.query(request);
            return resp != null && resp.isShipped();
        } catch (Exception ex) {
            log.warn("[isChannelAlreadyShipped][orderId={}] 渠道发货状态查询失败", order.getId(), ex);
            return false;
        }
    }

    private boolean needsDelay(PayOrderRespDTO payOrder) {
        LocalDateTime successTime = payOrder.getSuccessTime();
        if (successTime == null) {
            return true;
        }
        Duration duration = Duration.between(successTime, LocalDateTime.now());
        return duration.toSeconds() < 60;
    }

    private ChannelShippingRequestDTO.Item convertShippingItem(TradeOrderItemDO item) {
        // 构造精简的订单项信息，避免在履约层暴露交易域内部字段
        ChannelShippingRequestDTO.Item dto = new ChannelShippingRequestDTO.Item();
        dto.setOrderItemId(item.getId());
        dto.setSpuId(item.getSpuId());
        dto.setSpuName(item.getSpuName());
        dto.setProductType(item.getProductType());
        dto.setCount(item.getCount());
        return dto;
    }

    @NotNull
    private TradeOrderDO validateOrderExists(Long id) {
        // 校验订单是否存在
        TradeOrderDO order = tradeOrderMapper.selectById(id);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_RECEIVE)
    public void receiveOrderByMember(Long userId, Long id) {
        // 校验并获得交易订单（可收货）
        TradeOrderDO order = validateOrderReceivable(userId, id);

        // 收货订单
        receiveOrder0(order, LocalDateTime.now());
    }

    @Override
    public int receiveOrderBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getReceiveExpireTime());
        List<TradeOrderDO> orders = tradeOrderMapper
            .selectListByStatusAndDeliveryTimeLt(TradeOrderStatusEnum.DELIVERED.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                getSelf().receiveOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[receiveOrderBySystem][order({}) 自动收货订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 自动收货单个订单
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_RECEIVE)
    public void receiveOrderBySystem(TradeOrderDO order) {
        receiveOrder0(order, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_RECEIVE)
    public void receiveOrderByChannel(TradeOrderChannelReceiveReqDTO reqDTO) {
        if (reqDTO == null || StrUtil.isBlank(reqDTO.getMerchantTradeNo())) {
            log.warn("[receiveOrderByChannel][收到空的渠道收货消息，忽略]");
            return;
        }
        TradeOrderDO order = tradeOrderMapper.selectByNo(reqDTO.getMerchantTradeNo());
        if (order == null) {
            log.warn("[receiveOrderByWechat][订单编号({}) 不存在]", reqDTO.getMerchantTradeNo());
            return;
        }
        if (TradeOrderStatusEnum.isCompleted(order.getStatus())) {
            log.debug("[receiveOrderByWechat][订单({}) 已完成，无需处理]", order.getId());
            return;
        }
        if (!TradeOrderStatusEnum.isDelivered(order.getStatus())) {
            log.info("[receiveOrderByWechat][订单({}) 状态({}) 非待收货，忽略回调]", order.getId(), order.getStatus());
            return;
        }
        LocalDateTime receiveTime = ObjUtil.defaultIfNull(reqDTO.getConfirmReceiveTime(),
                ObjUtil.defaultIfNull(reqDTO.getSettlementTime(), LocalDateTime.now()));
        try {
            receiveOrder0(order, receiveTime);
        } catch (ServiceException ex) {
            if (ex.getCode().equals(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED.getCode())) {
                log.info("[receiveOrderByChannel][订单({}) 状态已变化，忽略回调]", order.getId());
                return;
            }
            throw ex;
        }
    }

    /**
     * 确保订单已经进入“待收货”状态。若当前仍处于“待发货”且满足发货条件，则补齐发货状态。
     *
     * @param order 当前订单快照
     * @return true - 订单最终处于待收货/已完成；false - 状态不满足或补齐失败
     */
    private boolean ensureDeliveredStatus(TradeOrderDO order) {
        if (order == null) {
            return false;
        }
        if (TradeOrderStatusEnum.isDelivered(order.getStatus())
                || TradeOrderStatusEnum.isCompleted(order.getStatus())) {
            return true;
        }
        if (!TradeOrderStatusEnum.isUndelivered(order.getStatus())) {
            return false;
        }
        TradeOrderDO updateObj = new TradeOrderDO()
                .setStatus(TradeOrderStatusEnum.DELIVERED.getStatus())
                .setDeliveryTime(LocalDateTime.now());
        int update = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), updateObj);
        if (update == 0) {
            return false;
        }
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(),
                TradeOrderStatusEnum.DELIVERED.getStatus());
        order.setStatus(updateObj.getStatus());
        order.setDeliveryTime(updateObj.getDeliveryTime());
        order.setLogisticsId(ObjectUtil.defaultIfNull(order.getLogisticsId(), updateObj.getLogisticsId()));
        order.setLogisticsNo(ObjectUtil.defaultIfNull(order.getLogisticsNo(), updateObj.getLogisticsNo()));
        return true;
    }

    /**
     * 收货订单的核心实现
     *
     * @param order 订单
     */
    private void receiveOrder0(TradeOrderDO order, LocalDateTime receiveTime) {
        LocalDateTime finalReceiveTime = ObjUtil.defaultIfNull(receiveTime, LocalDateTime.now());
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(), new TradeOrderDO()
            .setStatus(TradeOrderStatusEnum.COMPLETED.getStatus()).setReceiveTime(finalReceiveTime));
        if (updateCount == 0) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }

        // 插入订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus());

        settleBrokerageAfterReceive(order.getId());
    }

    private void receiveOrder0(TradeOrderDO order) {
        receiveOrder0(order, LocalDateTime.now());
    }

    /**
     * 校验交易订单满足可售货的条件
     * <p>
     * 1. 交易订单待收货
     *
     * @param userId 用户编号
     * @param id 交易订单编号
     * @return 交易订单
     */
    private TradeOrderDO validateOrderReceivable(Long userId, Long id) {
        // 校验订单是否存在
        TradeOrderDO order = tradeOrderMapper.selectByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 校验订单是否是待收货状态
        if (!TradeOrderStatusEnum.isDelivered(order.getStatus())) {
            throw exception(ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_CANCEL)
    public void cancelOrderByMember(Long userId, Long id) {
        // 1.1 校验存在
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.2 校验状态
        if (ObjectUtil.notEqual(order.getStatus(), TradeOrderStatusEnum.UNPAID.getStatus())) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }

        // 2. 取消订单
        cancelOrder0(order, TradeOrderCancelTypeEnum.MEMBER_CANCEL);
    }

    /**
     * 订单完成后按订单项编号解冻对应的待结算佣金。
     */
    private void settleBrokerageAfterReceive(Long orderId) {
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(orderId);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }
        Set<String> bizIds = orderItems.stream()
                .map(item -> String.valueOf(item.getId()))
                .collect(Collectors.toSet());
        try {
            brokerageRecordService.settleWaitUnfreezeByBizIds(bizIds);
        } catch (Exception ex) {
            log.error("[settleBrokerageAfterReceive][orderId={}] 佣金解冻处理失败", orderId, ex);
        }
    }

    /** 确保支付状态已经记录，返回最新的订单快照；若支付记录早已存在则保留当前状态。 */
    private TradeOrderDO recordPaymentIfNecessary(TradeOrderDO snapshot, Long payOrderId) {
        boolean alreadyPaid = Boolean.TRUE.equals(snapshot.getPayStatus());
        boolean payOrderMatched = ObjectUtil.equals(snapshot.getPayOrderId(), payOrderId);
        if (alreadyPaid && payOrderMatched) {
            return snapshot;
        }

        try {
            getSelf().updateOrderPaid(snapshot.getId(), payOrderId);
        } catch (ServiceException ex) {
            if (!alreadyPaid || !payOrderMatched) {
                throw ex;
            }
            log.debug("[recordPaymentIfNecessary][order({}) 支付状态已存在]", snapshot.getId());
        }
        return tradeOrderMapper.selectById(snapshot.getId());
    }

    private String normalizeDeliveryStatus(String status) {
        return StrUtil.isBlank(status) ? null : status.trim().toUpperCase();
    }

    private boolean isDeliveredStatus(String normalizedStatus) {
        return StrUtil.equalsAny(normalizedStatus, "DELIVERED", "NOREQUIRED");
    }

    @Override
    public int cancelOrderBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getPayExpireTime());
        List<TradeOrderDO> orders =
            tradeOrderMapper.selectListByStatusAndCreateTimeLt(TradeOrderStatusEnum.UNPAID.getStatus(), expireTime);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                getSelf().cancelOrderBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[cancelOrderBySystem][order({}) 过期订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    /**
     * 自动取消单个订单
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_CANCEL)
    public void cancelOrderBySystem(TradeOrderDO order) {
        cancelOrder0(order, TradeOrderCancelTypeEnum.PAY_TIMEOUT);
    }

    /**
     * 取消订单的核心实现
     *
     * @param order 订单
     * @param cancelType 取消类型
     */
    private void cancelOrder0(TradeOrderDO order, TradeOrderCancelTypeEnum cancelType) {
        // 1. 更新 TradeOrderDO 状态为已取消
        int updateCount = tradeOrderMapper.updateByIdAndStatus(order.getId(), order.getStatus(),
            new TradeOrderDO().setStatus(TradeOrderStatusEnum.CANCELED.getStatus()).setCancelType(cancelType.getType())
                .setCancelTime(LocalDateTime.now()));
        if (updateCount == 0) {
            throw exception(ORDER_CANCEL_FAIL_STATUS_NOT_UNPAID);
        }

        // 2. 执行 TradeOrderHandler 的后置处理
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrder(order, orderItems));

        // 3. 增加订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus());
    }

    /**
     * 如果金额全部被退款，则取消订单 如果还有未被退款的金额，则无需取消订单
     *
     * @param order 订单
     * @param refundPrice 退款金额
     */
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_CANCEL_AFTER_SALE)
    public void cancelOrderByAfterSale(TradeOrderDO order, Integer refundPrice) {
        // 1. 更新订单
        if (refundPrice < order.getPayPrice()) {
            return;
        }
        tradeOrderMapper
            .updateById(new TradeOrderDO().setId(order.getId()).setStatus(TradeOrderStatusEnum.CANCELED.getStatus())
                .setCancelType(TradeOrderCancelTypeEnum.AFTER_SALE_CLOSE.getType()).setCancelTime(LocalDateTime.now()));

        // 2. 执行 TradeOrderHandler 的后置处理
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrder(order, orderItems));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_DELETE)
    public void deleteOrder(Long userId, Long id) {
        // 1.1 校验存在
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(id, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        // 1.2 校验状态
        if (ObjectUtil.notEqual(order.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus())) {
            throw exception(ORDER_DELETE_FAIL_STATUS_NOT_CANCEL);
        }
        // 2. 删除订单
        tradeOrderMapper.deleteById(id);

        // 3. 记录日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    @Override
    public void updateOrderRemark(TradeOrderRemarkReqVO reqVO) {
        // 校验并获得交易订单
        validateOrderExists(reqVO.getId());

        // 更新
        TradeOrderDO order = TradeOrderConvert.INSTANCE.convert(reqVO);
        tradeOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_UPDATE_PRICE)
    public void updateOrderPrice(TradeOrderUpdatePriceReqVO reqVO) {
        // 1.1 校验交易订单
        TradeOrderDO order = validateOrderExists(reqVO.getId());
        if (order.getPayStatus()) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PAID);
        }
        // 1.2 校验调价金额是否变化
        if (order.getAdjustPrice() > 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_ALREADY);
        }
        // 1.3 支付价格不能为 0
        int newPayPrice = order.getPayPrice() + reqVO.getAdjustPrice();
        if (newPayPrice <= 0) {
            throw exception(ORDER_UPDATE_PRICE_FAIL_PRICE_ERROR);
        }

        // 2. 更新订单
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId())
            .setAdjustPrice(reqVO.getAdjustPrice() + order.getAdjustPrice()).setPayPrice(newPayPrice));

        // 3. 更新 TradeOrderItem，需要做 adjustPrice 的分摊
        List<TradeOrderItemDO> orderOrderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        List<Integer> dividePrices = TradePriceCalculatorHelper.dividePrice2(orderOrderItems, reqVO.getAdjustPrice());
        List<TradeOrderItemDO> updateItems = new ArrayList<>();
        for (int i = 0; i < orderOrderItems.size(); i++) {
            TradeOrderItemDO item = orderOrderItems.get(i);
            updateItems.add(
                new TradeOrderItemDO().setId(item.getId()).setAdjustPrice(item.getAdjustPrice() + dividePrices.get(i))
                    .setPayPrice(item.getPayPrice() + dividePrices.get(i)));
        }
        tradeOrderItemMapper.updateBatch(updateItems);

        // 4. 更新支付订单
        payOrderApi.updatePayOrderPrice(order.getPayOrderId(), newPayPrice);

        // 5. 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus(),
            MapUtil.<String, Object>builder().put("oldPayPrice", MoneyUtils.fenToYuanStr(order.getPayPrice()))
                .put("adjustPrice", MoneyUtils.fenToYuanStr(reqVO.getAdjustPrice()))
                .put("newPayPrice", MoneyUtils.fenToYuanStr(newPayPrice)).build());
    }

    @Override
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_UPDATE_ADDRESS)
    public void updateOrderAddress(TradeOrderUpdateAddressReqVO reqVO) {
        // 校验交易订单
        TradeOrderDO order = validateOrderExists(reqVO.getId());
        // 只有待发货状态，才可以修改订单收货地址；
        if (!TradeOrderStatusEnum.isUndelivered(order.getStatus())) {
            throw exception(ORDER_UPDATE_ADDRESS_FAIL_STATUS_NOT_DELIVERED);
        }

        // 更新
        tradeOrderMapper.updateById(TradeOrderConvert.INSTANCE.convert(reqVO));

        // 记录订单日志
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    @Override
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_PICK_UP_RECEIVE)
    public void pickUpOrderByAdmin(Long userId, Long id) {
        getSelf().pickUpOrder(userId, tradeOrderMapper.selectById(id));
    }

    @Override
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_PICK_UP_RECEIVE)
    public void pickUpOrderByAdmin(Long userId, String pickUpVerifyCode) {
        getSelf().pickUpOrder(userId, tradeOrderMapper.selectOneByPickUpVerifyCode(pickUpVerifyCode));
    }

    @Override
    public TradeOrderDO getByPickUpVerifyCode(String pickUpVerifyCode) {
        return tradeOrderMapper.selectOneByPickUpVerifyCode(pickUpVerifyCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public void pickUpOrder(Long userId, TradeOrderDO order) {
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (ObjUtil.notEqual(DeliveryTypeEnum.PICK_UP.getType(), order.getDeliveryType())) {
            throw exception(ORDER_RECEIVE_FAIL_DELIVERY_TYPE_NOT_PICK_UP);
        }
        DeliveryPickUpStoreDO deliveryPickUpStore = pickUpStoreService.getDeliveryPickUpStore(order.getPickUpStoreId());
        if (deliveryPickUpStore == null || !CollUtil.contains(deliveryPickUpStore.getVerifyUserIds(), userId)) {
            throw exception(ORDER_PICK_UP_FAIL_NOT_VERIFY_USER);
        }

        receiveOrder0(order, LocalDateTime.now());
    }

    // =================== Order Item ===================

    @Override
    public void updateOrderItemWhenAfterSaleCreate(Long id, Long afterSaleId) {
        // 更新订单项
        updateOrderItemAfterSaleStatus(id, TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(),
            TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(), afterSaleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderItemWhenAfterSaleSuccess(Long id, Integer refundPrice) {
        // 1.1 更新订单项
        updateOrderItemAfterSaleStatus(id, TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
            TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus(), null);
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectById(id);
        TradeOrderDO order = tradeOrderMapper.selectById(orderItem.getOrderId());
        // 1.2 留待退款成功事件统一触发领域补偿（物料、佣金等），避免与 MQ 入口重复

        // 2.1 更新订单的退款金额、积分
        Integer orderRefundPrice = order.getRefundPrice() + refundPrice;
        Integer orderRefundPoint = order.getRefundPoint() + orderItem.getUsePoint();
        Integer refundStatus =
            isAllOrderItemAfterSaleSuccess(order.getId()) ? TradeOrderRefundStatusEnum.ALL.getStatus() // 如果都售后成功，则需要取消订单
                : TradeOrderRefundStatusEnum.PART.getStatus();
        tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setRefundStatus(refundStatus)
            .setRefundPrice(orderRefundPrice).setRefundPoint(orderRefundPoint));
        // 2.2 如果全部退款，则进行取消订单
        getSelf().cancelOrderByAfterSale(order, orderRefundPrice);
    }

    @Override
    public void updateOrderItemWhenAfterSaleCancel(Long id) {
        // 更新订单项
        updateOrderItemAfterSaleStatus(id, TradeOrderItemAfterSaleStatusEnum.APPLY.getStatus(),
            TradeOrderItemAfterSaleStatusEnum.NONE.getStatus(), null);
    }

    private void updateOrderItemAfterSaleStatus(Long id, Integer oldAfterSaleStatus, Integer newAfterSaleStatus,
        Long afterSaleId) {
        // 更新订单项
        int updateCount =
            tradeOrderItemMapper.updateAfterSaleStatus(id, oldAfterSaleStatus, newAfterSaleStatus, afterSaleId);
        if (updateCount <= 0) {
            throw exception(ORDER_ITEM_UPDATE_AFTER_SALE_STATUS_FAIL);
        }

    }

    /**
     * 判断指定订单的所有订单项，是不是都售后成功
     *
     * @param id 订单编号
     * @return 是否都售后成功
     */
    private boolean isAllOrderItemAfterSaleSuccess(Long id) {
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(id);
        return orderItems.stream().allMatch(orderItem -> Objects.equals(orderItem.getAfterSaleStatus(),
            TradeOrderItemAfterSaleStatusEnum.SUCCESS.getStatus()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.MEMBER_COMMENT)
    public Long createOrderItemCommentByMember(Long userId, AppTradeOrderItemCommentCreateReqVO createReqVO) {
        // 1.1 先通过订单项 ID，查询订单项是否存在
        TradeOrderItemDO orderItem = tradeOrderItemMapper.selectByIdAndUserId(createReqVO.getOrderItemId(), userId);
        if (orderItem == null) {
            throw exception(ORDER_ITEM_NOT_FOUND);
        }
        // 1.2 校验订单相关状态
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderItem.getOrderId(), userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }
        if (ObjectUtil.notEqual(order.getStatus(), TradeOrderStatusEnum.COMPLETED.getStatus())) {
            throw exception(ORDER_COMMENT_FAIL_STATUS_NOT_COMPLETED);
        }
        if (ObjectUtil.notEqual(order.getCommentStatus(), Boolean.FALSE)) {
            throw exception(ORDER_COMMENT_STATUS_NOT_FALSE);
        }

        // 2. 创建评价
        Long commentId = createOrderItemComment0(orderItem, createReqVO);

        // 3. 如果订单项都评论了，则更新订单评价状态
        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(order.getId());
        if (!anyMatch(orderItems, item -> Objects.equals(item.getCommentStatus(), Boolean.FALSE))) {
            tradeOrderMapper.updateById(new TradeOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE)
                .setFinishTime(LocalDateTime.now()));
            // 增加订单日志。注意：只有在所有订单项都评价后，才会增加
            TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
        }
        return commentId;
    }

    @Override
    public int createOrderItemCommentBySystem() {
        // 1. 查询过期的待支付订单
        LocalDateTime expireTime = minusTime(tradeOrderProperties.getCommentExpireTime());
        List<TradeOrderDO> orders = tradeOrderMapper
            .selectListByStatusAndReceiveTimeLt(TradeOrderStatusEnum.COMPLETED.getStatus(), expireTime, false);
        if (CollUtil.isEmpty(orders)) {
            return 0;
        }

        // 2. 遍历执行，逐个取消
        int count = 0;
        for (TradeOrderDO order : orders) {
            try {
                getSelf().createOrderItemCommentBySystemBySystem(order);
                count++;
            } catch (Throwable e) {
                log.error("[createOrderItemCommentBySystem][order({}) 过期订单异常]", order.getId(), e);
            }
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderCombinationInfo(Long orderId, Long activityId, Long combinationRecordId, Long headId) {
        tradeOrderMapper.updateById(new TradeOrderDO().setId(orderId).setCombinationActivityId(activityId)
            .setCombinationRecordId(combinationRecordId).setCombinationHeadId(headId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelPaidOrder(Long userId, Long orderId, Integer cancelType) {
        // 1.1 这里校验下 cancelType 只允许拼团关闭；
        if (ObjUtil.notEqual(TradeOrderCancelTypeEnum.COMBINATION_CLOSE.getType(), cancelType)) {
            return;
        }
        // 1.2 检验订单存在
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }

        // 1.3 校验订单是否支付
        if (!order.getPayStatus()) {
            throw exception(ORDER_CANCEL_PAID_FAIL, "已支付");
        }
        // 1.3 校验订单是否未退款
        if (ObjUtil.notEqual(TradeOrderRefundStatusEnum.NONE.getStatus(), order.getRefundStatus())) {
            throw exception(ORDER_CANCEL_PAID_FAIL, "未退款");
        }

        // 1.4 退款前资源充足性校验（物料余额、佣金结算状态、权益回收策略等）
        List<TradeOrderItemDO> items = tradeOrderItemMapper.selectListByOrderId(order.getId());
        refundPreCheckService.validate(order, items);

        // 2.1 取消订单
        cancelOrder0(order, TradeOrderCancelTypeEnum.COMBINATION_CLOSE);
        // 2.2 创建退款单
        payRefundApi.createRefund(new PayRefundCreateReqDTO().setAppKey(tradeOrderProperties.getPayAppKey()) // 支付应用
            .setUserIp(NetUtil.getLocalhostStr()) // 使用本机 IP，因为是服务器发起退款的
            .setMerchantOrderId(String.valueOf(order.getId())) // 支付单号
            .setMerchantRefundId(String.valueOf(order.getId()))
            .setReason(TradeOrderCancelTypeEnum.COMBINATION_CLOSE.getName()).setPrice(order.getPayPrice())); // 价格信息
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.ADMIN_REFUND)
    public void adminRefundOrder(Long orderId, String reason, String password) {
        validateRefundPassword(password);
        TradeOrderDO order = validateOrderExists(orderId);
        if (!BooleanUtil.isTrue(order.getPayStatus()) || !TradeOrderStatusEnum.havePaid(order.getStatus())) {
            throw exception(ORDER_REFUND_FAIL_STATUS_NOT_PAID);
        }
        if (Objects.equals(order.getRefundStatus(), TradeOrderRefundStatusEnum.ALL.getStatus())) {
            throw exception(ORDER_REFUND_FAIL_ALREADY);
        }
        if (order.getPayOrderId() == null) {
            throw exception(ORDER_REFUND_FAIL_PAYMENT_NOT_FOUND);
        }
        int totalPayPrice = ObjectUtil.defaultIfNull(order.getPayPrice(), 0);
        int refundedPrice = ObjectUtil.defaultIfNull(order.getRefundPrice(), 0);
        int refundPrice = totalPayPrice - refundedPrice;
        if (refundPrice <= 0) {
            throw exception(ORDER_REFUND_FAIL_PRICE_INVALID);
        }

        List<TradeOrderItemDO> orderItems = tradeOrderItemMapper.selectListByOrderId(orderId);
        refundPreCheckService.validate(order, orderItems);

        String refundReason = StrUtil.blankToDefault(reason, "后台手动退款");
        PayRefundCreateReqDTO reqDTO = new PayRefundCreateReqDTO()
            .setAppKey(tradeOrderProperties.getPayAppKey())
            .setUserIp(StrUtil.blankToDefault(ServletUtils.getClientIP(), NetUtil.getLocalhostStr()))
            .setMerchantOrderId(String.valueOf(order.getId()))
            .setMerchantRefundId(buildAdminRefundId(order))
            .setPrice(refundPrice)
            .setReason(refundReason);
        payRefundApi.createRefund(reqDTO);

        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), TradeOrderStatusEnum.CANCELED.getStatus(),
            Collections.singletonMap("reason", refundReason));

        TradeOrderDO updateObj = new TradeOrderDO().setId(order.getId())
            .setStatus(TradeOrderStatusEnum.CANCELED.getStatus())
            .setCancelType(TradeOrderCancelTypeEnum.AFTER_SALE_CLOSE.getType())
            .setCancelTime(LocalDateTime.now())
            .setRefundStatus(TradeOrderRefundStatusEnum.ALL.getStatus())
            .setRefundPrice(totalPayPrice)
            .setRefundPoint(ObjectUtil.defaultIfNull(order.getUsePoint(), 0));
        tradeOrderMapper.updateById(updateObj);

        order.setStatus(updateObj.getStatus());
        order.setRefundStatus(updateObj.getRefundStatus());
        order.setRefundPrice(updateObj.getRefundPrice());
        order.setRefundPoint(updateObj.getRefundPoint());

        tradeOrderHandlers.forEach(handler -> handler.afterCancelOrder(order, orderItems));
    }

    @Override
    public void updateOrderGiveCouponIds(Long userId, Long orderId, List<Long> giveCouponIds) {
        // 1. 检验订单存在
        TradeOrderDO order = tradeOrderMapper.selectOrderByIdAndUserId(orderId, userId);
        if (order == null) {
            throw exception(ORDER_NOT_FOUND);
        }

        // 2. 更新订单赠送的优惠券编号列表
        tradeOrderMapper.updateById(new TradeOrderDO().setId(orderId).setGiveCouponIds(giveCouponIds));
    }

    /**
     * 创建单个订单的评论
     *
     * @param order 订单
     */
    @Transactional(rollbackFor = Exception.class)
    @TradeOrderLog(operateType = TradeOrderOperateTypeEnum.SYSTEM_COMMENT)
    public void createOrderItemCommentBySystemBySystem(TradeOrderDO order) {
        // 1. 查询未评论的订单项
        List<TradeOrderItemDO> orderItems =
            tradeOrderItemMapper.selectListByOrderIdAndCommentStatus(order.getId(), Boolean.FALSE);
        if (CollUtil.isEmpty(orderItems)) {
            return;
        }

        // 2. 逐个评论
        for (TradeOrderItemDO orderItem : orderItems) {
            // 2.1 创建评价
            AppTradeOrderItemCommentCreateReqVO commentCreateReqVO =
                new AppTradeOrderItemCommentCreateReqVO().setOrderItemId(orderItem.getId()).setAnonymous(false)
                    .setContent("").setBenefitScores(5).setDescriptionScores(5);
            createOrderItemComment0(orderItem, commentCreateReqVO);

            // 2.2 更新订单项评价状态
            tradeOrderItemMapper
                .updateById(new TradeOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        }

        // 3. 所有订单项都评论了，则更新订单评价状态
        tradeOrderMapper.updateById(
            new TradeOrderDO().setId(order.getId()).setCommentStatus(Boolean.TRUE).setFinishTime(LocalDateTime.now()));
        // 增加订单日志。注意：只有在所有订单项都评价后，才会增加
        TradeOrderLogUtils.setOrderInfo(order.getId(), order.getStatus(), order.getStatus());
    }

    /**
     * 创建订单项的评论的核心实现
     *
     * @param orderItem 订单项
     * @param createReqVO 评论内容
     * @return 评论编号
     */
    private Long createOrderItemComment0(TradeOrderItemDO orderItem, AppTradeOrderItemCommentCreateReqVO createReqVO) {
        // 1. 创建评价
        ProductCommentCreateReqDTO productCommentCreateReqDTO =
            TradeOrderConvert.INSTANCE.convert04(createReqVO, orderItem);
        Long commentId = productCommentApi.createComment(productCommentCreateReqDTO);

        // 2. 更新订单项评价状态
        tradeOrderItemMapper.updateById(new TradeOrderItemDO().setId(orderItem.getId()).setCommentStatus(Boolean.TRUE));
        return commentId;
    }

    private String buildAdminRefundId(TradeOrderDO order) {
        return "TRR" + order.getId() + "-" + IdGeneratorUtil.nextId();
    }

    private void validateRefundPassword(String password) {
        String expected = tradeOrderProperties.getAdminRefundPassword();
        if (StrUtil.isBlank(expected)) {
            return;
        }
        if (StrUtil.isBlank(password) || !StrUtil.equals(expected, password)) {
            throw exception(ORDER_REFUND_FAIL_PASSWORD_INVALID);
        }
    }

    @Override
    public boolean isAdminRefundPasswordEnabled() {
        return StrUtil.isNotBlank(tradeOrderProperties.getAdminRefundPassword());
    }

    // =================== 营销相关的操作 ===================

    /**
     * 发送订单支付成功MQ消息
     * 使用精简的消息格式，只包含核心标识信息
     *
     * @param order 订单信息
     * @param payTime 支付时间
     */
    private void sendOrderPaymentSuccessMessage(TradeOrderDO order, LocalDateTime payTime) {
        try {
            // 转换为精简消息对象
            TradeOrderPaymentSuccessMessage message = tradeOrderEventConvert.convertToPaymentSuccessMessage(
                order, payTime);

            // 同步发送消息，确保事务一致性
            boolean success = tradeOrderEventProducer.sendOrderPaymentSuccessMessage(message);

            if (!success) {
                log.error("[sendOrderPaymentSuccessMessage][订单支付成功消息发送失败: orderId={}]", order.getId());
                throw new RuntimeException("订单支付成功消息发送失败");
            }

            log.info("[sendOrderPaymentSuccessMessage][订单支付成功消息发送成功: orderId={}]", order.getId());
        } catch (Exception e) {
            log.error("[sendOrderPaymentSuccessMessage][订单支付成功消息发送异常: orderId={}]", order.getId(), e);
            throw new RuntimeException("订单支付成功消息发送失败", e);
        }
    }



    /**
     * 获得自身的代理对象，解决 AOP 生效问题
     *
     * @return 自己
     */
    private TradeOrderUpdateServiceImpl getSelf() {
        return SpringUtil.getBean(getClass());
    }
}
