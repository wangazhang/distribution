package com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.api.commission.CommissionStrategyApi;
import com.hissp.distribution.module.mb.api.commission.dto.*;
import com.hissp.distribution.module.mb.api.commission.util.CommissionMaterialGrantHelper;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionVirtualAccounts;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 补货分佣处理器 处理补货订单的分佣业务逻辑
 */
@Service
@Slf4j
public class RestockCommissionHandler {

    @Resource
    private BrokerageUserApi brokerageUserApi;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private MemberLevelApi memberLevelApi;

    @Resource
    private BrokerageRecordApi brokerageRecordApi;

    @Resource
    private CommissionStrategyApi commissionStrategyApi;

    @Resource
    private MaterialApi materialApi;

    /**
     * 处理补货分佣
     *
     * @param order 补货订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRestockCommission(MbOrderDO order) {
        try {
            String bizType = order.getBizType();
            if (!StringUtils.hasText(bizType)) {
                bizType = CommissionBizTypeConstants.RESTOCK;
            }
            CommissionCalculateReqDTO reqDTO = buildCommissionRequest(order, bizType);
            // 让佣金策略统一计算“正向奖励 + 反向出资” 组合，便于后续拓展不同策略。
            CommissionCalculateRespDTO respDTO = commissionStrategyApi.calculateCommission(reqDTO);
            List<BrokerageRecordCreateReqDTO> records = respDTO.getRecords();
            if (CollectionUtils.isEmpty(records)) {
                log.info("[handleRestockCommission][未生成分佣记录，订单({})]", order.getId());
                return;
            }
            // 在落库前校验补货收益方库存，必要时统一切换为总部出资
            adjustFundingAccountWhenInsufficient(order, records);
            applyMaterialGrants(respDTO.getMaterialGrants());
            // 将补货产生的正向佣金全部打上“待结算”标记，等待渠道妥投完成后再解冻。
            records.forEach(this::markAsFrozenPendingSettlement);
            brokerageRecordApi.addBrokerageBatch(records);
            log.info("[handleRestockCommission][补货订单({})分佣处理完成，策略={}，记录数={}]", order.getId(), respDTO.getPolicyCode(),
                records.size());
        } catch (Exception e) {
            log.error("[handleRestockCommission][补货订单({})分佣处理异常]", order.getId(), e);
            throw e;
        }
    }

    public CommissionCalculateReqDTO buildCommissionRequest(MbOrderDO order, String bizType) {
        CommissionCalculateReqDTO reqDTO = new CommissionCalculateReqDTO();
        reqDTO.setBizType(bizType);
        reqDTO.setProductId(order.getProductId());
        CommissionOrderDTO orderDTO = new CommissionOrderDTO();
        orderDTO.setOrderId(order.getId());
        orderDTO.setProductId(order.getProductId());
        orderDTO.setQuantity(order.getQuantity());
        orderDTO.setUnitPrice(order.getUnitPrice());
        orderDTO.setTotalPrice(order.getTotalPrice());
        reqDTO.setOrder(orderDTO);
        reqDTO.setHierarchies(buildHierarchies(order.getAgentUserId()));
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(BrokerageRecordBizCategoryEnum.ATTR_KEY, BrokerageRecordBizCategoryEnum.MB_ORDER.getType());
        reqDTO.setAttributes(attributes);
        return reqDTO;
    }

    private void markAsFrozenPendingSettlement(BrokerageRecordCreateReqDTO record) {
        if (record == null) {
            return;
        }
        Integer price = record.getPrice();
        if (price == null) {
            return;
        }
        record.setUnfreezeTime(null);
        if (price >= 0) {
            // 正向收入：先写 0，后续会根据配置补齐冻结天数
            record.setFrozenDays(0);
            record.setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        } else {
            // 逆向出资：交由 addBrokerage 注入默认冻结天数
            record.setFrozenDays(0);
            record.setStatus(BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus());
        }
    }

    /**
     * 在冻结记录前兜底成员补货收益方：
     * 1. 读取订单对应物料定义，基于补货数量校验收益方库存；
     * 2. 仅针对 3001 "成员补货商品收入" 记录做库存判定；
     * 3. 库存不足时将该收益记录切换为总部虚拟账号；
     * 4. 一旦触发兜底，所有由原出资方出资的所有负向出资记录也要同样切换，以保证账务平衡。
     * 5.
     */
    private void adjustFundingAccountWhenInsufficient(MbOrderDO order, List<BrokerageRecordCreateReqDTO> records) {
        if (order == null || CollectionUtils.isEmpty(records)) {
            return;
        }
        //获取补货数量
        Integer quantity = order.getQuantity();
        if (quantity == null || quantity <= 0) {
            return;
        }
        //获取定义
        MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
        if (definition == null || definition.getId() == null) {
            return;
        }
        Long materialId = definition.getId();
        //Long platformUserId = CommissionVirtualAccounts.PLATFORM_VIRTUAL_USER_ID;
        Long hqUserId = CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID;
        boolean needFallback = false;
        Long originalUserId = null;
        for (BrokerageRecordCreateReqDTO record : records) {
            // 只处理“成员补货商品收入”记录，其余按策略默认逻辑走
            if (record == null || !Objects.equals(record.getBizType(),
                    BrokerageRecordBizTypeEnum.MB_MEMBER_RESTOCK_INCOME.getType())) {
                continue;
            }
            //补货全额收入者，即为出资者，当库存不足需要切换为总部兜底
            //原出资者
             originalUserId = record.getUserId();
            if (originalUserId == null || Objects.equals(originalUserId, hqUserId)) {
                continue;
            }
            int available = queryAvailableBalance(originalUserId, materialId);
            if (available >= quantity) {
                //库存足够
                continue;
            }
            //库存不够，触发兜底，将收入归到总公司
            log.info("[handleRestockCommission][成员补货收益方库存不足，落总部兜底 orderId={} userId={} materialDef={} need={} available={}]",
                order.getId(), originalUserId, materialId, quantity, available);
            record.setUserId(hqUserId);
            needFallback = true;
        }
        if (!needFallback) {
            return;
        }
        // 同步把所有负向出资流水且为原出资账号的记录切换为总部账号，确保账务一致
        Long finalOriginalUserId = originalUserId;
        records.stream()
                .filter(Objects::nonNull)
                .filter(record -> Objects.equals(record.getUserId(), finalOriginalUserId)&& record.getPrice() != null && record.getPrice() < 0)
                .forEach(record -> {
                    record.setUserId(hqUserId);
                    record.setBizType(BrokerageRecordBizTypeEnum.MB_HQ_RESTOCK_FUND.getType());
                    record.setTitle(BrokerageRecordBizTypeEnum.MB_HQ_RESTOCK_FUND.getTitle());
                    record.setDescription(BrokerageRecordBizTypeEnum.MB_HQ_RESTOCK_FUND.getDescription());
                });
        log.info("[handleRestockCommission][成员补货收益归总部，出资由总部兜底 orderId={} materialDef={} hqUser={}]",
            order.getId(), materialId, hqUserId);
    }

