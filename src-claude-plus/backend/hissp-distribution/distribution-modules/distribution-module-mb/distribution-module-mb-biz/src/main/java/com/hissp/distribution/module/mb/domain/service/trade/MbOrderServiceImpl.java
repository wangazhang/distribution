package com.hissp.distribution.module.mb.domain.service.trade;

import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.id.IdGeneratorUtil;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.framework.common.util.servlet.ServletUtils;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.fulfillmentchannel.api.ChannelShippingApi;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRequestDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.WeixinOrderConfirmExtraRespDTO;
import com.hissp.distribution.module.fulfillmentchannel.api.dto.ChannelShippingQueryRespDTO.ChannelShippingConfirmStatus;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderDetailRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderSaveReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.mbdt.price.vo.AppMbPriceRespVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderCreateReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderRespVO;
import com.hissp.distribution.module.mb.adapter.controller.app.trade.vo.AppMbOrderWeixinExtraRespVO;
import com.hissp.distribution.module.mb.constants.MbOrderBizType;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbOrderMapper;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionVirtualAccounts;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler.MaterialConvertOrderHandler;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler.MaterialRestockOrderHandler;
import com.hissp.distribution.module.mb.domain.service.mbdt.price.MaterialRestockPriceService;
import com.hissp.distribution.module.mb.enums.MbOrderStatusEnum;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.pay.api.order.PayOrderApi;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderCreateReqDTO;
import com.hissp.distribution.module.pay.api.order.dto.PayOrderRespDTO;
import com.hissp.distribution.module.pay.api.refund.PayRefundApi;
import com.hissp.distribution.module.pay.api.refund.dto.PayRefundCreateReqDTO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.api.config.TradeConfigApi;
import com.hissp.distribution.module.trade.api.config.dto.TradeConfigRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import cn.hutool.core.util.StrUtil;
import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.mb.enums.ErrorCodeConstants.*;

/**
 * mb订单表：记录代理用户的mb所有业务类型的订单信息 Service 实现类
 *
 * @author azhanga
 */
@Service
@Validated
@Slf4j
public class MbOrderServiceImpl implements MbOrderService {

    private static final String MB_APP_KEY = "mb";
    @Value("${distribution.mb.order.admin-refund-password:}")
    private String adminRefundPassword;

    @Resource
    private MbOrderMapper mbOrderMapper;

    @Resource
    private MaterialRestockPriceService materialRestockPriceService;

    @Resource
    private PayOrderApi payOrderApi;

    @Resource
    private PayRefundApi payRefundApi;

    @Resource
    private PayAppService payAppService;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private BrokerageUserApi brokerageUserApi;
    @Resource
    private BrokerageRecordApi brokerageRecordApi;

    @Resource
    private MbVirtualDeliveryService mbVirtualDeliveryService;

    @Resource
    private ChannelShippingApi channelShippingApi;


    @Resource
    private MaterialApi materialApi;

    @Resource
    private MaterialRestockOrderHandler materialRestockOrderHandler;

    @Resource
    private MaterialConvertOrderHandler materialConvertOrderHandler;

    @Resource
    private TradeConfigApi tradeConfigApi;

    @Override
    public Long createMbOrder(MbOrderSaveReqVO createReqVO) {
        // 插入
        MbOrderDO mbOrder = BeanUtils.toBean(createReqVO, MbOrderDO.class);
        mbOrderMapper.insert(mbOrder);
        // 返回
        return mbOrder.getId();
    }

    @Override
    public void updateMbOrder(MbOrderSaveReqVO updateReqVO) {
        // 校验存在
        validateMbOrderExists(updateReqVO.getId());
        // 更新
        MbOrderDO updateObj = BeanUtils.toBean(updateReqVO, MbOrderDO.class);
        mbOrderMapper.updateById(updateObj);
    }

    @Override
    public void deleteMbOrder(Long id) {
        // 校验存在
        validateMbOrderExists(id);
        // 删除
        mbOrderMapper.deleteById(id);
    }

    private void validateMbOrderExists(Long id) {
        if (mbOrderMapper.selectById(id) == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }
    }

    @Override
    public MbOrderDO getMbOrder(Long id) {
        return mbOrderMapper.selectById(id);
    }

