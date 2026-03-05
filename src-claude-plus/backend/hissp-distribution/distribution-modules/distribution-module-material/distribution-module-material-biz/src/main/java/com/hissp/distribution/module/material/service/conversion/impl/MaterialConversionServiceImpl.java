package com.hissp.distribution.module.material.service.conversion.impl;

import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.service.conversion.MaterialConversionService;
import com.hissp.distribution.module.material.service.conversion.MaterialConverter;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class MaterialConversionServiceImpl implements MaterialConversionService {

    @Autowired(required = false)
    private List<MaterialConverter> converters;

    @Override
    public List<MaterialActDTO> convert(TradeOrderRespDTO order, TradeOrderItemRespDTO item, MaterialActDirectionEnum direction) {
        if (converters == null || converters.isEmpty()) {
            log.warn("[convert][No material converters found]");
            return Collections.emptyList();
        }

        for (MaterialConverter converter : converters) {
            if (converter.supports(item)) {
                return converter.convert(order, item, direction);
            }
        }

        log.debug("[convert][No suitable converter for orderItemId={}]", item.getId());
        return Collections.emptyList();
    }
}

