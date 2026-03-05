package com.hissp.distribution.module.mb.domain.service.mbdt;

import com.hissp.distribution.module.member.event.MemberLevelChangeEvent;
import com.hissp.distribution.module.member.event.SubCompanyUpgradeEvent;
import com.hissp.distribution.module.pay.api.notify.dto.PayOrderNotifyReqDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;

import java.util.List;

/**
 * 事业服务
 */
public interface MbBizOperationService {

    /**
     * 处理补货支付成功的逻辑
     * 
     * @param message 支付订单通知消息
     */
    void handleRestockPaymentSuccess(PayOrderNotifyReqDTO message);
    

    /**
     * 处理子公司升级的逻辑
     * 
     * @param event 子公司升级事件
     */
    void handleSubCompanyUpgradeListener(SubCompanyUpgradeEvent event);
    
    /**
     * 处理物料转化支付成功的逻辑
     *
     * @param event 支付订单通知消息
     */
    void handleMaterialConvertPaymentSuccess(PayOrderNotifyReqDTO event);

    /**
     * 处理会员等级手动变更的逻辑
     *
     * @param event 会员等级手动变更事件
     */
    void handleManualMemberLevelChange(MemberLevelChangeEvent event);



    /**
     * 处理支付成功后的物料库存增加
     *
     * @param orderId 物料补货订单ID
     * @return 是否处理成功
     */
    boolean handleAfterRestockPaymentSuccess(Long orderId);

    /**
     * 处理物料转化支付成功后的逻辑
     * 减少原料物料，增加目标物料
     *
     * @param orderId 物料转化订单ID
     * @return 是否处理成功
     */
    boolean handleAfterMaterialConvertPaymentSuccess(Long orderId);
}