    @Override
    public PageResult<MbOrderDO> getMbOrderPage(MbOrderPageReqVO pageReqVO) {
        return mbOrderMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<MbOrderRespVO> getAdminOrderPage(MbOrderPageReqVO pageReqVO) {
        PageResult<MbOrderRespVO> earlyResult = preprocessAdminOrderFilters(pageReqVO);
        if (earlyResult != null) {
            return earlyResult;
        }
        PageResult<MbOrderDO> pageResult = mbOrderMapper.selectPage(pageReqVO);
        if (pageResult == null || pageResult.getList() == null || pageResult.getList().isEmpty()) {
            return PageResult.empty(pageResult != null ? pageResult.getTotal() : 0L);
        }

        Set<Long> userIds = pageResult.getList().stream()
            .map(MbOrderDO::getAgentUserId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Map<Long, MemberUserRespDTO> userMap =
            userIds.isEmpty() ? Collections.emptyMap() : memberUserApi.getUserMap(userIds);

        List<MbOrderRespVO> respList = pageResult.getList().stream()
            .map(order -> convertToAdminSummary(order, userMap))
            .collect(Collectors.toList());
        return new PageResult<>(respList, pageResult.getTotal());
    }

    private PageResult<MbOrderRespVO> preprocessAdminOrderFilters(MbOrderPageReqVO pageReqVO) {
        if (pageReqVO == null) {
            return null;
        }
        if (StringUtils.hasText(pageReqVO.getOrderNo())) {
            Long orderId = parseOrderIdFromOrderNo(pageReqVO.getOrderNo());
            if (orderId == null) {
                return PageResult.empty();
            }
            pageReqVO.setOrderId(orderId);
        }
        if (StringUtils.hasText(pageReqVO.getAgentUserName())) {
            List<MemberUserRespDTO> users = memberUserApi.getUserListByNickname(pageReqVO.getAgentUserName());
            if (CollectionUtils.isEmpty(users)) {
                return PageResult.empty();
            }
            List<Long> userIds = users.stream()
                    .map(MemberUserRespDTO::getId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(userIds)) {
                return PageResult.empty();
            }
            pageReqVO.setAgentUserIds(userIds);
        }
        if (StringUtils.hasText(pageReqVO.getProductName())) {
            List<MaterialDefinitionRespDTO> definitions = materialApi.getDefinitionListByName(pageReqVO.getProductName());
            if (CollectionUtils.isEmpty(definitions)) {
                return PageResult.empty();
            }
            List<Long> productIds = definitions.stream()
                    .map(MaterialDefinitionRespDTO::getSpuId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isEmpty(productIds)) {
                return PageResult.empty();
            }
            pageReqVO.setProductIds(productIds);
        }
        return null;
    }

    @Override
    public MbOrderDetailRespVO getAdminOrderDetail(Long id) {
        MbOrderDO order = mbOrderMapper.selectById(id);
        if (order == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }
        return convertToAdminDetail(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUserMaterialRestockOrder(AppMbOrderCreateReqVO createReqVO) {
        // 获取当前登录用户ID
        Long userId = SecurityFrameworkUtils.getLoginUserId();

        // 设置订单中的用户ID
        createReqVO.setAgentUserId(userId);

        // 加载物料定义，决定业务能力
        MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(createReqVO.getProductId());
        if (definition == null) {
            throw exception(MATERIAL_DEFINITION_NOT_EXISTS);
        }

        boolean supportConvert = Boolean.TRUE.equals(definition.getSupportConvert());

        String bizType = createReqVO.getBizType();
        if (!StringUtils.hasText(bizType)) {
            bizType = MbOrderBizType.RESTOCK;
        }

        boolean isConvert = MbOrderBizType.MATERIAL_CONVERT.equals(bizType)
            || MbOrderBizType.COLLAGEN_CONVERT.equals(bizType);
        bizType = isConvert ? MbOrderBizType.MATERIAL_CONVERT : MbOrderBizType.RESTOCK;

        createReqVO.setBizType(bizType);

        AppMbPriceRespVO priceInfo = null;
        String productName;
        if (isConvert) {
            if (!supportConvert) {
                throw exception(MATERIAL_NOT_SUPPORT_CONVERT);
            }
            if (definition.getConvertedSpuId() == null) {
                throw exception(MATERIAL_CONVERT_TARGET_NOT_CONFIGURED);
            }
            if (definition.getConvertPrice() == null || definition.getConvertPrice() < 0) {
                throw exception(MATERIAL_CONVERT_PRICE_NOT_CONFIGURED);
            }
            // 校验库存是否足够
            MaterialBalanceRespDTO balance = materialApi.getBalance(userId, definition.getId());
            if (balance == null || balance.getAvailableBalance() < createReqVO.getQuantity()) {
                throw exception(USER_MATERIAL_BALANCE_NOT_ENOUGH);
            }
            createReqVO.setUnitPrice(definition.getConvertPrice());
            createReqVO.setTotalPrice(definition.getConvertPrice() * createReqVO.getQuantity());

            // 尝试获取目标物料名称，优先使用转化目标
            MaterialDefinitionRespDTO targetDefinition = materialApi.getDefinitionBySpuId(definition.getConvertedSpuId());
            if (targetDefinition != null && StringUtils.hasText(targetDefinition.getName())) {
                productName = targetDefinition.getName();
            } else if (StringUtils.hasText(definition.getName())) {
                productName = definition.getName();
            } else {
                productName = "物料转化";
            }
        } else {
            priceInfo = materialRestockPriceService.getMaterialRestockPriceByUserIdAndProductId(userId, createReqVO.getProductId());
            if (priceInfo == null || priceInfo.getRestockPrice() == null) {
                throw exception(MATERIAL_RESTOCK_PRICE_NOT_EXISTS);
            }
            int unitPrice = priceInfo.getRestockPrice();
            int totalPrice = unitPrice * createReqVO.getQuantity();
            createReqVO.setUnitPrice(unitPrice);
            createReqVO.setTotalPrice(totalPrice);
            // 下单阶段提前校验出资方的库存余额，避免支付成功后才发现库存不足。
            validateFundingStock(userId, definition.getId(), createReqVO.getQuantity());
            productName = priceInfo.getProductName();
        }

        // 创建订单对象
        MbOrderDO orderDO = new MbOrderDO();
        orderDO.setId(IdGeneratorUtil.nextId());
        orderDO.setProductId(createReqVO.getProductId());
        orderDO.setBizType(bizType);
        orderDO.setAgentUserId(createReqVO.getAgentUserId());
        orderDO.setQuantity(createReqVO.getQuantity());
        orderDO.setUnitPrice(createReqVO.getUnitPrice());
        orderDO.setTotalPrice(createReqVO.getTotalPrice());

        // 插入数据库
        mbOrderMapper.insert(orderDO);

        // 确定订单类型前缀 - 根据业务类型判断
        String orderTypePrefix = "MR"; // 默认为补货订单前缀
        String orderSubject = "补货: ";
        String orderBody = "物料补货订单: ";

        if (isConvert) {
            orderTypePrefix = "MC"; // 物料转化订单前缀
            orderSubject = "物料转化: ";
            orderBody = "物料转化订单: ";
        }

        // 创建支付订单
        Long payOrderId = createPayOrder(orderDO, productName, orderTypePrefix, orderSubject, orderBody);

        // 更新订单支付ID
        if (payOrderId != null) {
            orderDO.setPaymentId(String.valueOf(payOrderId));
            mbOrderMapper.updateById(orderDO);
        }

        // 返回支付订单ID（而不是订单ID）
        return payOrderId;
    }

    /**
     * 创建支付订单
     *
     * @param orderDO 订单
     * @param productName 产品名称
     * @param orderTypePrefix 订单类型前缀
     * @param orderSubject 订单主题
     * @param orderBody 订单描述
     * @return 支付订单ID
     */
    private Long createPayOrder(MbOrderDO orderDO, String productName,
                               String orderTypePrefix, String orderSubject, String orderBody) {
        // 获取MB专用的支付应用
        PayAppDO payApp;
        try {
            payApp = payAppService.validPayApp("mb");
        } catch (Exception e) {
            log.error("[createPayOrder][MB支付应用不存在，请先在支付管理中创建appKey为'mb'的应用]", e);
            throw new RuntimeException("MB支付应用未配置，请联系管理员配置MB支付应用");
        }

        // 1. 构建支付订单请求
        PayOrderCreateReqDTO createReqDTO = new PayOrderCreateReqDTO();
        // 1.1 设置应用信息 - 使用MB专用应用
        createReqDTO.setAppKey("mb");

        // 1.2 设置商户订单信息
        createReqDTO.setMerchantOrderId(orderTypePrefix + orderDO.getId()); // 使用订单ID作为商户订单ID
        createReqDTO.setSubject(orderSubject + productName); // 订单标题
        createReqDTO.setBody(orderBody + orderDO.getId()); // 订单描述

        // 1.3 设置订单金额
        createReqDTO.setPrice(orderDO.getTotalPrice());

        // 1.4 设置订单过期时间 - 默认30分钟后过期
        createReqDTO.setExpireTime(LocalDateTime.now().plus(30, ChronoUnit.MINUTES));

        // 1.5 设置用户IP
        createReqDTO.setUserIp(ServletUtils.getClientIP());

        // 2. 调用支付模块API创建支付订单
        return payOrderApi.createOrder(createReqDTO);
    }

    private void validateFundingStock(Long purchaserUserId, Long materialDefinitionId, Integer quantity) {
        if (materialDefinitionId == null || quantity == null || quantity <= 0) {
            return;
        }
        Long fundingUserId = resolveFundingUserId(purchaserUserId);
        if (Objects.equals(fundingUserId, CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID)) {
            // 平台兜底：允许透支，直接放行。
            log.info("[validateFundingStock][默认由平台出资，无需校验库存 userId={}]", purchaserUserId);
            return;
        }
        MaterialBalanceRespDTO balance = materialApi.getBalance(fundingUserId, materialDefinitionId);
        int available = balance != null && balance.getAvailableBalance() != null ? balance.getAvailableBalance() : 0;
        // 库存不足时仅记录信息，由总部虚拟账号兜底出资，避免下单链路被打断
        if (available < quantity) {
            log.info("[validateFundingStock][出资方库存不足，启用总部账号兜底 userId={} funding={} materialDef={} need={} available={}]",
                    purchaserUserId, fundingUserId, materialDefinitionId, quantity, available);
        }
    }

    private Long resolveFundingUserId(Long purchaserUserId) {
        BrokerageUserRespDTO bossUser = brokerageUserApi.getFirstBossBrokerageUser(purchaserUserId);
        if (bossUser != null && Boolean.TRUE.equals(bossUser.getBrokerageEnabled())
                && !Objects.equals(bossUser.getId(), purchaserUserId)) {
            return bossUser.getId();
        }
        // 未找到有效出资人时，统一由总部虚拟账号承担。
        return CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID;
    }



    @Override
    public PageResult<AppMbOrderRespVO> getUserMaterialOrderPage(AppMbOrderPageReqVO pageReqVO) {
        // 确保查询条件中包含当前用户ID，保证数据安全性
        Long currentUserId = SecurityFrameworkUtils.getLoginUserId();
        pageReqVO.setAgentUserId(currentUserId);

        // 查询订单分页数据
        PageResult<MbOrderDO> orderPage = mbOrderMapper.selectPage(pageReqVO);

        // 转换为响应VO
        return new PageResult<>(
            convertToAppMbOrderRespVOList(orderPage.getList()),
            orderPage.getTotal()
        );
    }

    @Override
    public AppMbOrderRespVO getUserMaterialOrderDetail(Long orderId, Long userId) {
        // 查询订单，确保只能查看自己的订单
        MbOrderDO orderDO = mbOrderMapper.selectOne(new LambdaQueryWrapperX<MbOrderDO>()
                .eq(MbOrderDO::getId, orderId)
                .eq(MbOrderDO::getAgentUserId, userId));

        if (orderDO == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }

        // 转换为响应VO
        AppMbOrderRespVO respVO = convertToAppMbOrderRespVO(orderDO);
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(orderDO.getStatus()));
        if (MbOrderStatusEnum.DELIVERED.equals(statusEnum)) {
            respVO.setWechatExtraData(buildWechatExtraData(orderDO));
        }
        return respVO;
    }

    @Override
    public List<Map<String, Object>> getOrderStatusList() {
        List<Map<String, Object>> statusList = new ArrayList<>();

        // 添加"全部"选项
        Map<String, Object> allStatus = new HashMap<>();
        allStatus.put("code", "");
        allStatus.put("name", "全部");
        allStatus.put("count", 0); // 可以后续统计
        statusList.add(allStatus);

        // 添加各个状态选项
        for (MbOrderStatusEnum status : MbOrderStatusEnum.values()) {
            Map<String, Object> statusMap = new HashMap<>();
            statusMap.put("code", status.getCode());
            statusMap.put("name", status.getDescription());
            statusMap.put("count", 0); // 可以后续统计每个状态的订单数量
            statusList.add(statusMap);
        }

        return statusList;
    }

    @Override
    public void adminUpdateOrderStatus(Long orderId, String status) {
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }
        if (!StringUtils.hasText(status)) {
            throw exception(MB_ORDER_STATUS_INVALID);
        }
        String normalizedTarget = normalizeOrderStatus(status);
        MbOrderStatusEnum targetStatus = MbOrderStatusEnum.fromCode(normalizedTarget);

        String currentStatusCode = normalizeOrderStatus(order.getStatus());
        MbOrderStatusEnum currentStatus = MbOrderStatusEnum.fromCode(currentStatusCode);
        if (currentStatus == targetStatus) {
            return;
        }
        if (currentStatus.isFinalStatus()) {
            throw exception(MB_ORDER_STATUS_INVALID);
        }

        order.setStatus(targetStatus.getCode());
        mbOrderMapper.updateById(order);
    }

    @Override
    public void retryVirtualDelivery(Long orderId) {
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }
        if (!StringUtils.hasText(order.getPaymentId())) {
            throw exception(MB_ORDER_PAYMENT_NOT_EXISTS);
        }

        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(order.getStatus()));
        if (!(MbOrderStatusEnum.DELIVERED.equals(statusEnum) || MbOrderStatusEnum.COMPLETED.equals(statusEnum))) {
            throw exception(MB_ORDER_STATUS_INVALID);
        }

        Long payOrderId;
        try {
            payOrderId = Long.valueOf(order.getPaymentId());
        } catch (NumberFormatException ex) {
            log.error("[retryVirtualDelivery][orderId={}] 支付单编号非法: {}", orderId, order.getPaymentId(), ex);
            throw exception(MB_ORDER_PAYMENT_NOT_EXISTS);
        }

        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            throw exception(MB_ORDER_PAYMENT_NOT_EXISTS);
        }
        String channelCode = payOrder.getChannelCode();
        if (!StringUtils.hasText(channelCode) || !channelCode.toLowerCase().contains("wx")) {
            throw exception(MB_ORDER_PAYMENT_CHANNEL_UNSUPPORTED);
        }

