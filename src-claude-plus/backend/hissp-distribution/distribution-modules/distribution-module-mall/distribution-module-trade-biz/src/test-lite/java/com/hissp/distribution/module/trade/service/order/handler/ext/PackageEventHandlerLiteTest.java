package com.hissp.distribution.module.trade.service.order.handler.ext;

import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.service.conversion.MaterialConversionService;
import com.hissp.distribution.module.product.api.packagex.ProductPackageApi;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderDO;
import com.hissp.distribution.module.trade.dal.dataobject.order.TradeOrderItemDO;
import com.hissp.distribution.module.trade.service.packagex.PackageCommissionService;
import com.hissp.distribution.module.trade.service.packagex.PackageGrantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageEventHandlerLiteTest {

    @Mock private ProductPackageApi productPackageApi;
    @Mock private PackageGrantService packageGrantService;
    @Mock private PackageCommissionService packageCommissionService;
    @Mock private MaterialConversionService materialConversionService;
    @Mock private MaterialApi materialApi;

    @InjectMocks private PackageEventHandler handler;

    private TradeOrderDO order;
    private TradeOrderItemDO item;
    private ProductPackageRespDTO pack;

    @BeforeEach
    void init() {
        order = TradeOrderDO.builder().id(2001L).userId(3001L).no("NO-2001").build();
        item = new TradeOrderItemDO();
        item.setId(1001L);
        item.setUserId(3001L);
        item.setOrderId(2001L);
        item.setSpuId(4001L);
        item.setCount(1);

        pack = new ProductPackageRespDTO();
    }

    @Test
    void onPayment_invokesMaterialGrantAndCommission() {
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        when(materialConversionService.convert(any(), any(), eq(MaterialActDirectionEnum.IN)))
                .thenReturn(List.of());

        handler.onPayment(order, item);

        verify(materialConversionService, times(1))
                .convert(any(), any(), eq(MaterialActDirectionEnum.IN));
        verify(materialApi, times(1)).applyActs(any());
        verify(packageGrantService, times(1)).grantOnPaid(order, item, pack);
        verify(packageCommissionService, times(1)).commissionOnPaid(order, item, pack);
    }

    @Test
    void onRefund_invokesMaterialRevokeAndCancelCommission() {
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        when(materialConversionService.convert(any(), any(), eq(MaterialActDirectionEnum.OUT)))
                .thenReturn(List.of(new MaterialActDTO()));

        handler.onRefund(order, item);

        verify(materialConversionService, times(1))
                .convert(any(), any(), eq(MaterialActDirectionEnum.OUT));
        verify(materialApi, times(1)).applyActs(any());
        verify(packageGrantService, times(1)).revokeOnCancel(order, item, pack);
        verify(packageCommissionService, times(1)).cancelCommission(order, item, pack);
    }

    @Test
    void supports_true_whenPackageExists() {
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(pack);
        boolean supported = handler.supports(item);
        assert(supported);
    }

    @Test
    void supports_false_whenNoPackage() {
        when(productPackageApi.getEnabledPackageBySpuId(4001L)).thenReturn(null);
        boolean supported = handler.supports(item);
        assert(!supported);
    }
}

