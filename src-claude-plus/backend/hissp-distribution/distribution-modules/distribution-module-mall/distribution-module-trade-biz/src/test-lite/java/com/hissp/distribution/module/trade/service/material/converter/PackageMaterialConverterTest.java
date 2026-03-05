package com.hissp.distribution.module.trade.service.material.converter;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderItemRespDTO;
import com.hissp.distribution.module.trade.api.order.dto.TradeOrderRespDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PackageMaterialConverterLiteTest {

    @Mock
    private ProductPackageApi productPackageApi;
    @Mock
    private MaterialApi materialApi;

    @InjectMocks
    private PackageMaterialConverter converter;

    private TradeOrderRespDTO order;
    private TradeOrderItemRespDTO item;

    @BeforeEach
    void setUp() {
        order = new TradeOrderRespDTO();
        order.setId(2001L);
        order.setUserId(3001L);
        order.setNo("NO-2001");

        item = new TradeOrderItemRespDTO();
        item.setId(1001L);
        item.setUserId(3001L);
        item.setOrderId(2001L);
        item.setSpuId(4001L);
        item.setCount(1);
    }

    @Test
    void supports_returnsTrue_whenPackageExists() {
        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        assertTrue(converter.supports(item));
    }

    @Test
    void supports_returnsFalse_whenNoPackage() {
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(null);
        assertFalse(converter.supports(item));
    }

    @Test
    void convert_buildsActs_forMaterialItems_only() {
        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        List<ProductPackageRespDTO.Item> items = new ArrayList<>();

        ProductPackageRespDTO.Item material1 = new ProductPackageRespDTO.Item();
        material1.setItemType(1);
        material1.setItemId("501");
        material1.setItemQuantity(2);
        items.add(material1);
        MaterialDefinitionRespDTO definition1 = new MaterialDefinitionRespDTO();
        definition1.setId(9001L);
        definition1.setStatus(1);

        ProductPackageRespDTO.Item nonMaterial = new ProductPackageRespDTO.Item();
        nonMaterial.setItemType(2);
        nonMaterial.setItemId("RIGHT_OPEN");
        items.add(nonMaterial);

        pack.setItems(items);
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        when(materialApi.getDefinitionBySpuId(501L)).thenReturn(definition1);

        List<MaterialActDTO> acts = converter.convert(order, item, MaterialActDirectionEnum.IN);

        assertEquals(1, acts.size());
        MaterialActDTO act = acts.get(0);
        assertEquals(order.getUserId(), act.getUserId());
        assertEquals(9001L, act.getMaterialId());
        assertEquals(2, act.getQuantity());
        assertEquals(MaterialActDirectionEnum.IN, act.getDirection());
        assertEquals("1001_9001", act.getBizKey());
        assertEquals("PACKAGE_GRANT", act.getBizType());
        assertEquals("套包发放", act.getReason());
    }

    @Test
    void convert_ignores_nonNumericMaterialId_but_keepsBizKeyFormattingStringSafe() {
        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        List<ProductPackageRespDTO.Item> items = new ArrayList<>();

        ProductPackageRespDTO.Item materialWithTextId = new ProductPackageRespDTO.Item();
        materialWithTextId.setItemType(1);
        materialWithTextId.setItemId("OPEN_BROKERAGE");
        items.add(materialWithTextId);

        pack.setItems(items);
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);

        List<MaterialActDTO> acts = converter.convert(order, item, MaterialActDirectionEnum.IN);

        assertTrue(acts.isEmpty());
    }

    @Test
    void convert_defaultsQuantityToOne_whenNull() {
        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        List<ProductPackageRespDTO.Item> items = new ArrayList<>();

        ProductPackageRespDTO.Item material = new ProductPackageRespDTO.Item();
        material.setItemType(1);
        material.setItemId("777");
        material.setItemQuantity(null);
        items.add(material);

        pack.setItems(items);
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        MaterialDefinitionRespDTO definition = new MaterialDefinitionRespDTO();
        definition.setId(7007L);
        definition.setStatus(1);
        when(materialApi.getDefinitionBySpuId(777L)).thenReturn(definition);

        List<MaterialActDTO> acts = converter.convert(order, item, MaterialActDirectionEnum.IN);

        assertEquals(1, acts.size());
        assertEquals(1, acts.get(0).getQuantity());
    }

    @Test
    void convert_setsOutDirection_fields_whenRefund() {
        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        List<ProductPackageRespDTO.Item> items = new ArrayList<>();

        ProductPackageRespDTO.Item material = new ProductPackageRespDTO.Item();
        material.setItemType(1);
        material.setItemId("888");
        material.setItemQuantity(3);
        items.add(material);

        pack.setItems(items);
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        MaterialDefinitionRespDTO definition = new MaterialDefinitionRespDTO();
        definition.setId(8080L);
        definition.setStatus(1);
        when(materialApi.getDefinitionBySpuId(888L)).thenReturn(definition);

        List<MaterialActDTO> acts = converter.convert(order, item, MaterialActDirectionEnum.OUT);

        assertEquals(1, acts.size());
        MaterialActDTO act = acts.get(0);
        assertEquals(MaterialActDirectionEnum.OUT, act.getDirection());
        assertEquals("PACKAGE_REVOKE", act.getBizType());
        assertEquals("套包退款回退", act.getReason());
    }

    @Test
    void convert_returnsEmpty_whenNoPackOrEmptyItems() {
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(null);
        List<MaterialActDTO> acts1 = converter.convert(order, item, MaterialActDirectionEnum.IN);
        assertTrue(acts1.isEmpty());

        ProductPackageRespDTO pack = new ProductPackageRespDTO();
        pack.setItems(new ArrayList<>());
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        List<MaterialActDTO> acts2 = converter.convert(order, item, MaterialActDirectionEnum.IN);
        assertTrue(acts2.isEmpty());
    }
}