        mbVirtualDeliveryService.scheduleVirtualDelivery(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminRefundOrder(Long orderId, String reason, String password) {
        validateRefundPassword(password);
        // 退款处理步骤：先校验订单&支付单，再构造退款申请，把订单状态切换为退款中以避免重复触发。
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(order.getStatus()));
        if (!isAdminRefundEnabled(statusEnum)) {
            throw exception(MB_ORDER_STATUS_INVALID);
        }
        if (!StringUtils.hasText(order.getPaymentId())) {
            throw exception(MB_ORDER_PAYMENT_NOT_EXISTS);
        }
        Long payOrderId = parsePayOrderId(order);
        if (payOrderId == null) {
            throw exception(MB_ORDER_PAYMENT_NOT_EXISTS);
        }
        PayOrderRespDTO payOrder = payOrderApi.getOrder(payOrderId);
        if (payOrder == null) {
            throw exception(MB_ORDER_PAYMENT_NOT_EXISTS);
        }
        if (!PayOrderStatusEnum.isSuccessOrRefund(payOrder.getStatus())) {
            throw exception(MB_ORDER_STATUS_INVALID);
        }
        int refundPrice = Optional.ofNullable(order.getTotalPrice()).orElse(0);
        if (payOrder.getPrice() != null && refundPrice > payOrder.getPrice()) {
            refundPrice = payOrder.getPrice();
        }
        if (refundPrice <= 0) {
            throw exception(MB_ORDER_STATUS_INVALID);
        }

        String refundReason = StringUtils.hasText(reason) ? reason.trim() : "后台手动退款";
        PayAppDO payApp = payAppService.validPayApp(MB_APP_KEY);
        PayRefundCreateReqDTO refundReq = new PayRefundCreateReqDTO();
        refundReq.setAppKey(payApp.getAppKey());
        refundReq.setUserIp(ServletUtils.getClientIP());
        refundReq.setMerchantOrderId(buildMerchantOrderId(order));
        refundReq.setMerchantRefundId(buildMerchantRefundId(order));
        refundReq.setPrice(refundPrice);
        refundReq.setReason(refundReason);
        payRefundApi.createRefund(refundReq);

        order.setStatus(MbOrderStatusEnum.REFUNDING.getCode());
        mbOrderMapper.updateById(order);
        log.info("[adminRefundOrder][orderId={}] 已发起退款，merchantOrderId={}, merchantRefundId={}",
            orderId, refundReq.getMerchantOrderId(), refundReq.getMerchantRefundId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveOrderByMember(Long userId, Long orderId) {
        // 先校验订单状态，避免无效操作
        MbOrderDO order = validateOrderReceivable(userId, orderId);
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(order.getStatus()));
        if (MbOrderStatusEnum.COMPLETED.equals(statusEnum)) {
            return;
        }
        receiveOrder0(order, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkOrderReceiveStatus(Long orderId) {
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(order.getStatus()));
        if (MbOrderStatusEnum.COMPLETED.equals(statusEnum)) {
            return true;
        }

        Long payOrderId = parsePayOrderId(order);
        if (payOrderId == null) {
            log.debug("[checkOrderReceiveStatus][orderId={}] 缺少支付单ID，无法查询渠道收货状态", orderId);
            return false;
        }

        ChannelShippingQueryRequestDTO request = new ChannelShippingQueryRequestDTO();
        request.setOrderId(order.getId());
        request.setOrderNo(generateOrderNo(order));
        request.setPayOrderId(payOrderId);
        request.setDelivered(MbOrderStatusEnum.DELIVERED.equals(statusEnum) || MbOrderStatusEnum.COMPLETED.equals(statusEnum));

        ChannelShippingQueryRespDTO resp;
        try {
            resp = channelShippingApi.query(request);
        } catch (Exception ex) {
            log.warn("[checkOrderReceiveStatus][orderId={}] 渠道查询失败", orderId, ex);
            return false;
        }
        if (resp == null || !resp.isShipped()) {
            log.debug("[checkOrderReceiveStatus][orderId={}] 渠道未返回发货信息", orderId);
            return false;
        }

        ChannelShippingConfirmStatus confirmStatus = Optional.ofNullable(resp.getConfirmStatus())
            .orElse(ChannelShippingConfirmStatus.UNKNOWN);
        if (!ChannelShippingConfirmStatus.CONFIRMED.equals(confirmStatus)) {
            if (!ChannelShippingConfirmStatus.CANCELED.equals(confirmStatus)) {
                log.debug("[checkOrderReceiveStatus][orderId={}] 渠道状态为{}，暂不确认", orderId, confirmStatus);
            }
            return false;
        }

        if (!ensureDeliveredStatus(order)) {
            order = mbOrderMapper.selectById(orderId);
            if (order == null) {
                return false;
            }
        }

        try {
            receiveOrder0(order, LocalDateTime.now());
            return true;
        } catch (ServiceException ex) {
            if (Objects.equals(ex.getCode(), MB_ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED.getCode())) {
                MbOrderDO latest = mbOrderMapper.selectById(orderId);
                if (latest == null) {
                    return false;
                }
                MbOrderStatusEnum latestStatus = MbOrderStatusEnum.fromCode(normalizeOrderStatus(latest.getStatus()));
                return MbOrderStatusEnum.COMPLETED.equals(latestStatus);
            }
            log.warn("[checkOrderReceiveStatus][orderId={}] 渠道确认失败", orderId, ex);
            return false;
        }
    }

    @Override
    public void processOrderPaidNotify(Long orderId, Long payOrderId, String deliveryStatus) {
        if (orderId == null) {
            return;
        }
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            log.warn("[processOrderPaidNotify][orderId={}] 订单不存在，忽略渠道妥投回调", orderId);
            return;
        }
        String normalizedStatus = normalizeDeliveryStatus(deliveryStatus);
        if (!isDeliveredDeliveryStatus(normalizedStatus)) {
            if (isCanceledDeliveryStatus(normalizedStatus)) {
                log.info("[processOrderPaidNotify][orderId={}] 渠道返回取消确认状态，保持待收货", orderId);
            } else {
                log.debug("[processOrderPaidNotify][orderId={}] 渠道妥投状态为{}，暂不处理", orderId, normalizedStatus);
            }
            return;
        }

        if (!ensureDeliveredStatus(order)) {
            log.info("[processOrderPaidNotify][orderId={}] 当前状态({}) 非待确认收货，忽略渠道确认", orderId, order.getStatus());
            return;
        }

        LocalDateTime receiveTime = LocalDateTime.now();
        try {
            receiveOrder0(order, receiveTime);
            log.info("[processOrderPaidNotify][orderId={}] 渠道妥投渠道确认收货成功", orderId);
        } catch (ServiceException ex) {
            if (Objects.equals(ex.getCode(), MB_ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED.getCode())) {
                log.debug("[processOrderPaidNotify][orderId={}] 状态已变更为非待收货，忽略渠道确认", orderId);
                return;
            }
            throw ex;
        }
    }

    private MbOrderDO validateOrderReceivable(Long userId, Long orderId) {
        MbOrderDO order = mbOrderMapper.selectOne(new LambdaQueryWrapperX<MbOrderDO>()
                .eq(MbOrderDO::getId, orderId)
                .eq(MbOrderDO::getAgentUserId, userId));
        if (order == null) {
            throw exception(USER_MATERIAL_ORDER_NOT_EXISTS);
        }
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(order.getStatus()));
        if (MbOrderStatusEnum.COMPLETED.equals(statusEnum)) {
            return order;
        }
        if (!MbOrderStatusEnum.DELIVERED.equals(statusEnum)) {
            throw exception(MB_ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        return order;
    }

    private boolean ensureDeliveredStatus(MbOrderDO order) {
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizeOrderStatus(order.getStatus()));
        if (MbOrderStatusEnum.DELIVERED.equals(statusEnum) || MbOrderStatusEnum.COMPLETED.equals(statusEnum)) {
            return true;
        }
        if (!MbOrderStatusEnum.PROCESSING.equals(statusEnum)) {
            return false;
        }
        LocalDateTime deliveryTime = order.getDeliveryTime() != null ? order.getDeliveryTime() : LocalDateTime.now();
        MbOrderDO updateObj = new MbOrderDO()
                .setStatus(MbOrderStatusEnum.DELIVERED.getCode())
                .setDeliveryTime(deliveryTime);
        int updated = mbOrderMapper.updateByIdAndStatus(order.getId(), statusEnum.getCode(), updateObj);
        if (updated == 0) {
            return false;
        }
        order.setStatus(updateObj.getStatus());
        order.setDeliveryTime(deliveryTime);
        return true;
    }

    private void receiveOrder0(MbOrderDO order, LocalDateTime receiveTime) {
        LocalDateTime finalReceiveTime = receiveTime != null ? receiveTime : LocalDateTime.now();
        finalizeFulfillmentBeforeComplete(order);
        String currentStatus = normalizeOrderStatus(order.getStatus());
        MbOrderDO updateObj = new MbOrderDO()
                .setStatus(MbOrderStatusEnum.COMPLETED.getCode())
                .setReceiveTime(finalReceiveTime);
        int update = mbOrderMapper.updateByIdAndStatus(order.getId(), currentStatus, updateObj);
        if (update == 0) {
            throw exception(MB_ORDER_RECEIVE_FAIL_STATUS_NOT_DELIVERED);
        }
        order.setStatus(updateObj.getStatus());
        order.setReceiveTime(finalReceiveTime);

        scheduleBrokerageSettlement(order, finalReceiveTime);
    }

    /**
     * 收货前补齐库存发放或物料转化
     *
     * @param order 当前订单
     */
    private void finalizeFulfillmentBeforeComplete(MbOrderDO order) {
        if (order == null) {
            return;
        }
        String bizType = StrUtil.blankToDefault(order.getBizType(), "");
        try {
            if (StrUtil.equalsIgnoreCase(bizType, MbOrderBizType.RESTOCK)) {
                materialRestockOrderHandler.handleAfterReceive(order);
            } else if (StrUtil.equalsIgnoreCase(bizType, MbOrderBizType.MATERIAL_CONVERT)
                    || StrUtil.equalsIgnoreCase(bizType, MbOrderBizType.COLLAGEN_CONVERT)) {
                materialConvertOrderHandler.handleAfterReceive(order);
            }
        } catch (Exception ex) {
            log.error("[finalizeFulfillmentBeforeComplete][orderId={}] 收货后库存发放失败", order.getId(), ex);
            throw ex;
        }
    }

    /**
     * 根据统一配置补齐佣金解冻时间
     */
    private void scheduleBrokerageSettlement(MbOrderDO order, LocalDateTime receiveTime) {
        TradeConfigRespDTO tradeConfig = tradeConfigApi.getTradeConfig();
        int frozenDays = Optional.ofNullable(tradeConfig)
            .map(TradeConfigRespDTO::getBrokerageFrozenDays)
            .orElse(0);
        String bizId = String.valueOf(order.getId());
        LocalDateTime unfreezeTime = receiveTime != null ? receiveTime.plusDays(Math.max(frozenDays, 0)) : null;
        brokerageRecordApi.scheduleWaitSettlementByBizIds(Collections.singleton(bizId), frozenDays, unfreezeTime);
    }

    private Long parsePayOrderId(MbOrderDO order) {
        if (order == null || !StringUtils.hasText(order.getPaymentId())) {
            return null;
        }
        try {
            return Long.valueOf(order.getPaymentId());
        } catch (NumberFormatException ex) {
            log.warn("[parsePayOrderId][orderId={}] 支付单编号非法: {}", order != null ? order.getId() : null, order != null ? order.getPaymentId() : null, ex);
            return null;
        }
    }

    private boolean isAdminRefundEnabled(MbOrderStatusEnum statusEnum) {
        if (statusEnum == null) {
            return false;
        }
        if (statusEnum.isRefundRelated()) {
            return statusEnum == MbOrderStatusEnum.REFUND_FAILED;
        }
        switch (statusEnum) {
            case PROCESSING:
            case DELIVERED:
            case COMPLETED:
            case FAILED:
                return true;
            default:
                return false;
        }
    }

    private String buildMerchantOrderId(MbOrderDO order) {
        return resolveOrderPrefix(order) + order.getId();
    }

    private String buildMerchantRefundId(MbOrderDO order) {
        return IdGeneratorUtil.nextIdStr(resolveOrderPrefix(order) + "R");
    }

    private void validateRefundPassword(String password) {
        if (!StringUtils.hasText(adminRefundPassword)) {
            return;
        }
        if (!StringUtils.hasText(password) || !adminRefundPassword.equals(password)) {
            throw exception(MB_ORDER_REFUND_PASSWORD_INVALID);
        }
    }

    @Override
    public boolean isAdminRefundPasswordEnabled() {
        return StringUtils.hasText(adminRefundPassword);
    }

    private String resolveOrderPrefix(MbOrderDO order) {
        if (order == null || !StringUtils.hasText(order.getBizType())) {
            return "MB";
        }
        String bizType = order.getBizType().toLowerCase(Locale.ROOT);
        if (MbOrderBizType.MATERIAL_CONVERT.equalsIgnoreCase(bizType)
            || MbOrderBizType.COLLAGEN_CONVERT.equalsIgnoreCase(bizType)) {
            return "MC";
        }
        return "MR";
    }

    private String normalizeDeliveryStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return "";
        }
        return status.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isDeliveredDeliveryStatus(String status) {
        return "DELIVERED".equals(status) || "NOREQUIRED".equals(status);
    }