    // 查询用户在指定物料上的可用库存
    private int queryAvailableBalance(Long userId, Long materialId) {
        if (userId == null || materialId == null) {
            return 0;
        }
        // Material 模块返回可能为空，这里统一做 0 兜底
        MaterialBalanceRespDTO balance = materialApi.getBalance(userId, materialId);
        return balance != null && balance.getAvailableBalance() != null ? balance.getAvailableBalance() : 0;
    }

    private List<CommissionHierarchyDTO> buildHierarchies(Long userId) {
        List<CommissionHierarchyDTO> list = new ArrayList<>();
        // self
        Integer selfLevel = getUserMemberLevelByUserId(userId);
        CommissionHierarchyDTO self = createHierarchy("SELF", userId, selfLevel, true);
        if (self != null) {
            list.add(self);
        }

        BrokerageUserRespDTO firstUser = brokerageUserApi.getBindBrokerageUser(userId);
        if (firstUser != null && Boolean.TRUE.equals(firstUser.getBrokerageEnabled())) {
            CommissionHierarchyDTO parent = createHierarchy("PARENT", firstUser.getId(), getUserMemberLevelByUserId(firstUser.getId()), true);
            if (parent != null) {
                list.add(parent);
            }
            BrokerageUserRespDTO secondUser = brokerageUserApi.getBindBrokerageUser(firstUser.getId());
            if (secondUser != null && Boolean.TRUE.equals(secondUser.getBrokerageEnabled())) {
                CommissionHierarchyDTO grand = createHierarchy("GRANDPARENT", secondUser.getId(),
                    getUserMemberLevelByUserId(secondUser.getId()), true);
                if (grand != null) {
                    list.add(grand);
                }
            }
        }
        BrokerageUserRespDTO bossUser = brokerageUserApi.getFirstBossBrokerageUser(userId);
        if (bossUser != null && Boolean.TRUE.equals(bossUser.getBrokerageEnabled())) {
            // 找到团队/分公司 => 作为 Team 出资主体
            CommissionHierarchyDTO team = createHierarchy("TEAM", bossUser.getId(), getUserMemberLevelByUserId(bossUser.getId()), true);
            if (team != null) {
                list.add(team);
            }
        } else {
            // 未找到上级时，默认由总部平台出资
            CommissionHierarchyDTO team = createHierarchy("TEAM", CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID, null, true);
            if (team != null) {
                list.add(team);
            }
        }
        // 平台层级：用于对接平台自营或中转策略
        CommissionHierarchyDTO platform = createHierarchy(CommissionVirtualAccounts.HIERARCHY_PLATFORM,
                CommissionVirtualAccounts.PLATFORM_VIRTUAL_USER_ID, null, true);
        if (platform != null) {
            list.add(platform);
        }

        // 冗余补录 HQ 层级，方便策略中单独针对平台处理
        CommissionHierarchyDTO hq = createHierarchy(CommissionVirtualAccounts.HIERARCHY_HQ,
                CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID, null, true);
        if (hq != null) {
            list.add(hq);
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

    private Integer getUserMemberLevelByUserId(Long userId) {
        MemberUserRespDTO userInfo = memberUserApi.getUser(userId);
        if (userInfo == null || userInfo.getLevelId() == null) {
            return null;
        }
        MemberLevelRespDTO levelInfo = memberLevelApi.getMemberLevel(userInfo.getLevelId());
        if (levelInfo == null || levelInfo.getLevel() == null) {
            return null;
        }
        return levelInfo.getLevel();
    }

    /**
     * 补货产生的物料奖励直接计入相应的代理库存。
     */
    private void applyMaterialGrants(List<CommissionMaterialGrantDTO> grants) {
        List<MaterialActDTO> acts = CommissionMaterialGrantHelper.buildActs(
            grants,
            MaterialActDirectionEnum.IN,
            null,
            "补货分佣物料奖励");
        if (!acts.isEmpty()) {
            materialApi.applyActs(acts);
        }
    }
}
