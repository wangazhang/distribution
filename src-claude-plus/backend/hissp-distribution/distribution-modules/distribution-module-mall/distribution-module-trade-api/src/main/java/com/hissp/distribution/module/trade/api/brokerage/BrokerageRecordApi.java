package com.hissp.distribution.module.trade.api.brokerage;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 分佣记录 API 接口
 *
 * @author system
 */
public interface BrokerageRecordApi {

    /**
     * 批量添加分佣记录；根据冻结天数，判断是否更新余额；
     *
     * @param records 分佣记录列表
     * @return 添加结果
     */
    boolean addBrokerageBatch(List<BrokerageRecordCreateReqDTO> records);

    /**
     * 取消佣金：将佣金记录，状态修改为已失效
     *
     * @param bizType 业务类型
     * @param bizId   业务编号
     */
    void cancelBrokerage(BrokerageRecordBizTypeEnum bizType, String bizId);

    /**
     * 取消佣金：根据业务编号查找并取消所有相关的分佣记录
     *
     * @param bizId 业务编号
     */
    void cancelBrokerage(String bizId);

    /**
     * 为指定业务编号的待结算佣金记录补齐冻结信息
     *
     * @param bizIds       业务编号集合
     * @param frozenDays   冻结天数，小于等于 0 表示立即解冻
     * @param unfreezeTime 解冻时间；为空时会使用 <code>LocalDateTime.now().plusDays(frozenDays)</code>
     */
    void scheduleWaitSettlementByBizIds(Collection<String> bizIds, Integer frozenDays, LocalDateTime unfreezeTime);

}
