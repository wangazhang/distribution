package com.hissp.distribution.module.trade.service.order.handler.ext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.api.commission.CommissionStrategyApi;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionBizTypeConstants;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateReqDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionCalculateRespDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionHierarchyDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionMaterialGrantDTO;
import com.hissp.distribution.module.mb.api.commission.dto.CommissionOrderDTO;
import com.hissp.distribution.module.mb.api.commission.util.CommissionMaterialGrantHelper;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.service.order.handler.event.PaymentSuccessHandler;
import com.hissp.distribution.module.trade.service.order.handler.event.RefundSuccessHandler;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 商城中物料商品事件处理器
 *
 * @author 芋道源码
 */
@Slf4j
@Component
public class MbProductEventHandler implements PaymentSuccessHandler, RefundSuccessHandler {

    private static final String MATERIAL_STOCK_OUT_BIZ_TYPE = "MB_PRODUCT_STOCK_OUT";
    private static final String MATERIAL_STOCK_OUT_FUND_BIZ_TYPE = "MB_PRODUCT_STOCK_FUND";
    private static final String DIRECT_COMMISSION_TITLE = "物料直供佣金";
    private static final long PLATFORM_VIRTUAL_USER_ID = 0L;

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private BrokerageUserApi brokerageUserApi;
    @Resource
    private BrokerageRecordApi brokerageRecordApi;
    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private CommissionStrategyApi commissionStrategyApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;

    @Override
    public boolean supports(TradeOrderItemDO item) {
        List<Long> spuIds = Collections.singletonList(item.getSpuId());
        return productSpuApi.isMbProduct(spuIds) && !productSpuApi.isCareerProduct(spuIds);
    }

