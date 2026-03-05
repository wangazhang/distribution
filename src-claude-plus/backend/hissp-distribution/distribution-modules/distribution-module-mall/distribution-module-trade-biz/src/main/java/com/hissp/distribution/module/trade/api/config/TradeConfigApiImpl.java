package com.hissp.distribution.module.trade.api.config;

import com.hissp.distribution.module.trade.api.config.dto.TradeConfigRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.config.TradeConfigDO;
import com.hissp.distribution.module.trade.service.config.TradeConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 交易配置 API 实现。
 */
@Service
@Validated
public class TradeConfigApiImpl implements TradeConfigApi {

    @Resource
    private TradeConfigService tradeConfigService;

    /**
     * 将 DO 转换为 DTO，避免对外暴露内部结构。
     */
    private TradeConfigRespDTO convert(TradeConfigDO configDO) {
        if (configDO == null) {
            return null;
        }
        TradeConfigRespDTO respDTO = new TradeConfigRespDTO();
        respDTO.setBrokerageFrozenDays(configDO.getBrokerageFrozenDays());
        return respDTO;
    }

    @Override
    public TradeConfigRespDTO getTradeConfig() {
        TradeConfigDO configDO = tradeConfigService.getTradeConfig();
        return convert(configDO);
    }
}
