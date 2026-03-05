package com.hissp.distribution.module.trade.service.packagex.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import cn.hutool.core.util.ObjectUtil;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
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
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageUserService;
import com.hissp.distribution.module.trade.service.packagex.PackageCommissionService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PackageCommissionServiceImpl implements PackageCommissionService {

    @Resource
    private BrokerageUserService brokerageUserService;
    @Resource
    private BrokerageRecordApi brokerageRecordApi;
    @Resource
    private CommissionStrategyApi commissionStrategyApi;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private MaterialApi materialApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void commissionOnPaid(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack) {
        CommissionCalculateReqDTO reqDTO = buildCommissionRequest(order, orderItem, pack);
        CommissionCalculateRespDTO respDTO = commissionStrategyApi.calculateCommission(reqDTO);
        List<BrokerageRecordCreateReqDTO> records = respDTO.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            log.info("[PackageCommissionService][commissionOnPaid] 无分佣记录生成，orderItemId={}", orderItem.getId());
            return;
        }
        applyFrozenConfig(records);
        applyMaterialGrants(respDTO.getMaterialGrants());
        brokerageRecordApi.addBrokerageBatch(records);
        log.info("[PackageCommissionService][commissionOnPaid] 生成分佣记录 {} 条，orderItemId={}", records.size(),
            orderItem.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelCommission(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack) {
        CommissionCalculateReqDTO reqDTO = buildCommissionRequest(order, orderItem, pack);
        CommissionCalculateRespDTO respDTO = commissionStrategyApi.calculateCommission(reqDTO);
        revertMaterialGrants(respDTO.getMaterialGrants(), orderItem.getId());
        brokerageRecordApi.cancelBrokerage(String.valueOf(orderItem.getId()));
    }

    private CommissionCalculateReqDTO buildCommissionRequest(TradeOrderDO order, TradeOrderItemDO orderItem,
        ProductPackageRespDTO pack) {
        CommissionCalculateReqDTO reqDTO = new CommissionCalculateReqDTO();
        reqDTO.setBizType(CommissionBizTypeConstants.PACKAGE_ORDER);
        reqDTO.setProductId(orderItem.getSpuId());
        reqDTO.setPackageId(pack != null ? pack.getId() : null);
        CommissionOrderDTO orderDTO = new CommissionOrderDTO();
        orderDTO.setOrderId(orderItem.getId());
        orderDTO.setProductId(orderItem.getSpuId());
        orderDTO.setPackageId(pack != null ? pack.getId() : null);
        orderDTO.setQuantity(orderItem.getCount());
        orderDTO.setUnitPrice(orderItem.getPrice());
        orderDTO.setTotalPrice(orderItem.getPayPrice());
        reqDTO.setOrder(orderDTO);
        reqDTO.setHierarchies(buildHierarchies(order.getUserId()));
        Map<String, Object> attributes = new HashMap<>();
        //package order  也是商城订单，记录在trade_order表
        attributes.put(BrokerageRecordBizCategoryEnum.ATTR_KEY, BrokerageRecordBizCategoryEnum.MALL_ORDER.getType());
        reqDTO.setAttributes(attributes);
        return reqDTO;
    }

    private List<CommissionHierarchyDTO> buildHierarchies(Long userId) {
        List<CommissionHierarchyDTO> list = new ArrayList<>();
        CommissionHierarchyDTO self = createHierarchy("SELF", userId, resolveUserLevel(userId), true);
        if (self != null) {
            list.add(self);
        }

        BrokerageUserDO first = brokerageUserService.getBindBrokerageUser(userId);
        if (first != null && Boolean.TRUE.equals(first.getBrokerageEnabled())) {
            CommissionHierarchyDTO parent = createHierarchy("PARENT", first.getId(), resolveUserLevel(first.getId()), true);
            if (parent != null) {
                list.add(parent);
            }
            BrokerageUserDO second = brokerageUserService.getBindBrokerageUser(first.getId());
            if (second != null && Boolean.TRUE.equals(second.getBrokerageEnabled())) {
                CommissionHierarchyDTO grand = createHierarchy("GRANDPARENT", second.getId(),
                    resolveUserLevel(second.getId()), true);
                if (grand != null) {
                    list.add(grand);
                }
            }
        }

        BrokerageUserDO boss = brokerageUserService.getFirstBossBrokerageUser(userId);
        if (boss != null && Boolean.TRUE.equals(boss.getBrokerageEnabled())) {
            CommissionHierarchyDTO team = createHierarchy("TEAM", boss.getId(), resolveUserLevel(boss.getId()), true);
            if (team != null) {
                list.add(team);
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

    private void applyFrozenConfig(List<BrokerageRecordCreateReqDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        records.forEach(this::markAsFrozenPendingSettlement);
    }

    private void markAsFrozenPendingSettlement(BrokerageRecordCreateReqDTO record) {
        if (record == null) {
            return;
        }
        record.setFrozenDays(0);
        record.setUnfreezeTime(null);
        if (record.getPrice() != null && record.getPrice() > 0) {
            record.setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        } else {
            record.setStatus(ObjectUtil.defaultIfNull(record.getStatus(),
                BrokerageRecordStatusEnum.SETTLEMENT.getStatus()));
        }
    }

    /**
     * 将策略返回的物料奖励转换为物料模块可识别的入账动作。
     */
    private void applyMaterialGrants(List<CommissionMaterialGrantDTO> grants) {
        List<MaterialActDTO> acts = CommissionMaterialGrantHelper.buildActs(
            grants,
            MaterialActDirectionEnum.IN,
            null,
            "套包分佣物料奖励");
        if (!acts.isEmpty()) {
            materialApi.applyActs(acts);
        }
    }

    /**
     * 退款场景下回收物料奖励，保持佣金与物料同步。
     */
    private void revertMaterialGrants(List<CommissionMaterialGrantDTO> grants, Long orderItemId) {
        List<MaterialActDTO> acts = CommissionMaterialGrantHelper.buildActs(
            grants,
            MaterialActDirectionEnum.OUT,
            orderItemId != null ? "REFUND-" + orderItemId : "REFUND",
            "套包分佣物料退回");
        if (!acts.isEmpty()) {
            materialApi.applyActs(acts);
        }
    }
}