    private boolean isCanceledDeliveryStatus(String status) {
        return "CANCELED".equals(status);
    }

    private AppMbOrderWeixinExtraRespVO buildWechatExtraData(MbOrderDO order) {
        if (order == null) {
            return null;
        }
        Long payOrderId = parsePayOrderId(order);
        if (payOrderId == null) {
            return null;
        }
        ChannelShippingQueryRequestDTO request = new ChannelShippingQueryRequestDTO();
        request.setOrderId(order.getId());
        request.setOrderNo(generateOrderNo(order));
        request.setPayOrderId(payOrderId);
        request.setDelivered(Boolean.TRUE);
        try {
            ChannelShippingQueryRespDTO resp = channelShippingApi.query(request);
            if (resp == null) {
                return null;
            }
            WeixinOrderConfirmExtraRespDTO extra = resp.getExtra(ChannelShippingQueryRespDTO.EXTRA_WEIXIN_CONFIRM,
                    WeixinOrderConfirmExtraRespDTO.class);
            if (extra == null) {
                return null;
            }
            AppMbOrderWeixinExtraRespVO respVO = new AppMbOrderWeixinExtraRespVO();
            respVO.setMerchantId(extra.getMerchantId());
            respVO.setSubMerchantId(extra.getSubMerchantId());
            respVO.setTransactionId(extra.getTransactionId());
            respVO.setMerchantTradeNo(extra.getMerchantTradeNo());
            return respVO;
        } catch (Exception ex) {
            log.warn("[buildWechatExtraData][orderId={}] 查询渠道收货额外数据失败", order.getId(), ex);
            return null;
        }
    }