    @Override
    public void onPayment(TradeOrderDO order, TradeOrderItemDO item) {
        BrokerageUserRespDTO buyerBrokerage = brokerageUserApi.getBrokerageUser(order.getUserId());
        if (buyerBrokerage != null && Boolean.TRUE.equals(buyerBrokerage.getBrokerageEnabled())) {
            // 自身就是代理，不给上级分佣
            return;
        }

        List<CommissionHierarchyDTO> hierarchies = buildHierarchies(order.getUserId());
        if (hierarchies.size() <= 1) {
            // 无上级链路
            return;
        }

        // 先尝试基于物料库存的直供逻辑，若处理成功则无需走策略服务
        if (tryHandleInventoryCommission(order, item, hierarchies)) {
            return;
        }

        CommissionCalculateReqDTO reqDTO = buildCommissionRequest(order, item, hierarchies);
        CommissionCalculateRespDTO respDTO = commissionStrategyApi.calculateCommission(reqDTO);
        List<BrokerageRecordCreateReqDTO> records = respDTO.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("[MbProductEventHandler][onPayment] 未生成分佣记录，orderItemId={}", item.getId());
            return;
        }
        applyMaterialGrants(respDTO.getMaterialGrants());
        brokerageRecordApi.addBrokerageBatch(records);
        log.info("[MbProductEventHandler][onPayment] 生成分佣记录 {} 条，orderItemId={}", records.size(), item.getId());
    }

    @Override
    public void onRefund(TradeOrderDO order, TradeOrderItemDO item) {
        String bizId = String.valueOf(item.getId());
        List<CommissionHierarchyDTO> hierarchies = buildHierarchies(order.getUserId());
        List<BrokerageRecordDO> records = brokerageRecordService.getBrokerageRecordsByBizId(bizId);
        if (!CollectionUtils.isEmpty(records)) {
            List<BrokerageRecordDO> directSupplyRecords = records.stream()
                    .filter(record -> BrokerageRecordBizTypeEnum.MB_DIRECT_SUPPLY.getType().equals(record.getBizType()))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(directSupplyRecords)) {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(item.getSpuId());
                Integer quantity = item.getCount();
                if (definition == null || definition.getId() == null) {
                    log.warn("[MbProductEventHandler][onRefund] 直供回库失败，缺少物料定义，orderItemId={}", item.getId());
                } else if (quantity == null || quantity <= 0) {
                    log.warn("[MbProductEventHandler][onRefund] 直供回库失败，数量非法，orderItemId={} quantity={}", item.getId(), quantity);
                } else {
                    directSupplyRecords.stream()
                            .map(BrokerageRecordDO::getUserId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .forEach(userId -> restoreMaterialStock(userId, definition.getId(), quantity, item.getId()));
                }
            }
        }
        CommissionCalculateReqDTO reqDTO = buildCommissionRequest(order, item, hierarchies);
        CommissionCalculateRespDTO respDTO = commissionStrategyApi.calculateCommission(reqDTO);
        revertMaterialGrants(respDTO.getMaterialGrants(), item.getId());
        brokerageRecordApi.cancelBrokerage(bizId);
        log.info("[MbProductEventHandler][onRefund] 取消分佣，orderItemId={}", item.getId());
    }

    // 尝试识别具备物料库存的上级，优先走直供分佣逻辑
    private boolean tryHandleInventoryCommission(TradeOrderDO order, TradeOrderItemDO item,
            List<CommissionHierarchyDTO> hierarchies) {
        Integer quantity = item.getCount();
        if (quantity == null || quantity <= 0) {
            //数量不足，非直供
            return false;
        }
        MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(item.getSpuId());
        if (definition == null || definition.getId() == null) {
            //非物料
            return false;
        }

        CommissionHierarchyDTO supplier = resolveDirectParentWithStock(definition.getId(), hierarchies, quantity);
        if (supplier == null || supplier.getUserId() == null) {
            return false;
        }

        Integer commissionAmount = item.getPayPrice();
        if (commissionAmount == null) {
            Integer unitPrice = item.getPrice();
            commissionAmount = unitPrice != null ? unitPrice * quantity : 0;
        }
        if (commissionAmount == null || commissionAmount <= 0) {
            log.info("[MbProductEventHandler][tryHandleInventoryCommission] 佣金金额异常，orderItemId={} amount={}",
                    item.getId(), commissionAmount);
            return false;
        }

        // 扣减库存并生成物料流水，确保直供库存准确
        deductMaterialStock(supplier.getUserId(), definition.getId(), quantity, item.getId());

        // 构建直供佣金记录（收益 + 平台出资）并落库
        List<BrokerageRecordCreateReqDTO> records =
                buildDirectCommissionRecords(order, item, hierarchies, supplier, commissionAmount);
        brokerageRecordApi.addBrokerageBatch(records);
        log.info("[MbProductEventHandler][tryHandleInventoryCommission] 直供分佣成功，orderItemId={} supplier={} quantity={} amount={}",
                item.getId(), supplier.getUserId(), quantity, commissionAmount);
        return true;
    }

    // 找到直属上级并校验是否有足够库存
    private CommissionHierarchyDTO resolveDirectParentWithStock(Long materialId,
            List<CommissionHierarchyDTO> hierarchies, int quantity) {
        for (CommissionHierarchyDTO hierarchy : hierarchies) {
            if (hierarchy == null || hierarchy.getUserId() == null
                    || !isParentHierarchy(hierarchy)) {
                continue;
            }
            MaterialBalanceRespDTO balance = materialApi.getBalance(hierarchy.getUserId(), materialId);
            if (balance != null && balance.getAvailableBalance() != null
                    && balance.getAvailableBalance() >= quantity) {
                return hierarchy;
            }
        }
        return null;
    }

    // 判断是否为购买者的直属上级
    private boolean isParentHierarchy(CommissionHierarchyDTO hierarchy) {
        if (hierarchy == null) {
            return false;
        }
        String key = hierarchy.getHierarchy();
        return key != null && "PARENT".equalsIgnoreCase(key);
    }

    // 扣减指定用户的物料库存，并记录幂等键
    private void deductMaterialStock(Long userId, Long materialId, Integer quantity, Long orderItemId) {
        MaterialActDTO act = MaterialActDTO.builder()
                .userId(userId)
                .materialId(materialId)
                .quantity(quantity)
                .direction(MaterialActDirectionEnum.OUT)
                .bizKey(buildStockBizKey(orderItemId, userId))
                .bizType(MATERIAL_STOCK_OUT_BIZ_TYPE)
                .reason("商品销售出库:{}".replace("{}", String.valueOf(orderItemId)))
                .build();
        materialApi.applyActs(Collections.singletonList(act));
    }

    // 恢复指定用户的物料库存
    private void restoreMaterialStock(Long userId, Long materialId, Integer quantity, Long orderItemId) {
        MaterialActDTO act = MaterialActDTO.builder()
                .userId(userId)
                .materialId(materialId)
                .quantity(quantity)
                .direction(MaterialActDirectionEnum.IN)
                .bizKey(buildStockBizKey(orderItemId, userId))
                .bizType(MATERIAL_STOCK_OUT_FUND_BIZ_TYPE)
                .reason("商品售后回库：{}".replace("{}", String.valueOf(orderItemId)))
                .build();
        materialApi.applyActs(Collections.singletonList(act));
        log.info("[MbProductEventHandler][restoreMaterialStock] 回补库存，orderItemId={} userId={} materialId={} quantity={}",
                orderItemId, userId, materialId, quantity);
    }

    /**
     * 直供商品场景下需要同步发放物料，此处借助 MaterialApi 入账。
     */
    private void applyMaterialGrants(List<CommissionMaterialGrantDTO> grants) {
        List<MaterialActDTO> acts = CommissionMaterialGrantHelper.buildActs(
            grants,
            MaterialActDirectionEnum.IN,
            null,
            "商城分佣物料奖励");
        if (!acts.isEmpty()) {
            materialApi.applyActs(acts);
        }
    }

    /**
     * 退款时回收通过策略发放的物料，确保账实一致。
     */
    private void revertMaterialGrants(List<CommissionMaterialGrantDTO> grants, Long orderItemId) {
        List<MaterialActDTO> acts = CommissionMaterialGrantHelper.buildActs(
            grants,
            MaterialActDirectionEnum.OUT,
            orderItemId != null ? "REFUND-" + orderItemId : "REFUND",
            "商城分佣物料退回");
        if (!acts.isEmpty()) {
            materialApi.applyActs(acts);
        }
    }

    // 构建直供佣金记录（收益 + 平台出资），金额使用订单项的实付总额
    private List<BrokerageRecordCreateReqDTO> buildDirectCommissionRecords(TradeOrderDO order, TradeOrderItemDO item,
            List<CommissionHierarchyDTO> hierarchies, CommissionHierarchyDTO supplier, Integer commissionAmount) {
        CommissionHierarchyDTO self = CollectionUtils.isEmpty(hierarchies) ? null : hierarchies.get(0);
        List<BrokerageRecordCreateReqDTO> records = new ArrayList<>(2);

        BrokerageRecordCreateReqDTO incomeRecord = new BrokerageRecordCreateReqDTO();
        incomeRecord.setUserId(supplier.getUserId());
        incomeRecord.setBizId(String.valueOf(item.getId()));
        incomeRecord.setBizType(BrokerageRecordBizTypeEnum.MB_DIRECT_SUPPLY.getType());
        incomeRecord.setTitle(DIRECT_COMMISSION_TITLE);
        incomeRecord.setDescription(DIRECT_COMMISSION_TITLE);
        incomeRecord.setPrice(commissionAmount);
        incomeRecord.setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        incomeRecord.setSourceUserLevel(self != null ? self.getLevel() : null);
        incomeRecord.setSourceUserId(order.getUserId());
        incomeRecord.setBizCategory(BrokerageRecordBizCategoryEnum.MALL_ORDER.getType());
        records.add(incomeRecord);

        BrokerageRecordCreateReqDTO fundRecord = new BrokerageRecordCreateReqDTO();
        fundRecord.setUserId(PLATFORM_VIRTUAL_USER_ID);
        fundRecord.setBizId(String.valueOf(item.getId()));
        fundRecord.setBizType(BrokerageRecordBizTypeEnum.MB_PLATFORM_DIRECT_FUND.getType());
        fundRecord.setTitle(BrokerageRecordBizTypeEnum.MB_PLATFORM_DIRECT_FUND.getTitle());
        fundRecord.setDescription(BrokerageRecordBizTypeEnum.MB_PLATFORM_DIRECT_FUND.getDescription());
        fundRecord.setPrice(-commissionAmount);
        fundRecord.setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        fundRecord.setSourceUserLevel(self != null ? self.getLevel() : null);
        fundRecord.setSourceUserId(order.getUserId());
        fundRecord.setBizCategory(BrokerageRecordBizCategoryEnum.MALL_ORDER.getType());
        records.add(fundRecord);
        return records;
    }

    private String buildStockBizKey(Long orderItemId, Long userId) {
        return "MB_STOCK_OUT:" + orderItemId + ":" + userId;
    }

    private List<CommissionHierarchyDTO> buildHierarchies(Long userId) {
        List<CommissionHierarchyDTO> list = new ArrayList<>();
        CommissionHierarchyDTO self = createHierarchy("SELF", userId, resolveUserLevel(userId), true);
        if (self != null) {
            list.add(self);
        }

        BrokerageUserRespDTO parent = brokerageUserApi.getBindBrokerageUser(userId);
        if (parent != null && Boolean.TRUE.equals(parent.getBrokerageEnabled())) {
            CommissionHierarchyDTO parentDto = createHierarchy("PARENT", parent.getId(), resolveUserLevel(parent.getId()), true);
            if (parentDto != null) {
                list.add(parentDto);
            }
            BrokerageUserRespDTO grand = brokerageUserApi.getBindBrokerageUser(parent.getId());
            if (grand != null && Boolean.TRUE.equals(grand.getBrokerageEnabled())) {
                CommissionHierarchyDTO grandDto = createHierarchy("GRANDPARENT", grand.getId(), resolveUserLevel(grand.getId()), true);
                if (grandDto != null) {
                    list.add(grandDto);
                }
            }
        }

        BrokerageUserRespDTO boss = brokerageUserApi.getFirstBossBrokerageUser(userId);
        if (boss != null && Boolean.TRUE.equals(boss.getBrokerageEnabled())) {
            CommissionHierarchyDTO teamDto = createHierarchy("TEAM", boss.getId(), resolveUserLevel(boss.getId()), true);
            if (teamDto != null) {
                list.add(teamDto);
            }
        }
        return list;
    }

    private CommissionHierarchyDTO createHierarchy(String hierarchy, Long userId, Integer level, Boolean enabled) {
        if (userId == null) {
            return null;
        }
        CommissionHierarchyDTO dto = new CommissionHierarchyDTO();
        dto.setHierarchy(hierarchy);
        dto.setUserId(userId);
        dto.setLevel(level);
        dto.setBrokerageEnabled(enabled);
        return dto;
    }

    private Integer resolveUserLevel(Long userId) {
        if (userId == null) {
            return null;
        }
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        if (user == null || user.getLevelId() == null) {
            return null;
        }
        MemberLevelRespDTO level = memberLevelApi.getMemberLevel(user.getLevelId());
        return level != null ? level.getLevel() : null;
    }

    private CommissionCalculateReqDTO buildCommissionRequest(TradeOrderDO order, TradeOrderItemDO item,
        List<CommissionHierarchyDTO> hierarchies) {
        CommissionCalculateReqDTO reqDTO = new CommissionCalculateReqDTO();
        reqDTO.setBizType(CommissionBizTypeConstants.MALL_GOODS_ORDER);
        reqDTO.setProductId(item.getSpuId());
        CommissionOrderDTO orderDTO = new CommissionOrderDTO();
        orderDTO.setOrderId(item.getId());
        orderDTO.setProductId(item.getSpuId());
        orderDTO.setQuantity(item.getCount());
        orderDTO.setUnitPrice(item.getPrice());
        orderDTO.setTotalPrice(item.getPayPrice());
        reqDTO.setOrder(orderDTO);
        reqDTO.setHierarchies(hierarchies);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(BrokerageRecordBizCategoryEnum.ATTR_KEY, BrokerageRecordBizCategoryEnum.MALL_ORDER.getType());
        reqDTO.setAttributes(attributes);
        return reqDTO;
    }
}
