package com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.dal.mysql.trade.MbOrderMapper;
import com.hissp.distribution.module.mb.domain.service.trade.MbVirtualDeliveryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 物料转化订单处理器
 * 统一处理物料转化订单的状态流转和业务逻辑
 *
 * @author azhanga
 */
@Service
@Slf4j
public class MaterialConvertOrderHandler {

    @Resource
    private MbOrderMapper mbOrderMapper;
    
    @Resource
    private MaterialApi materialApi;

    @Resource
    private MbVirtualDeliveryService mbVirtualDeliveryService;
    
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
     * 物料转化订单-处理订单支付成功
     * 
     * @param orderId 转化订单ID
     * @return 处理结果
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handlePaymentSuccess(Long orderId) {
        // 1. 获取物料转化订单
        MbOrderDO order = mbOrderMapper.selectById(orderId);
        if (order == null) {
            log.warn("[handlePaymentSuccess][物料转化订单不存在: {}]", orderId);
            return false;
        }

        // 2. 检查订单状态 - 幂等性判断
        if (OrderStatus.COMPLETED.equals(order.getStatus())
                || OrderStatus.DELIVERED.equals(order.getStatus())) {
            log.info("[handlePaymentSuccess][物料转化订单已处理完成，不再重复处理: {}]", orderId);
            return true; // 已处理成功，返回true
        }

        if (OrderStatus.PROCESSING.equals(order.getStatus())) {
            log.warn("[handlePaymentSuccess][物料转化订单正在处理中，可能存在并发处理: {}]", orderId);
            return false;
        }

        // 3. 原子更新订单状态为 PROCESSING，确保单次处理
        String currentStatus = order.getStatus();
        int updated = mbOrderMapper.updateByIdAndStatus(order.getId(), currentStatus,
                new MbOrderDO().setStatus(OrderStatus.PROCESSING));
        if (updated == 0) {
            log.warn("[handlePaymentSuccess][物料转化订单状态已变化，忽略处理: {} -> {}]", orderId, currentStatus);
            return false;
        }
        order.setStatus(OrderStatus.PROCESSING);

        // 4. 延迟触发渠道虚拟发货
        mbVirtualDeliveryService.scheduleVirtualDelivery(order.getId());
        return true;
    }

    /**
     * 收货后执行物料转化出入库
     *
     * @param order 转化订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleAfterReceive(MbOrderDO order) {
        if (order == null) {
            return;
        }
        processMaterialConvert(order);
    }

    /**
     * 处理物料转化：减少原料，增加目标物料
     *
     * @param order 转化订单
     */
    private void processMaterialConvert(MbOrderDO order) {
        MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(order.getProductId());
        if (definition == null) {
            log.warn("[processMaterialConvert][物料定义不存在，无法转化: {}]", order.getProductId());
            markOrderFailed(order);
            throw new IllegalStateException("物料定义不存在");
        }
        if (!Boolean.TRUE.equals(definition.getSupportConvert())) {
            log.warn("[processMaterialConvert][物料({})未开启转化能力]", order.getProductId());
            markOrderFailed(order);
            throw new IllegalStateException("物料未开启转化能力");
        }
        Long targetSpuId = definition.getConvertedSpuId();
        if (targetSpuId == null) {
            log.warn("[processMaterialConvert][物料({})未配置转化目标]", order.getProductId());
            markOrderFailed(order);
            throw new IllegalStateException("物料未配置转化目标");
        }

        MaterialDefinitionRespDTO targetDefinition = materialApi.getDefinitionBySpuId(targetSpuId);
        if (targetDefinition == null) {
            log.warn("[processMaterialConvert][物料({})配置的转化目标({})未找到物料定义]", order.getProductId(), targetSpuId);
            markOrderFailed(order);
            throw new IllegalStateException("转化目标物料定义不存在");
        }

        MaterialActDTO decreaseActDTO = MaterialActDTO.builder()
                .userId(order.getAgentUserId())
                .materialId(definition.getId())
                .quantity(order.getQuantity())
                .direction(MaterialActDirectionEnum.OUT)
                .bizKey("convert_order_" + order.getId() + "_decrease")
                .bizType("CONVERT_DECREASE")
                .reason("物料转化-减少原料: " + order.getId())
                .build();

        MaterialActDTO increaseActDTO = MaterialActDTO.builder()
                .userId(order.getAgentUserId())
                .materialId(targetDefinition.getId())
                .quantity(order.getQuantity())
                .direction(MaterialActDirectionEnum.IN)
                .bizKey("convert_order_" + order.getId() + "_increase")
                .bizType("CONVERT_INCREASE")
                .reason("物料转化-增加目标物料: " + order.getId())
                .build();

        try {
            materialApi.applyActs(List.of(decreaseActDTO, increaseActDTO));

            // 转化成功后保持订单处于 DELIVERED，由后续流程完成签收
            order.setStatus(OrderStatus.DELIVERED);
            LocalDateTime deliveryTime = order.getDeliveryTime() != null ? order.getDeliveryTime() : LocalDateTime.now();
            order.setDeliveryTime(deliveryTime);
            mbOrderMapper.updateById(order);

            log.info("[processMaterialConvert][物料转化订单处理成功: {} 用户:{} 原料物料:{} 目标物料:{} 数量:{}]",
                    order.getId(), order.getAgentUserId(), definition.getId(), targetDefinition.getId(),
                    order.getQuantity());
        } catch (Exception e) {
            log.error("[processMaterialConvert][物料转化订单处理异常: {}]", order.getId(), e);
            markOrderFailed(order);
            throw e;
        }
    }

    private void markOrderFailed(MbOrderDO order) {
        try {
            order.setStatus(OrderStatus.FAILED);
            mbOrderMapper.updateById(order);
        } catch (Exception ex) {
            log.error("[markOrderFailed][更新订单状态失败: {}]", order.getId(), ex);
        }
    }
}
