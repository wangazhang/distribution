package com.hissp.distribution.module.trade.api.brokerage;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageRecordConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 分佣记录 API 接口实现类
 *
 * @author system
 */
@Service
@Validated
public class BrokerageRecordApiImpl implements BrokerageRecordApi {

    @Resource
    private BrokerageRecordService brokerageRecordService;

    @Override
    public boolean addBrokerageBatch(List<BrokerageRecordCreateReqDTO> records) {
        if (records == null || records.isEmpty()) {
            return false;
        }

        // 将DTO转换为DO
        List<BrokerageRecordDO> brokerageRecords = new ArrayList<>(records.size());
        for (BrokerageRecordCreateReqDTO recordDTO : records) {
            brokerageRecords.add(BrokerageRecordConvert.INSTANCE.convert(recordDTO));
        }

        // 调用Service批量添加
        brokerageRecordService.addBrokerageBatch(brokerageRecords);
        return true;
    }

    @Override
    public void cancelBrokerage(BrokerageRecordBizTypeEnum bizType, String bizId) {
        brokerageRecordService.cancelBrokerage(bizType, bizId);
    }

    @Override
    public void cancelBrokerage(String bizId) {
        brokerageRecordService.cancelBrokerage(bizId);
    }

    @Override
    public void scheduleWaitSettlementByBizIds(Collection<String> bizIds, Integer frozenDays, LocalDateTime unfreezeTime) {
        brokerageRecordService.scheduleWaitSettlementByBizIds(bizIds, frozenDays, unfreezeTime);
    }

}
