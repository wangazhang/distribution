package com.hissp.distribution.module.pay.api.transfer;

import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferCreateReqDTO;
import com.hissp.distribution.module.pay.api.transfer.dto.PayTransferRespDTO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.transfer.PayTransferDO;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.transfer.PayTransferService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 转账单 API 实现类
 *
 * @author jason
 */
@Service
@Validated
public class PayTransferApiImpl implements PayTransferApi {

    @Resource
    private PayTransferService payTransferService;
    @Resource
    private PayAppService payAppService;

    @Override
    public Long createTransfer(PayTransferCreateReqDTO reqDTO) {
        return payTransferService.createTransfer(reqDTO);
    }

    @Override
    public PayTransferRespDTO getTransfer(Long id) {
        PayTransferDO transfer = payTransferService.getTransfer(id);
        return BeanUtils.toBean(transfer, PayTransferRespDTO.class);
    }

    @Override
    public PayTransferRespDTO getTransferByMerchantTransferId(String appKey, String merchantTransferId) {
        // 校验应用有效后，再根据 appId + 业务转账单号查询，保证多租户/多应用隔离
        PayAppDO app = payAppService.validPayApp(appKey);
        PayTransferDO transfer = payTransferService.getTransferByAppIdAndMerchantTransferId(app.getId(), merchantTransferId);
        return BeanUtils.toBean(transfer, PayTransferRespDTO.class);
    }

    @Override
    public Boolean syncTransfer(Long id) {
        // 返回 true/false 让调用方知道是否触发成功，内部失败会抛异常
        return payTransferService.syncTransfer(id);
    }

}
