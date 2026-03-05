package com.hissp.distribution.module.trade.service.material.converter;

import com.hissp.distribution.framework.test.core.ut.BaseMockitoUnitTest;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PackageMaterialConverterTest extends BaseMockitoUnitTest {

    @InjectMocks
    private PackageMaterialConverter packageMaterialConverter;

    @Mock
    private ProductPackageApi productPackageApi;

    @Test
    void testConvert_Success() {
        // Arrange
        TradeOrderRespDTO order = new TradeOrderRespDTO().setId(1L).setUserId(100L);
        TradeOrderItemRespDTO item = new TradeOrderItemRespDTO().setId(10L).setSpuId(200L);

        ProductPackageRespDTO.Item packageItem = new ProductPackageRespDTO.Item();
        packageItem.setItemType(1); // 商品物料
        packageItem.setItemId("300");
        packageItem.setItemQuantity(5);
        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        pack.setItems(Collections.singletonList(packageItem));

        when(productPackageApi.getEnabledPackageBySpuId(item.getSpuId())).thenReturn(pack);

        // Act
        List<MaterialActDTO> acts = packageMaterialConverter.convert(order, item, MaterialActDirectionEnum.IN);

        // Assert
        assertNotNull(acts);
        assertEquals(1, acts.size());
        MaterialActDTO act = acts.get(0);
        assertEquals(order.getUserId(), act.getUserId());
        assertEquals(300L, act.getMaterialId());
        assertEquals(5, act.getQuantity());
        assertEquals(MaterialActDirectionEnum.IN, act.getDirection());
        assertEquals(String.format("%d_%d", item.getId(), 300L), act.getBizKey());
    }

    @Test
    void testSupports_True() {
        // Arrange
        TradeOrderItemRespDTO item = new TradeOrderItemRespDTO().setSpuId(200L);
        when(productPackageApi.getEnabledPackageBySpuId(item.getSpuId())).thenReturn(new ProductPackageRespDTO());

        // Act & Assert
        assertTrue(packageMaterialConverter.supports(item));
    }

    @Test
    void testSupports_False() {
        // Arrange
        TradeOrderItemRespDTO item = new TradeOrderItemRespDTO().setSpuId(200L);
        when(productPackageApi.getEnabledPackageBySpuId(item.getSpuId())).thenReturn(null);

        // Act & Assert
        assertFalse(packageMaterialConverter.supports(item));
    }
}