    private MbOrderRespVO convertToAdminSummary(MbOrderDO order, Map<Long, MemberUserRespDTO> userMap) {
        MbOrderRespVO respVO = new MbOrderRespVO();
        respVO.setId(order.getId());
        respVO.setOrderNo(generateOrderNo(order));
        respVO.setProductId(order.getProductId());
        respVO.setBizType(order.getBizType());
        respVO.setBizTypeName(getBizTypeDisplay(order.getBizType()));
        respVO.setAgentUserId(order.getAgentUserId());
        if (order.getAgentUserId() != null && userMap != null) {
            MemberUserRespDTO user = userMap.get(order.getAgentUserId());
            if (user != null) {
                respVO.setAgentUserNickname(user.getNickname());
                respVO.setAgentUserMobile(user.getMobile());
            }
        }
        respVO.setQuantity(order.getQuantity());
        respVO.setUnitPrice(order.getUnitPrice());
        respVO.setUnitPriceDisplay(formatPrice(order.getUnitPrice()));
        respVO.setTotalPrice(order.getTotalPrice());
        respVO.setTotalPriceDisplay(formatPrice(order.getTotalPrice()));
        respVO.setPaymentId(order.getPaymentId());
        String statusCode = normalizeOrderStatus(order.getStatus());
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(statusCode);
        respVO.setStatus(statusCode);
        respVO.setStatusName(statusEnum.getDescription());
        respVO.setCreateTime(order.getCreateTime());
        respVO.setUpdateTime(order.getUpdateTime());
        respVO.setDeliveryTime(order.getDeliveryTime());
        respVO.setReceiveTime(order.getReceiveTime());
        respVO.setRefundable(isAdminRefundEnabled(statusEnum) && StringUtils.hasText(order.getPaymentId()));

        if (order.getProductId() != null) {
            try {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
                if (definition != null) {
                    respVO.setProductName(definition.getName());
                    respVO.setProductImage(definition.getImage());
                } else {
                    respVO.setProductName(getDefaultProductName(order.getProductId()));
                }
            } catch (Exception ex) {
                log.warn("[convertToAdminSummary][获取物料信息失败 orderId={} productId={}]", order.getId(), order.getProductId(), ex);
                respVO.setProductName(getDefaultProductName(order.getProductId()));
            }
        } else {
            respVO.setProductName("未知商品");
        }
        return respVO;
    }

