package com.hissp.distribution.module.mb.domain.service.mbdt;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.mb.constants.MbConstants;
import com.hissp.distribution.module.mb.dal.dataobject.trade.MbOrderDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler.MaterialConvertOrderHandler;
import com.hissp.distribution.module.mb.domain.service.mbdt.commission.handler.MaterialRestockOrderHandler;
import com.hissp.distribution.module.mb.domain.service.trade.MbOrderService;
import com.hissp.distribution.module.mb.enums.ErrorCodeConstants;
import com.hissp.distribution.module.mb.enums.MbOrderStatusEnum;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelReqDTO;
import com.hissp.distribution.module.member.event.MemberLevelChangeEvent;
import com.hissp.distribution.module.member.event.SubCompanyUpgradeEvent;
import com.hissp.distribution.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageRecordApi;
import com.hissp.distribution.module.trade.api.brokerage.BrokerageUserApi;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserCreateOrUpdateDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.api.packagex.PackageGrantApi;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class MbBizOperationServiceImpl implements MbBizOperationService {

    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private BrokerageUserApi brokerageUserApi;
    @Resource
    private MaterialApi materialApi;
    @Resource
    private BrokerageRecordApi brokerageRecordApi;
    @Resource
    private PackageGrantApi packageGrantApi;
    @Resource
    private MbBizOperationService mbBizOperationService;

    @Resource
    private MaterialRestockOrderHandler materialRestockOrderHandler;

    @Resource
    private MaterialConvertOrderHandler materialConvertOrderHandler;

    @Resource
    private MbOrderService mbOrderService;

    @Override
    public void handleRestockPaymentSuccess(PayOrderNotifyReqDTO message) {
        // 解析出物料订单ID
        String materialOrderIdStr = message.getMerchantOrderId().replace("MR", "");
        try {
            Long materialOrderId = Long.parseLong(materialOrderIdStr);
            boolean processed = false;
            if (paymentAlreadyHandled(materialOrderId)) {
                processed = true;
                log.debug("[handleRestockPaymentSuccess][orderId={}] 回调已处理过支付阶段，跳过重复执行]", materialOrderId);
            } else {
                // 处理物料订单支付成功 - 增加库存、添加变更记录和处理分佣
                processed = mbBizOperationService.handleAfterRestockPaymentSuccess(materialOrderId);
                if (!processed) {
                    log.warn("[handleRestockPaymentSuccess][物料补货订单支付成功后处理失败：materialOrderId:{}]", materialOrderId);
                } else {
                    log.info("[handleRestockPaymentSuccess][物料补货订单支付成功，推动后续库存与分佣：orderId={}]", materialOrderId);
                }
            }
            if (processed) {
                mbOrderService.processOrderPaidNotify(materialOrderId, message.getPayOrderId(), message.getDeliveryStatus());
            }
        } catch (NumberFormatException e) {
            log.error("[onMessage][解析物料订单ID失败：{}]", materialOrderIdStr, e);
        }

    }


    @Override
    public void handleSubCompanyUpgradeListener(SubCompanyUpgradeEvent event) {
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleMaterialConvertPaymentSuccess(PayOrderNotifyReqDTO message) {
        // 解析出物料转化订单ID
        String convertOrderIdStr = message.getMerchantOrderId().replace("MC", "");
        try {
            Long convertOrderId = Long.parseLong(convertOrderIdStr);
            log.info("[handleMaterialConvertPaymentSuccess][开始处理物料转化订单支付成功：{}]", convertOrderId);

            boolean processed = false;
            if (paymentAlreadyHandled(convertOrderId)) {
                processed = true;
                log.debug("[handleMaterialConvertPaymentSuccess][orderId={}] 回调已处理过支付阶段，跳过重复执行]", convertOrderId);
            } else {
                processed = mbBizOperationService.handleAfterMaterialConvertPaymentSuccess(convertOrderId);
                if (!processed) {
                    log.warn("[handleMaterialConvertPaymentSuccess][处理物料转化订单支付失败：{}]", convertOrderId);
                } else {
                    log.info("[handleMaterialConvertPaymentSuccess][处理物料转化订单支付成功，执行了库存流转：orderId:{}]",
                            convertOrderId);
                }
            }
            if (processed) {
                mbOrderService.processOrderPaidNotify(convertOrderId, message.getPayOrderId(), message.getDeliveryStatus());
            }
        } catch (NumberFormatException e) {
            log.error("[handleMaterialConvertPaymentSuccess][解析物料转化订单ID失败：{}]", convertOrderIdStr, e);
        } catch (Exception e) {
            log.error("[handleMaterialConvertPaymentSuccess][处理物料转化订单异常：{}]", convertOrderIdStr, e);
        }
    }

    @Override
    public void handleManualMemberLevelChange(MemberLevelChangeEvent event) {
        boolean enableBrokerage = false;
        // 如果是成为分销者等级，则创建分销用户，并且开启分销能力；
        // 处理所变等级的初始化权益

        // 如果前后等级没有变化，则不处理
        if (Objects.equals(event.getBeforeLevelId(), event.getAfterLevelId())) {
            grantPackageIfNeeded(event);
            return;
        }

        // 如果新等级是游客或VIP级别，同步等级+关闭用户分销功能
        if (Objects.equals(event.getAfterLevelId(), MbConstants.MbTouristLevelInfo.MB_TOURIST_LEVEL_ID)
            || Objects.equals(event.getAfterLevelId(), MbConstants.MbVIPLevelInfo.MB_VIP_LEVEL_ID)) {
            enableBrokerage = false;
            BrokerageUserCreateOrUpdateDTO brokerageUserCreateOrUpdateDTO = new BrokerageUserCreateOrUpdateDTO();
            brokerageUserCreateOrUpdateDTO.setId(event.getUserId())
                // .setBindUserId()
                // .setBindUserTime()
                .setBrokerageEnabled(enableBrokerage)
                // .setBrokerageTime()
                // .setBrokeragePrice()
                // .setFrozenPrice()
                // .setTotalBrokeragePrice()
                .setLevelId(event.getAfterLevelId());
            brokerageUserApi.createOrUpdateBrokerageUser(brokerageUserCreateOrUpdateDTO);
            grantPackageIfNeeded(event);
            return;
        }
        // 如果新等级是SVIP级别，则开启用户分销功能
        if (Objects.equals(event.getAfterLevelId(), MbConstants.MbSVIPLevelInfo.MB_SVIP_LEVEL_ID)
            || Objects.equals(event.getAfterLevelId(), MbConstants.MbBOSSLevelInfo.MB_SUB_COM_LEVEL_ID)) {
            enableBrokerage = true;
            BrokerageUserCreateOrUpdateDTO brokerageUserCreateOrUpdateDTO = new BrokerageUserCreateOrUpdateDTO();
            brokerageUserCreateOrUpdateDTO.setId(event.getUserId())
                // .setBindUserId(0L) //人工开启的置为 0
                // .setBindUserTime()
                .setBrokerageEnabled(enableBrokerage).setBrokerageTime(LocalDateTime.now())
                // .setBrokeragePrice()
                // .setFrozenPrice()
                // .setTotalBrokeragePrice()
                .setLevelId(event.getAfterLevelId());
            brokerageUserApi.createOrUpdateBrokerageUser(brokerageUserCreateOrUpdateDTO);
            // brokerageUserApi.updateBrokerageUserEnabled(updateReqVO.getId(), true);
        }

        // 如果新等级是子公司级别，且比之前等级高，且开启分佣
        Integer beforeLevel = event.getBeforeLevel() == null ? 0 : event.getBeforeLevel();
        if (Objects.equals(event.getAfterLevelId(), MbConstants.MbBOSSLevelInfo.MB_SUB_COM_LEVEL_ID)
            && MbConstants.MbBOSSLevelInfo.MB_SUB_COM_LEVEL > beforeLevel
            && event.getManualEnableBrokerage()) {
            log.info("[dealWithSubCompanyUpgrade]管理员操作，开启分公司开通分佣");
            // 创建并发布子公司升级事件
            SubCompanyUpgradeEvent subCompanyUpgradeEvent = new SubCompanyUpgradeEvent();
            subCompanyUpgradeEvent.setUserId(event.getUserId()).setBeforeLevel(event.getBeforeLevel())
                .setOperatorId(event.getOperatorId()).setOperateTime(event.getOperateTime());
            handleSubCompanyUpgradeListener(subCompanyUpgradeEvent);
        }
        grantPackageIfNeeded(event);
    }

    private void grantPackageIfNeeded(MemberLevelChangeEvent event) {
        if (event.getPackageId() == null) {
            return;
        }
        String bizKey = String.format("manual-level-%s-%s",
            Objects.toString(event.getOperatorId(), "sys"),
            event.getOperateTime() != null ? event.getOperateTime().toString() : "unknown");
        packageGrantApi.grantManual(event.getUserId(), event.getPackageId(), event.getOperatorId(), bizKey);
    }

    private boolean paymentAlreadyHandled(Long orderId) {
        MbOrderDO order = mbOrderService.getMbOrder(orderId);
        if (order == null) {
            return false;
        }
        String normalizedStatus = normalizeOrderStatus(order.getStatus());
        try {
            MbOrderStatusEnum statusEnum = MbOrderStatusEnum.fromCode(normalizedStatus);
            return statusEnum != MbOrderStatusEnum.PENDING && statusEnum != MbOrderStatusEnum.FAILED;
        } catch (IllegalArgumentException ex) {
            log.warn("[paymentAlreadyHandled][orderId={}] 未知状态({})，默认执行支付处理", orderId, normalizedStatus);
            return false;
        }
    }

    private String normalizeOrderStatus(String status) {
        return StrUtil.isBlank(status) ? "" : status.trim().toUpperCase();
    }


    @Override
    public boolean handleAfterRestockPaymentSuccess(Long orderId) {
        // 委托给专门的处理器处理
        return materialRestockOrderHandler.handlePaymentSuccess(orderId);
    }

    @Override
    public boolean handleAfterMaterialConvertPaymentSuccess(Long orderId) {
        // 委托给专门的物料转化处理器处理
        return materialConvertOrderHandler.handlePaymentSuccess(orderId);
    }
}
