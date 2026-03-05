package com.hissp.distribution.module.pay.service.transfer;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.pay.core.client.dto.transfer.PayTransferInnerRespDTO;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.transfer.vo.PayTransferPageReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.transfer.PayTransferDO;

import jakarta.validation.Valid;

/**
 * 转账 Service 接口
 *
 * @author jason
 */
public interface PayTransferService {

    /**
     * 创建转账单，并发起转账
     *
     * 此时，会发起转账渠道的调用
     *
     * @param reqVO 请求
     * @param userIp 用户 ip
     * @return 渠道的返回结果
     */
    PayTransferDO createTransfer(@Valid PayTransferCreateReqVO reqVO, String userIp);

    /**
     * 创建转账单，并发起转账
     *
     * @param reqDTO 创建请求
     * @return 转账单编号
     */
    Long createTransfer(@Valid PayTransferCreateReqDTO reqDTO);

    /**
     * 获取转账单
     * @param id 转账单编号
     */
    PayTransferDO getTransfer(Long id);

    /**
     * 根据应用与商户转账单号获取转账单
     *
     * @param appId 应用编号
     * @param merchantTransferId 业务侧转账单号
     * @return 转账单
     */
    PayTransferDO getTransferByAppIdAndMerchantTransferId(Long appId, String merchantTransferId);

    /**
     * 获得转账单分页
     *
     * @param pageReqVO 分页查询
     * @return 转账单分页
     */
    PageResult<PayTransferDO> getTransferPage(PayTransferPageReqVO pageReqVO);

    /**
     * 同步渠道转账单状态
     *
     * @return 同步到状态的转账数量，包括转账成功、转账失败、转账中的
     */
    int syncTransfer();

    /**
     * 同步指定转账单
     *
     * @param id 转账单编号
     * @return 是否同步成功
     */
    boolean syncTransfer(Long id);

    /**
     * 渠道的转账通知
     *
     * @param channelId  渠道编号
     * @param notify     通知
     */
    void notifyTransfer(Long channelId, PayTransferInnerRespDTO notify);

}
