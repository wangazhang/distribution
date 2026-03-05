package com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbOrderMapper;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.CommissionVirtualAccounts;
import com.hissp.distribution.module.mb.domain.service.trade.MbVirtualDeliveryService;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 物料补货订单处理器
 * 统一处理物料补货订单的状态流转和业务逻辑
 *
 * @author azhanga
 */
@Service
@Slf4j
public class MaterialRestockOrderHandler {

    @Resource
    private MbOrderMapper mbOrderMapper;

    @Resource
    private MaterialApi materialApi;

    @Resource
    private RestockCommissionHandler restockCommissionHandler;

    @Resource
    private MbVirtualDeliveryService mbVirtualDeliveryService;

    @Resource
    private BrokerageUserApi brokerageUserApi;

    /**
     * 订单状态常量
     */
    public static final class OrderStatus {
        /**
         * 待处理
         */
        public static final String PENDING = "PENDING";

        /**
         * 处理中
         */
        public static final String PROCESSING = "PROCESSING";

        /**
         * 待确认收货
         */
        public static final String DELIVERED = "DELIVERED";

        /**
         * 已完成
         */
        public static final String COMPLETED = "COMPLETED";

        /**
         * 失败
         */
        public static final String FAILED = "FAILED";
    }

    /**
     * 补货订单-处理订单支付成功
     *
     * @param orderId 补货订单ID
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(Long orderId) {
        // 1. 获取物料补货订单
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            log.warn("[handlePaymentSuccess][补货订单不存在: {}]", orderId);
            return false;
        }

        // 2. 检查订单状态 - 幂等性判断
        if (OrderStatus.COMPLETED.equals(order.getStatus())
                || OrderStatus.DELIVERED.equals(order.getStatus())) {
            log.info("[handlePaymentSuccess][补货订单已处理完成，不再重复处理: {}]", orderId);
            return true; // 已处理成功，返回true
        }

        if (OrderStatus.PROCESSING.equals(order.getStatus())) {
            log.warn("[handlePaymentSuccess][补货订单正在处理中，可能存在并发处理: {}]", orderId);
            return false;
        }

        // 3. 尝试原子更新状态为 PROCESSING，避免重复处理
        String currentStatus = order.getStatus();
        int updated = mbOrderMapper.updateByIdAndStatus(order.getId(), currentStatus,
                new MbOrderDO().setStatus(OrderStatus.PROCESSING));
        if (updated == 0) {
            log.warn("[handlePaymentSuccess][补货订单状态已变化，忽略处理: {} -> {}]", orderId, currentStatus);
            return false;
        }
        order.setStatus(OrderStatus.PROCESSING);

        // 4. 预生成分佣记录，保持与交易单一致的冻结策略
        try {
            restockCommissionHandler.handleRestockCommission(order);
        } catch (Exception e) {
            log.error("[handlePaymentSuccess][补货订单({})分佣处理异常]", orderId, e);
            // 分佣异常不阻断发货流程，由后续任务兜底
        }

        // 5. 延迟触发渠道虚拟发货
        mbVirtualDeliveryService.scheduleVirtualDelivery(order.getId());
        return true;
    }

    /**
     * 收货后为代理补齐库存入账
     *
     * @param order 补货订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleAfterReceive(MbOrderDO order) {
        if (order == null) {
            return;
        }
        increaseUserMaterialBalance(order);
    }

    /**
     * 增加用户物料库存
     *
     * @param order 补货订单
     */
    private void increaseUserMaterialBalance(MbOrderDO order) {
        MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
        if (definition == null) {
            log.warn("[increaseUserMaterialBalance][补货订单({}) 未找到对应物料定义: spu={}]", order.getId(), order.getProductId());
            markOrderFailed(order);
            throw new IllegalStateException("未找到补货物料定义");
        }
        // 组装物料动作：
        //  ① 先为下单代理入账（后续分佣、库存展示都依赖这条记录）；
        //  ② 如果存在出资方，再追加一条向出资方扣减的动作，使库存保持收支平衡。
        List<MaterialActDTO> acts = new ArrayList<>();
        acts.add(MaterialActDTO.builder()
                .userId(order.getAgentUserId())
                .materialId(definition.getId())
                .quantity(order.getQuantity())
                .direction(MaterialActDirectionEnum.IN) // 增加操作
                .bizKey("restock_order_" + order.getId()) // 业务唯一标识
                .bizType("RESTOCK_ORDER") // 补货订单类型
                .reason("补货订单: " + order.getId()) // 操作原因
                .build());

        Long fundingUserId = resolveFundingUserId(order, definition.getId(), order.getQuantity());
        if (fundingUserId != null && !Objects.equals(fundingUserId, order.getAgentUserId())) {
            boolean allowNegative = Objects.equals(fundingUserId, CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID);
            acts.add(MaterialActDTO.builder()
                    .userId(fundingUserId)
                    .materialId(definition.getId())
                    .quantity(order.getQuantity())
                    .direction(MaterialActDirectionEnum.OUT) // 出资扣减
                    .bizKey("restock_order_" + order.getId() + "_fund")
                    .bizType("RESTOCK_FUND")
                    .reason("补货订单出资扣减: " + order.getId())
                    .allowNegative(allowNegative)
                    .build());
        }

        try {
            // 调用物料API更新库存并记录变更
            materialApi.applyActs(acts);

            // 收货时补齐发货时间，保持状态在 DELIVERED
            order.setStatus(OrderStatus.DELIVERED);
            LocalDateTime deliveryTime = Optional.ofNullable(order.getDeliveryTime()).orElse(LocalDateTime.now());
            order.setDeliveryTime(deliveryTime);
            mbOrderMapper.updateById(order);

            log.info("[increaseUserMaterialBalance][补货订单处理成功: {} 用户:{} 物料定义:{} 数量:{} 出资方:{}]",
                    order.getId(), order.getAgentUserId(), definition.getId(), order.getQuantity(), fundingUserId);
        } catch (Exception e) {
            log.error("[increaseUserMaterialBalance][补货订单处理异常: {}]", order.getId(), e);
            markOrderFailed(order);
            throw e;
        }
    }

