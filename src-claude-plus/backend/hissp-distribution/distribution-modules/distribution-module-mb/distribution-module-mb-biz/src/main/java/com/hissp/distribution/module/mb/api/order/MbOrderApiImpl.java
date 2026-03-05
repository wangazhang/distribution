package com.hissp.distribution.module.mb.api.order;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderDetailRespVO;
import com.hissp.distribution.module.mb.api.order.dto.MbOrderSimpleRespDTO;
import com.hissp.distribution.module.mb.domain.service.trade.MbOrderService;

import jakarta.annotation.Resource;

/**
 * MB 订单 API 实现
 *
 * @author codex
 */
@Service
@Validated
public class MbOrderApiImpl implements MbOrderApi {

    @Resource
    private MbOrderService mbOrderService;

    @Override
    public MbOrderSimpleRespDTO getOrderSummary(Long orderId) {
        // 业务含义：没有订单编号就没有可查数据，此处直接返回 null
        if (orderId == null) {
            return null;
        }
        // 先获取后台复用的详情 VO，再转换为跨模块传输 DTO，保持展示一致
        MbOrderDetailRespVO detail = mbOrderService.getAdminOrderDetail(orderId);
        return convert(detail);
    }

    private MbOrderSimpleRespDTO convert(MbOrderDetailRespVO detail) {
        if (detail == null) {
            return null;
        }
        MbOrderSimpleRespDTO dto = new MbOrderSimpleRespDTO();
        dto.setId(detail.getId());
        dto.setOrderNo(detail.getOrderNo());
        dto.setProductId(detail.getProductId());
        dto.setProductName(detail.getProductName());
        dto.setProductImage(detail.getProductImage());
        dto.setBizType(detail.getBizType());
        dto.setBizTypeName(detail.getBizTypeName());
        dto.setStatus(detail.getStatus());
        dto.setStatusName(detail.getStatusName());
        dto.setQuantity(detail.getQuantity());
        dto.setUnitPrice(detail.getUnitPrice());
        dto.setUnitPriceDisplay(detail.getUnitPriceDisplay());
        dto.setTotalPrice(detail.getTotalPrice());
        dto.setTotalPriceDisplay(detail.getTotalPriceDisplay());
        dto.setPaymentId(detail.getPaymentId());
        dto.setPayChannelCode(detail.getPayChannelCode());
        dto.setPayStatus(detail.getPayStatus());
        dto.setPayStatusName(detail.getPayStatusName());
        dto.setPayPrice(detail.getPayPrice());
        dto.setPayPriceDisplay(detail.getPayPriceDisplay());
        dto.setPaySuccessTime(detail.getPaySuccessTime());
        dto.setAgentUserId(detail.getAgentUserId());
        dto.setAgentUserNickname(detail.getAgentUserNickname());
        dto.setAgentUserMobile(detail.getAgentUserMobile());
        dto.setCanRetryVirtualDelivery(detail.getCanRetryVirtualDelivery());
        dto.setCanReceive(detail.getCanReceive());
        dto.setCreateTime(detail.getCreateTime());
        dto.setUpdateTime(detail.getUpdateTime());
        dto.setDeliveryTime(detail.getDeliveryTime());
        dto.setReceiveTime(detail.getReceiveTime());
        return dto;
    }
}
