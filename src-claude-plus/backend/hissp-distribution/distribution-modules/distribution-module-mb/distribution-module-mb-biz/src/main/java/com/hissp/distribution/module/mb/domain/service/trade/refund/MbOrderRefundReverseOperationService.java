package com.hissp.distribution.module.mb.domain.service.trade.refund;

import com.hissp.distribution.module.pay.api.notify.dto.PayRefundNotifyReqDTO;

/**
 * MB业务退款逆操作服务接口
 * 集成了退款类型识别、路由处理、记录管理和逆操作执行的完整功能
 * 处理MB业务相关的退款成功后的逆操作，包括物料回退、分佣取消等
 *
 * @author system
 */
public interface MbOrderRefundReverseOperationService {

    /**
     * 处理退款通知（主入口方法）
     * 集成了类型识别、记录管理、路由处理和逆操作执行
     *
     * @param notifyReqDTO 退款通知DTO
     * @return 处理结果
     */
    boolean processRefundNotification(PayRefundNotifyReqDTO notifyReqDTO);

}
