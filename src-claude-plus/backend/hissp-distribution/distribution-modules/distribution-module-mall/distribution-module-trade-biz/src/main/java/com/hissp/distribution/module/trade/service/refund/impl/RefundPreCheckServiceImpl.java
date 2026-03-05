package com.hissp.distribution.module.trade.service.refund.impl;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.service.conversion.MaterialConversionService;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.enums.ErrorCodeConstants;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.service.refund.RefundPreCheckService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 退款前资源充足性校验（使用统一物料域）
 * 1) 先判断佣金是否已结算
 * 2) 通过物料转化器转换出回退的物料动作，调用 materialApi.isRevocable 校验资源充足
 * 3) 权益回收策略（默认不回收，可配置开启）
 */
@Slf4j
@Service
public class RefundPreCheckServiceImpl implements RefundPreCheckService {

    @Value("${refund.precheck.enabled:false}")
    private boolean precheckEnabled;
    @Value("${refund.precheck.entitlement.recovery.enabled:false}")
    private boolean entitlementRecoveryEnabled;

    @Resource
    private ProductPackageApi productPackageApi;
    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private MaterialConversionService materialConversionService;
    @Resource
    private MaterialApi materialApi;

    @Override
    public void validate(TradeOrderDO order, List<TradeOrderItemDO> items) {
        if (!precheckEnabled) {
            log.info("[RefundPreCheck][disabled=true, skip validate orderId={}]", order.getId());
            return;
        }
        if (items == null || items.isEmpty()) {
            return;
        }
        // 适配 DTO
        TradeOrderRespDTO orderDTO = new TradeOrderRespDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUserId());
        orderDTO.setNo(order.getNo());

        for (TradeOrderItemDO item : items) {
            ProductPackageRespDTO pack = productPackageApi.getEnabledPackageBySpuId(item.getSpuId());
            if (pack == null) {
                // 普通商品暂不校验（可扩展 Converter 后统一处理）
                log.debug("[RefundPreCheck][orderId={} itemId={}] 普通商品，无资源校验", order.getId(), item.getId());
                continue;
            }

            // 1) 佣金不得已结算（存在已结算佣金则阻断自动退款）
            boolean commissionSettled = brokerageRecordService.existsSettledByBizId(String.valueOf(item.getId()));
            if (commissionSettled) {
                throw exception(ErrorCodeConstants.AFTER_SALE_REFUND_FAIL_COMMISSION_ALREADY_SETTLED);
            }

            // 2) 物料回退资源校验（统一通过物料域校验）
            TradeOrderItemRespDTO itemDTO = new TradeOrderItemRespDTO();
            itemDTO.setId(item.getId());
            itemDTO.setUserId(item.getUserId());
            itemDTO.setOrderId(item.getOrderId());
            itemDTO.setSpuId(item.getSpuId());
            itemDTO.setCount(item.getCount());
            List<MaterialActDTO> acts = materialConversionService.convert(orderDTO, itemDTO, MaterialActDirectionEnum.OUT);
            boolean ok = materialApi.isRevocable(acts);
            if (!ok) {
                throw exception(ErrorCodeConstants.AFTER_SALE_REFUND_FAIL_RESOURCE_NOT_ENOUGH);
            }

            // 3) 权益回收策略（默认不自动回收；若开启回收，需要进一步校验回收安全性）
            if (entitlementRecoveryEnabled) {
                log.info("[RefundPreCheck][orderId={} itemId={}] 权益回收策略已开启，但尚未实现具体回收校验，默认放行", order.getId(), item.getId());
                // TODO: 按策略校验权益可回收性（等级降级安全、无依赖影响等）
            }
        }
    }
}

