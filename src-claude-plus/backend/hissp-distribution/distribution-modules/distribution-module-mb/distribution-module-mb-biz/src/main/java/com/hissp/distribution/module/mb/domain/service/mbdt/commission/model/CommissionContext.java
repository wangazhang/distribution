package com.hissp.distribution.module.mb.domain.service.mbdt.commission.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;

import lombok.Data;

/**
 * 分佣计算上下文，抽象不同业务场景的通用信息
 */
@Data
public class CommissionContext {

    private final Map<String, HierarchyNode> hierarchyMap = new HashMap<>();

    private final Map<String, Object> attributes = new HashMap<>();

    private CommissionOrderSnapshot order;

    private final List<BrokerageRecordCreateReqDTO> brokerageRecords = new ArrayList<>();

    public void assignHierarchy(String hierarchy, BrokerageUserRespDTO user, Integer level) {
        if (hierarchy == null) {
            return;
        }
        HierarchyNode node = hierarchyMap.computeIfAbsent(normalize(hierarchy), key -> new HierarchyNode());
        node.setUser(user);
        node.setUserId(user != null ? user.getId() : null);
        node.setLevel(level);
    }

    public void assignHierarchy(String hierarchy, Long userId, Integer level) {
        if (hierarchy == null) {
            return;
        }
        HierarchyNode node = hierarchyMap.computeIfAbsent(normalize(hierarchy), key -> new HierarchyNode());
        node.setUserId(userId);
        node.setLevel(level);
    }

    public void ensureHierarchy(String hierarchy, Long userId, Integer level) {
        if (hierarchy == null || userId == null) {
            return;
        }
        HierarchyNode node = hierarchyMap.get(normalize(hierarchy));
        if (node == null || node.getUserId() == null) {
            assignHierarchy(hierarchy, userId, level);
        }
    }

    public BrokerageUserRespDTO getHierarchyUser(String hierarchy) {
        HierarchyNode node = hierarchyMap.get(normalize(hierarchy));
        return node != null ? node.getUser() : null;
    }

    public Long getHierarchyUserId(String hierarchy) {
        HierarchyNode node = hierarchyMap.get(normalize(hierarchy));
        return node != null ? node.getUserId() : null;
    }

    public Integer getHierarchyLevel(String hierarchy) {
        HierarchyNode node = hierarchyMap.get(normalize(hierarchy));
        return node != null ? node.getLevel() : null;
    }

    public Long resolveUserIdByHierarchy(String hierarchy) {
        return getHierarchyUserId(hierarchy);
    }

    public Integer lookupLevelByHierarchy(String hierarchy) {
        return getHierarchyLevel(hierarchy);
    }

    public Long getTriggerUserId() {
        return getHierarchyUserId("SELF");
    }

    public Integer getTriggerLevel() {
        return getHierarchyLevel("SELF");
    }

    private String normalize(String hierarchy) {
        return hierarchy == null ? "" : hierarchy.trim().toUpperCase(Locale.ROOT);
    }

    @Data
    public static class HierarchyNode {
        private BrokerageUserRespDTO user;
        private Long userId;
        private Integer level;

        public Long getUserId() {
            if (user != null && user.getId() != null) {
                return user.getId();
            }
            return userId;
        }
    }

    @Data
    public static class CommissionOrderSnapshot {
        private Long orderId;
        private String bizOrderNo;
        private Long productId;
        private Long packageId;
        private Integer quantity;
        private Integer unitPrice;
        private Integer totalPrice;

        public Integer getEffectiveTotalPrice() {
            if (totalPrice != null) {
                return totalPrice;
            }
            if (unitPrice != null && quantity != null) {
                return unitPrice * quantity;
            }
            return null;
        }

        public Integer getEffectiveUnitPrice() {
            if (unitPrice != null) {
                return unitPrice;
            }
            if (totalPrice != null && quantity != null && quantity > 0) {
                return totalPrice / quantity;
            }
            return null;
        }
    }

    public Integer getOrderTotalPrice() {
        return order != null ? order.getEffectiveTotalPrice() : null;
    }

    public Integer getOrderUnitPrice() {
        return order != null ? order.getEffectiveUnitPrice() : null;
    }

    public Integer getOrderQuantity() {
        return order != null ? order.getQuantity() : null;
    }

    public void mergeAttributes(Map<String, Object> extra) {
        if (extra == null || extra.isEmpty()) {
            return;
        }
        extra.forEach((k, v) -> {
            if (k != null && v != null) {
                attributes.put(k, v);
            }
        });
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }
}
