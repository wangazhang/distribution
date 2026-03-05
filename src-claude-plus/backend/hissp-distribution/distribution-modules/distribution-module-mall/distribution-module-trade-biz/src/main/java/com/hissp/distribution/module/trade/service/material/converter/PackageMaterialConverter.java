package com.hissp.distribution.module.trade.service.material.converter;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.service.conversion.MaterialConverter;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.product.enums.ErrorCodeConstants;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;

@Slf4j
@Component
public class PackageMaterialConverter implements MaterialConverter {

    @Resource
    private ProductPackageApi productPackageApi;
    @Resource
    private MaterialApi materialApi;

    @Override
    public boolean supports(TradeOrderItemRespDTO item) {
        return productPackageApi.getEnabledPackageBySpuId(item.getSpuId()) != null;
    }

    @Override
    public List<MaterialActDTO> convert(TradeOrderRespDTO order, TradeOrderItemRespDTO item, MaterialActDirectionEnum direction) {
        ProductPackageRespDTO pack = productPackageApi.getEnabledPackageBySpuId(item.getSpuId());
        if (pack == null || pack.getItems() == null || pack.getItems().isEmpty()) {
            return Collections.emptyList();
        }

        List<MaterialActDTO> acts = new ArrayList<>();
        for (ProductPackageRespDTO.Item packageItem : pack.getItems()) {
            // Only handle material items (type 1)
            if (!Objects.equals(packageItem.getItemType(), 1)) {
                continue;
            }

            try {
                Long spuId = Long.valueOf(packageItem.getItemId());
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(spuId);
                if (definition == null) {
                    throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_MATERIAL_DEFINITION_NOT_EXISTS, spuId);
                }
                if (!Objects.equals(definition.getStatus(), 1)) {
                    throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_MATERIAL_DEFINITION_DISABLED, spuId);
                }

                Long materialId = definition.getId();
                MaterialActDTO act = MaterialActDTO.builder()
                        .userId(order.getUserId())
                        .materialId(materialId)
                        .quantity(packageItem.getItemQuantity() != null ? packageItem.getItemQuantity() : 1)
                        .direction(direction)
                        .bizKey(String.format("%d_%d", item.getId(), materialId))
                        .bizType(direction == MaterialActDirectionEnum.IN ? "PACKAGE_GRANT" : "PACKAGE_REVOKE")
                        .reason(direction == MaterialActDirectionEnum.IN ? "套包发放" : "套包退款回退")
                        .build();
                acts.add(act);
            } catch (NumberFormatException e) {
                log.warn("[convert][packageId={} itemId={}] 商品项无法解析为数字，跳过发放", pack.getId(), packageItem.getItemId());
            }
        }
        return acts;
    }
}
