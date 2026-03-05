package com.hissp.distribution.module.trade.convert.config;

import com.hissp.distribution.module.trade.controller.admin.config.vo.TradeConfigRespVO;
import com.hissp.distribution.module.trade.controller.admin.config.vo.TradeConfigSaveReqVO;
import com.hissp.distribution.module.trade.controller.app.config.vo.AppTradeConfigRespVO;
import com.hissp.distribution.module.trade.dal.dataobject.config.TradeConfigDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 交易中心配置 Convert
 *
 * @author owen
 */
@Mapper
public interface TradeConfigConvert {

    TradeConfigConvert INSTANCE = Mappers.getMapper(TradeConfigConvert.class);

    TradeConfigDO convert(TradeConfigSaveReqVO bean);

    TradeConfigRespVO convert(TradeConfigDO bean);

    AppTradeConfigRespVO convert02(TradeConfigDO tradeConfig);
}