    private void markOrderFailed(MbOrderDO order) {
        try {
            order.setStatus(OrderStatus.FAILED);
            mbOrderMapper.updateById(order);
        } catch (Exception ex) {
            log.error("[markOrderFailed][更新订单状态为失败时异常: {}]", order.getId(), ex);
        }
    }

    private Long resolveFundingUserId(MbOrderDO order, Long materialDefinitionId, Integer quantity) {
        // 优先尝试团队/分公司账号：只有开启分佣资格且库存充足时才使用该账号
        BrokerageUserRespDTO bossUser = brokerageUserApi.getFirstBossBrokerageUser(order.getAgentUserId());
        Long candidate = null;
        if (bossUser != null && Boolean.TRUE.equals(bossUser.getBrokerageEnabled())) {
            candidate = bossUser.getId();
        }
        if (candidate != null && Objects.equals(candidate, order.getAgentUserId())) {
            candidate = null;
        }
        if (candidate != null) {
            MaterialBalanceRespDTO balance = materialApi.getBalance(candidate, materialDefinitionId);
            int available = balance != null ? Optional.ofNullable(balance.getAvailableBalance()).orElse(0) : 0;
            if (available >= Optional.ofNullable(quantity).orElse(0)) {
                return candidate;
            }
            log.warn("[resolveFundingUserId][出资方库存不足，使用总部资金池补充: orderId={} sponsor={} need={} available={}]",
                    order.getId(), candidate, quantity, available);
        }
        // 总部资金池允许透支，为避免阻断补货流程，直接回落总部虚拟账号。
        log.info("[resolveFundingUserId][使用总部资金池出资，可出现负库存: orderId={} need={}]", order.getId(), quantity);
        return CommissionVirtualAccounts.HQ_VIRTUAL_USER_ID;
    }
}