    private MbOrderDetailRespVO convertToAdminDetail(MbOrderDO order) {
        MbOrderDetailRespVO detail = new MbOrderDetailRespVO();
        detail.setId(order.getId());
        detail.setOrderNo(generateOrderNo(order));
        detail.setProductId(order.getProductId());
        detail.setBizType(order.getBizType());
        detail.setBizTypeName(getBizTypeDisplay(order.getBizType()));
        detail.setAgentUserId(order.getAgentUserId());
        detail.setQuantity(order.getQuantity());
        detail.setUnitPrice(order.getUnitPrice());
        detail.setUnitPriceDisplay(formatPrice(order.getUnitPrice()));
        detail.setTotalPrice(order.getTotalPrice());
        detail.setTotalPriceDisplay(formatPrice(order.getTotalPrice()));
        detail.setPaymentId(order.getPaymentId());
        detail.setCreateTime(order.getCreateTime());
        detail.setUpdateTime(order.getUpdateTime());

        String statusCode = normalizeOrderStatus(order.getStatus());
        MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(statusCode);
        detail.setStatus(statusCode);
        detail.setStatusName(statusEnum.getDescription());
        detail.setDeliveryTime(order.getDeliveryTime());
        detail.setReceiveTime(order.getReceiveTime());
        detail.setCanReceive(MbOrderStatusEnum.DELIVERED.equals(statusEnum));
        detail.setRefundable(isAdminRefundEnabled(statusEnum) && StringUtils.hasText(order.getPaymentId()));

        if (order.getProductId() != null) {
            try {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
                if (definition != null) {
                    detail.setProductName(definition.getName());
                    detail.setProductImage(definition.getImage());
                } else {
                    detail.setProductName(getDefaultProductName(order.getProductId()));
                }
            } catch (Exception ex) {
                log.warn("[convertToAdminDetail][获取物料信息失败 orderId={} productId={}]", order.getId(), order.getProductId(), ex);
                detail.setProductName(getDefaultProductName(order.getProductId()));
            }
        } else {
            detail.setProductName("未知商品");
        }

        if (order.getAgentUserId() != null) {
            try {
                MemberUserRespDTO user = memberUserApi.getUser(order.getAgentUserId());
                if (user != null) {
                    detail.setAgentUserNickname(user.getNickname());
                    detail.setAgentUserMobile(user.getMobile());
                }
            } catch (Exception ex) {
                log.warn("[convertToAdminDetail][获取用户信息失败 orderId={} userId={}]", order.getId(), order.getAgentUserId(), ex);
            }
        }

        PayOrderRespDTO payOrder = null;
        if (StringUtils.hasText(order.getPaymentId())) {
            try {
                Long payOrderId = Long.valueOf(order.getPaymentId());
                payOrder = payOrderApi.getOrder(payOrderId);
            } catch (Exception ex) {
                log.warn("[convertToAdminDetail][获取支付信息失败 orderId={} paymentId={}]", order.getId(), order.getPaymentId(), ex);
            }
        }

        if (payOrder != null) {
            detail.setPayChannelCode(payOrder.getChannelCode());
            detail.setPayStatus(payOrder.getStatus());
            detail.setPayStatusName(getPayStatusName(payOrder.getStatus()));
            detail.setPayPrice(payOrder.getPrice());
            detail.setPayPriceDisplay(formatPrice(payOrder.getPrice()));
            detail.setPaySuccessTime(payOrder.getSuccessTime());
            boolean canRetry = StringUtils.hasText(payOrder.getChannelCode())
                && payOrder.getChannelCode().toLowerCase().contains("wx")
                && (MbOrderStatusEnum.DELIVERED.equals(statusEnum) || MbOrderStatusEnum.COMPLETED.equals(statusEnum));
            detail.setCanRetryVirtualDelivery(canRetry);
        } else {
            detail.setPayStatusName(getPayStatusName(null));
            detail.setPayPrice(order.getTotalPrice());
            detail.setPayPriceDisplay(formatPrice(order.getTotalPrice()));
            detail.setCanRetryVirtualDelivery(Boolean.FALSE);
        }

        return detail;
    }

