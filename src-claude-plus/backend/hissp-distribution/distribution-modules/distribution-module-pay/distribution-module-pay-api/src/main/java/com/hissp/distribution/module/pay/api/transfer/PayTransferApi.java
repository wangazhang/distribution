package com.hissp.distribution.module.pay.api.transfer;

import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferCreateReqDTO;

import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferRespDTO;
import jakarta.validation.Valid;

/**
 * 转账单 API 接口
 *
 * @author jason
 */
public interface PayTransferApi {

    /**
     * 创建转账单
     *
     * @param reqDTO 创建请求
     * @return 转账单编号
     */
    Long createTransfer(@Valid PayTransferCreateReqDTO reqDTO);

    /**
     * 获得转账单
     *
     * @param id 转账单编号
     * @return 转账单
     */
    PayTransferRespDTO getTransfer(Long id);

    /**
     * 根据应用和业务单号获取转账单
     *
     * @param appKey 应用标识
     * @param merchantTransferId 业务侧转账单号
     */
    PayTransferRespDTO getTransferByMerchantTransferId(String appKey, String merchantTransferId);

    /**
     * 触发单笔转账同步
     *
     * @param id 转账单编号
     * @return 是否同步成功
     */
    Boolean syncTransfer(Long id);

}
