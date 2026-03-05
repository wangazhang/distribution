package com.hissp.distribution.module.trade.service.packagex.impl;

import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelReqDTO;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageConstants;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageItemTypeEnum;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.dal.dataobject.packagex.TradePackageGrantRecordDO;
import com.hissp.distribution.module.trade.dal.mysql.packagex.TradePackageGrantRecordMapper;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageUserService;
import com.hissp.distribution.module.trade.service.packagex.PackageGrantService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class PackageGrantServiceImpl implements PackageGrantService {

    @Resource
    private BrokerageUserService brokerageUserService;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private TradePackageGrantRecordMapper grantRecordMapper;
    @Resource
    private ProductPackageApi productPackageApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private MemberUserApi memberUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantOnPaid(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack) {
        // Idempotency check
        TradePackageGrantRecordDO existed = grantRecordMapper.selectByOrderItemId(orderItem.getId());
        if (existed != null && Objects.equals(existed.getGrantStatus(), 1)) {
            return;
        }
        if (pack == null) {
            log.warn("[grantOnPaid][orderItemId={}] 套包信息为空，跳过发放", orderItem.getId());
            return;
        }
        Long userId = order.getUserId();

        // Grant entitlements
        applyEntitlements(userId, pack, orderItem.getId());

        // Log the grant
        Map<String, Object> details = new HashMap<>();
        details.put("packageId", pack.getId());
        details.put("items", pack.getItems());
        TradePackageGrantRecordDO rec = TradePackageGrantRecordDO.builder()
                .userId(userId)
                .orderId(order.getId())
                .orderItemId(orderItem.getId())
                .packageId(pack.getId())
                .grantStatus(1)
                .details(details)
                .build();
        grantRecordMapper.insert(rec);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeOnCancel(TradeOrderDO order, TradeOrderItemDO orderItem, ProductPackageRespDTO pack) {
        TradePackageGrantRecordDO existed = grantRecordMapper.selectByOrderItemId(orderItem.getId());
        if (existed == null || Objects.equals(existed.getGrantStatus(), 2)) {
            return;
        }

        // Mark the log as revoked
        TradePackageGrantRecordDO update = new TradePackageGrantRecordDO();
        update.setId(existed.getId());
        update.setGrantStatus(2);
        grantRecordMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void grantManual(Long userId, Long packageId, Long operatorId, String bizKey) {
        if (userId == null || packageId == null) {
            return;
        }
        ProductPackageRespDTO pack = productPackageApi.getPackageById(packageId);
        if (pack == null) {
            log.warn("[grantManual][userId={} packageId={}] 套包不存在，跳过发放", userId, packageId);
            return;
        }
        applyEntitlements(userId, pack, null);
        List<String> materialLogs = applyProductMaterials(userId, pack);
        logManualGrantDetails(userId, packageId, pack, materialLogs);
        log.info("[grantManual][userId={} packageId={} operatorId={} bizKey={}] 手动发放套包权益完成",
            userId, packageId, operatorId, bizKey);
    }

    private void applyEntitlements(Long userId, ProductPackageRespDTO pack, Long bizId) {
        if (pack == null || pack.getItems() == null) {
            return;
        }
        MemberUserRespDTO user = memberUserApi.getUser(userId);
        Integer currentLevelOrder = resolveLevelOrder(user != null ? user.getLevelId() : null);
        for (ProductPackageRespDTO.Item item : pack.getItems()) {
            if (!Objects.equals(item.getItemType(), ProductPackageItemTypeEnum.ENTITLEMENT.getCode())) {
                continue;
            }
            String entitlementType = item.getItemId();
            if (Objects.equals(entitlementType, ProductPackageConstants.ENTITLEMENT_OPEN_BROKERAGE_NAME)) {
                brokerageUserService.updateBrokerageUserEnabled(userId, true);
                continue;
            }
            if (Objects.equals(entitlementType, ProductPackageConstants.ENTITLEMENT_SET_LEVEL_NAME)) {
                Long levelId = extractLevelId(item);
                if (levelId == null) {
                    log.warn("[applyEntitlements][userId={} packageId={}] SET_LEVEL 缺少 levelId", userId, pack.getId());
                    continue;
                }
                MemberLevelRespDTO targetLevel = memberLevelApi.getMemberLevel(levelId);
                if (targetLevel == null) {
                    log.warn("[applyEntitlements][userId={} packageId={}] SET_LEVEL 找不到目标等级, levelId={}",
                        userId, pack.getId(), levelId);
                    continue;
                }
                Integer targetLevelOrder = targetLevel.getLevel();
                // 如果当前用户等级更高则直接跳过，避免降级
                if (currentLevelOrder != null && targetLevelOrder != null && targetLevelOrder < currentLevelOrder) {
                    log.info("[applyEntitlements][userId={} packageId={}] 当前等级更高，跳过发放SET_LEVEL: currentLevelId={} targetLevelId={}",
                        userId, pack.getId(), user != null ? user.getLevelId() : null, levelId);
                    continue;
                }
                MemberLevelReqDTO req = new MemberLevelReqDTO();
                req.setUserId(userId);
                req.setLevelId(levelId);
                req.setReason("套包发放等级");
                req.setEnableBrokerage(false);
                req.setAdminOp(false);
                req.setBizId(bizId);
                req.setBizType(null);
                memberLevelApi.updateUserLevel(req);
                if (targetLevelOrder != null) {
                    currentLevelOrder = targetLevelOrder;
                }
                if (user != null) {
                    user.setLevelId(levelId);
                }
            }
        }
    }

    // 解析等级顺序，便于套包等级与当前等级对比
    private Integer resolveLevelOrder(Long levelId) {
        if (levelId == null) {
            return null;
        }
        MemberLevelRespDTO level = memberLevelApi.getMemberLevel(levelId);
        return level != null ? level.getLevel() : null;
    }

    private List<String> applyProductMaterials(Long userId, ProductPackageRespDTO pack) {
        List<String> materialLogs = new ArrayList<>();
        if (pack == null || pack.getItems() == null) {
            return materialLogs;
        }
        List<MaterialActDTO> acts = new ArrayList<>();
        for (ProductPackageRespDTO.Item item : pack.getItems()) {
            if (!Objects.equals(item.getItemType(), ProductPackageItemTypeEnum.PRODUCT.getCode())) {
                continue;
            }
            try {
                Long spuId = Long.valueOf(item.getItemId());
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(spuId);
                if (definition == null) {
                    log.warn("[applyProductMaterials][userId={} packageId={}] 找不到物料定义, spuId={}",
                        userId, pack.getId(), spuId);
                    materialLogs.add(String.format("spuId=%s(无对应物料定义) x%s",
                        item.getItemId(), item.getItemQuantity()));
                    continue;
                }
                if (!Objects.equals(definition.getStatus(), 1)) {
                    log.warn("[applyProductMaterials][userId={} packageId={}] 物料定义未启用, materialId={}",
                        userId, pack.getId(), definition.getId());
                    materialLogs.add(String.format("materialId=%d(%s,未启用) x%s",
                        definition.getId(), definition.getName(), item.getItemQuantity()));
                    continue;
                }
                Integer quantity = item.getItemQuantity() != null ? item.getItemQuantity() : 1;
                MaterialActDTO act = MaterialActDTO.builder()
                    .userId(userId)
                    .materialId(definition.getId())
                    .quantity(quantity)
                    .direction(MaterialActDirectionEnum.IN)
                    .bizKey(String.format("manual_pkg_%d_%d_%d", userId, pack.getId(), definition.getId()))
                    .bizType("PACKAGE_GRANT")
                    .reason("套包手动发放")
                    .build();
                acts.add(act);
                materialLogs.add(String.format("materialId=%d(%s) x%d",
                    definition.getId(), definition.getName(), quantity));
            } catch (NumberFormatException ex) {
                log.warn("[applyProductMaterials][packageId={} itemId={}] 商品项无法解析为数字，跳过发放",
                    pack.getId(), item.getItemId());
                materialLogs.add(String.format("spuId=%s(解析失败)", item.getItemId()));
            } catch (Exception ex) {
                log.error("[applyProductMaterials][userId={} packageId={}] 处理物料失败 itemId={}",
                    userId, pack.getId(), item.getItemId(), ex);
            }
        }
        if (!acts.isEmpty()) {
            try {
                materialApi.applyActs(acts);
            } catch (Exception ex) {
                log.error("[applyProductMaterials][userId={} packageId={}] 执行物料发放失败", userId, pack.getId(), ex);
            }
        }
        return materialLogs;
    }

    private Long extractLevelId(ProductPackageRespDTO.Item item) {
        if (item.getExtJson() == null) {
            return null;
        }
        Object value = item.getExtJson().get(ProductPackageConstants.EXT_KEY_LEVEL_ID);
        if (value == null) {
            return null;
        }
        try {
            return Long.valueOf(String.valueOf(value));
        } catch (Exception ex) {
            return null;
        }
    }

    private void logManualGrantDetails(Long userId, Long packageId, ProductPackageRespDTO pack,
        List<String> materialLogs) {
        if (materialLogs == null) {
            materialLogs = new ArrayList<>();
        }
        List<String> entitlementLogs = new ArrayList<>();
        if (pack.getItems() != null) {
            for (ProductPackageRespDTO.Item item : pack.getItems()) {
                if (Objects.equals(item.getItemType(), ProductPackageItemTypeEnum.ENTITLEMENT.getCode())) {
                    String entitlementDesc = item.getItemId();
                    if (Objects.equals(item.getItemId(), ProductPackageConstants.ENTITLEMENT_SET_LEVEL_NAME)) {
                        Long levelId = extractLevelId(item);
                        entitlementDesc =
                            String.format("SET_LEVEL(levelId=%s)", levelId != null ? levelId : "null");
                    }
                    entitlementLogs.add(entitlementDesc);
                    continue;
                }
                // 其他类型记录原始信息
                materialLogs.add(String.format("custom itemType=%s id=%s x%s",
                    item.getItemType(), item.getItemId(), item.getItemQuantity()));
            }
        }
        log.info("[grantManual][userId={} packageId={}] 发放权益: {}; 发放物料: {}",
            userId,
            packageId,
            entitlementLogs.isEmpty() ? "无" : String.join(", ", entitlementLogs),
            materialLogs.isEmpty() ? "无" : String.join(", ", materialLogs));
    }
}
