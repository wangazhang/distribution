package com.hissp.distribution.module.trade.api.config;

import com.hissp.distribution.module.trade.api.config.dto.TradeConfigRespDTO;

/**
 * 交易中心配置 API
 *
 * <p>对外暴露统一的交易配置读取能力，避免业务模块直接依赖底层 Service。</p>
 */
public interface TradeConfigApi {

    /**
     * 获取当前生效的交易配置
     *
     * @return 交易配置
     */
    TradeConfigRespDTO getTradeConfig();

}

