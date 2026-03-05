package com.hissp.distribution.module.pay.service.settle;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountPageReqVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountChangeCardReqVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountSaveReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import com.hissp.distribution.module.pay.enums.payease.PayeaseSubMerchantStatusEnum;

import java.util.Map;

public interface PaySettleAccountService {

    PaySettleAccountDO get(Long id);

    PaySettleAccountDO getByUser(Long userId);

    PageResult<PaySettleAccountDO> getPage(SettleAccountPageReqVO pageReqVO);

    Long saveDraft(Long userId, AppSettleAccountSaveReqVO reqVO);

    void submit(Long userId);

    void adminSubmit(Long id);

    Long saveChangeCardDraft(Long userId, AppSettleAccountChangeCardReqVO reqVO);

    void submitChangeCard(Long userId);

    void syncStatus(Long id);

    PaySettleAccountDO getApprovedAccount(Long userId);

    /**
     * 处理首信易入网回调
     *
     * @param channelId 渠道编号
     * @param headers   回调请求头
     * @param params    回调查询参数
     * @param body      回调请求体
     */
    void handleDeclarationNotify(Long channelId, Map<String, String> headers,
                                  Map<String, String> params, String body);

    void recordStatus(Long id, PayeaseSubMerchantStatusEnum status,
                      String rejectReason, String subMerchantId, String requestId);

    void clearProblemRecord(Long id);
}