    private String getPayStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        for (PayOrderStatusEnum value : PayOrderStatusEnum.values()) {
            if (Objects.equals(value.getStatus(), status)) {
                return value.getName();
            }
        }
        return "未知";
    }

    /**
     * 将订单DO列表转换为App响应VO列表
     *
     * @param orderList 订单DO列表
     * @return App响应VO列表
     */
    private List<AppMbOrderRespVO> convertToAppMbOrderRespVOList(List<MbOrderDO> orderList) {
        if (orderList == null || orderList.isEmpty()) {
            return new ArrayList<>();
        }

        return orderList.stream()
            .map(this::convertToAppMbOrderRespVO)
            .collect(Collectors.toList());
    }

    /**
     * 将订单DO转换为App响应VO
     *
     * @param orderDO 订单DO
     * @return App响应VO
     */
    private AppMbOrderRespVO convertToAppMbOrderRespVO(MbOrderDO orderDO) {
        AppMbOrderRespVO respVO = new AppMbOrderRespVO();

        // 手动复制基础字段，避免类型转换问题
        respVO.setId(orderDO.getId());
        respVO.setProductId(orderDO.getProductId());
        respVO.setBizType(orderDO.getBizType());
        respVO.setAgentUserId(orderDO.getAgentUserId());
        respVO.setQuantity(orderDO.getQuantity());
        respVO.setUnitPrice(orderDO.getUnitPrice());
        respVO.setTotalPrice(orderDO.getTotalPrice());
        respVO.setPaymentId(orderDO.getPaymentId());
        respVO.setCreateTime(orderDO.getCreateTime());
        respVO.setUpdateTime(orderDO.getUpdateTime());
        respVO.setDeliveryTime(orderDO.getDeliveryTime());
        respVO.setReceiveTime(orderDO.getReceiveTime());

        // 获取商品信息（名称和图片）
        try {
            AppMbPriceRespVO priceInfo = materialRestockPriceService
                .getMaterialRestockPriceByUserIdAndProductId(orderDO.getAgentUserId(), orderDO.getProductId());
            if (priceInfo != null) {
                respVO.setProductName(priceInfo.getProductName());
                respVO.setProductImage(priceInfo.getProductImage());
            }
        } catch (Exception e) {
            log.warn("[convertToAppMbOrderRespVO][获取商品信息失败: orderId={}, productId={}]",
                orderDO.getId(), orderDO.getProductId(), e);
            // 设置默认商品名称
            respVO.setProductName(getDefaultProductName(orderDO.getProductId()));
        }

        // 处理订单状态转换
        MbOrderStatusEnum statusEnum = null;
        if (orderDO.getStatus() != null) {
            try {
                // 先尝试标准化状态值
                String normalizedStatus = normalizeOrderStatus(orderDO.getStatus());
                statusEnum = MbOrderStatusEnum.fromCode(normalizedStatus);
                respVO.setStatus(statusEnum.getAppCode());
                respVO.setStatusName(statusEnum.getDescription());
                respVO.setCanReceive(Boolean.FALSE);
                if (MbOrderStatusEnum.DELIVERED.equals(statusEnum)) {
                    respVO.setCanReceive(Boolean.TRUE);
                }
            } catch (Exception e) {
                log.warn("[convertToAppMbOrderRespVO][订单状态转换失败: orderId={}, status={}]",
                    orderDO.getId(), orderDO.getStatus(), e);
                statusEnum = MbOrderStatusEnum.PENDING;
                respVO.setStatus(1); // 默认状态
                respVO.setStatusName("未知状态");
                respVO.setCanReceive(Boolean.FALSE);
            }
        } else {
            // 默认状态为待处理
            statusEnum = MbOrderStatusEnum.PENDING;
            respVO.setStatus(1);
            respVO.setStatusName(MbOrderStatusEnum.PENDING.getDescription());
            respVO.setCanReceive(Boolean.FALSE);
        }

        // 设置额外的显示字段
        respVO.setOrderNo(generateOrderNo(orderDO));
        respVO.setTotalPriceDisplay(formatPrice(orderDO.getTotalPrice()));
        respVO.setUnitPriceDisplay(formatPrice(orderDO.getUnitPrice()));
        respVO.setCanRefund(statusEnum != null && statusEnum.canRefund());
        respVO.setCreateTimeDisplay(formatDateTime(orderDO.getCreateTime()));
        respVO.setStatusColor(getStatusColor(statusEnum));
        respVO.setStatusIcon(getStatusIcon(statusEnum));
        respVO.setBizTypeDisplay(getBizTypeDisplay(orderDO.getBizType()));

        return respVO;
    }

    /**
     * 标准化订单状态值
     * 处理数据库中可能存在的不同状态格式
     *
     * @param status 原始状态值
     * @return 标准化后的状态值
     */
    private String normalizeOrderStatus(String status) {
        if (status == null) {
            return MbOrderStatusEnum.PENDING.getCode();
        }

        // 转换为大写并去除空格
        String normalizedStatus = status.trim().toUpperCase();

        // 处理可能的状态值映射
        switch (normalizedStatus) {
            case "COMPLETE":
                return "COMPLETED";
            case "PROCESS":
            case "PROCESSING":
                return "PROCESSING";
            case "DELIVER":
            case "DELIVERED":
            case "DELIVERY":
                return "DELIVERED";
            case "PENDING":
                return "PENDING";
            case "FAILED":
            case "FAIL":
                return "FAILED";
            case "REFUNDING":
                return "REFUNDING";
            case "REFUNDED":
                return "REFUNDED";
            case "REFUND_FAILED":
                return "REFUND_FAILED";
            default:
                // 如果是已知的标准状态，直接返回
                try {
                    MbOrderStatusEnum.fromCode(normalizedStatus);
                    return normalizedStatus;
                } catch (Exception e) {
                    log.warn("[normalizeOrderStatus][未知的订单状态: {}，使用默认状态]", status);
                    return MbOrderStatusEnum.PENDING.getCode();
                }
        }
    }

    /**
     * 生成订单号显示
     *
     * @param orderDO 订单DO
     * @return 订单号
     */
    private String generateOrderNo(MbOrderDO orderDO) {
        if (orderDO == null || orderDO.getId() == null) {
            return "";
        }
        String bizType = StrUtil.blankToDefault(orderDO.getBizType(), "");
        String prefix = "MR";
        if (StrUtil.equalsIgnoreCase(bizType, MbOrderBizType.MATERIAL_CONVERT)
                || StrUtil.equalsIgnoreCase(bizType, MbOrderBizType.COLLAGEN_CONVERT)) {
            prefix = "MC";
        } else if (StrUtil.isNotBlank(bizType) && !StrUtil.equalsIgnoreCase(bizType, MbOrderBizType.RESTOCK)) {
            prefix = "MB";
        }
        return prefix + orderDO.getId();
    }

    /**
     * 格式化价格显示（分转元）
     *
     * @param priceInCents 价格（分）
     * @return 格式化后的价格字符串
     */
    private String formatPrice(Integer priceInCents) {
        if (priceInCents == null) {
            return "0.00";
        }
        return String.format("%.2f", priceInCents / 100.0);
    }

    /**
     * 格式化时间显示
     *
     * @param dateTime 时间
     * @return 格式化后的时间字符串
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }

    /**
     * 获取状态颜色
     *
     * @param statusEnum 状态枚举
     * @return 颜色值
     */
    private String getStatusColor(MbOrderStatusEnum statusEnum) {
        if (statusEnum == null) {
            return "#999999";
        }

        switch (statusEnum) {
            case PENDING:
                return "#ff9500";      // 橙色 - 待处理
            case PROCESSING:
                return "#007aff";      // 蓝色 - 处理中
            case DELIVERED:
                return "#34c759";      // 绿色 - 待确认收货
            case COMPLETED:
                return "#34c759";      // 绿色 - 已完成
            case FAILED:
                return "#ff3b30";      // 红色 - 处理失败
            case REFUNDING:
                return "#ff9500";      // 橙色 - 退款中
            case REFUNDED:
                return "#8e8e93";      // 灰色 - 已退款
            case REFUND_FAILED:
                return "#ff3b30";      // 红色 - 退款失败
            default:
                return "#999999";      // 默认灰色
        }
    }

    /**
     * 获取状态图标
     *
     * @param statusEnum 状态枚举
     * @return 图标名称
     */
    private String getStatusIcon(MbOrderStatusEnum statusEnum) {
        if (statusEnum == null) {
            return "clock";
        }

        switch (statusEnum) {
            case PENDING:
                return "clock";        // 时钟 - 待处理
            case PROCESSING:
                return "loading";      // 加载 - 处理中
            case DELIVERED:
                return "clock";        // 时钟 - 待确认收货
            case COMPLETED:
                return "success";      // 成功 - 已完成
            case FAILED:
                return "error";        // 错误 - 处理失败
            case REFUNDING:
                return "refund";       // 退款 - 退款中
            case REFUNDED:
                return "refunded";     // 已退款 - 已退款
            case REFUND_FAILED:
                return "error";        // 错误 - 退款失败
            default:
                return "clock";        // 默认时钟
        }
    }

    /**
     * 获取业务类型显示
     *
     * @param bizType 业务类型
     * @return 业务类型显示名称
     */
    private String getBizTypeDisplay(String bizType) {
        if (bizType == null) {
            return "未知类型";
        }

        switch (bizType) {
            case MbOrderBizType.RESTOCK:
                return "物料补货";
            case MbOrderBizType.MATERIAL_CONVERT:
            case MbOrderBizType.COLLAGEN_CONVERT:
                return "物料转化";
            case "careerProduct":
                return "事业产品";
            case "normalProduct":
                return "普通产品";
            default:
                return bizType;
        }
    }

    /**
     * 获取默认商品名称
     *
     * @param productId 商品ID
     * @return 商品名称
     */
    private String getDefaultProductName(Long productId) {
        if (productId == null) {
            return "未知商品";
        }

        MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(productId);
        if (definition != null && StringUtils.hasText(definition.getName())) {
            return definition.getName();
        }
        return "商品ID: " + productId;
    }

    private Long parseOrderIdFromOrderNo(String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }
        String value = orderNo.trim().toUpperCase(Locale.ROOT);
        StringBuilder digits = new StringBuilder();
        for (int i = value.length() - 1; i >= 0; i--) {
            char ch = value.charAt(i);
            if (Character.isDigit(ch)) {
                digits.append(ch);
            } else if (digits.length() > 0) {
                break;
            }
        }
        if (digits.length() == 0) {
            return null;
        }
        digits.reverse();
        try {
            return Long.valueOf(digits.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
