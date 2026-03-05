package com.hissp.distribution.module.product.service.packagex.impl;

import com.hissp.distribution.module.product.api.packagex.PackageEntitlementType;
import com.hissp.distribution.module.product.api.packagex.dto.ProductPackageRespDTO;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageCommissionDO;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageDO;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageItemDO;
import com.hissp.distribution.module.product.dal.mysql.packagex.ProductPackageCommissionMapper;
import com.hissp.distribution.module.product.dal.mysql.packagex.ProductPackageItemMapper;
import com.hissp.distribution.module.product.dal.mysql.packagex.ProductPackageMapper;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageConstants;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageItemTypeEnum;
import com.hissp.distribution.module.product.service.packagex.ProductPackageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductPackageServiceImpl implements ProductPackageService {

    @Resource
    private ProductPackageMapper packageMapper;
    @Resource
    private ProductPackageItemMapper itemMapper;
    @Resource
    private ProductPackageCommissionMapper commissionMapper;

    @Override
    public ProductPackageRespDTO getEnabledBySpuId(Long spuId) {
        ProductPackageDO pack = packageMapper.selectEnabledBySpuId(spuId);
        if (pack == null) return null;
        return buildPackageResp(pack);
    }

    @Override
    public ProductPackageRespDTO getById(Long packageId) {
        if (packageId == null) {
            return null;
        }
        ProductPackageDO pack = packageMapper.selectById(packageId);
        if (pack == null) {
            return null;
        }
        return buildPackageResp(pack);
    }

    private ProductPackageRespDTO buildPackageResp(ProductPackageDO pack) {
        ProductPackageRespDTO dto = new ProductPackageRespDTO();
        dto.setId(pack.getId());
        dto.setName(pack.getName());
        dto.setSpuId(pack.getSpuId());
        dto.setStatus(pack.getStatus());
        dto.setRemark(pack.getRemark());

        // items
        List<ProductPackageItemDO> items = itemMapper.selectByPackageId(pack.getId());
        List<ProductPackageRespDTO.Item> itemDTOs = items.stream().map(it -> {
            ProductPackageRespDTO.Item x = new ProductPackageRespDTO.Item();
            x.setId(it.getId());
            x.setPackageId(it.getPackageId());
            x.setItemType(it.getItemType());
            // itemId 映射为字符串，权益做可读化
            String itemIdStr;
            if (ProductPackageItemTypeEnum.ENTITLEMENT.getCode().equals(it.getItemType())) {
                if (PackageEntitlementType.OPEN_BROKERAGE == it.getItemId()) {
                    itemIdStr = ProductPackageConstants.ENTITLEMENT_OPEN_BROKERAGE_NAME;
                } else if (PackageEntitlementType.SET_LEVEL == it.getItemId()) {
                    itemIdStr = ProductPackageConstants.ENTITLEMENT_SET_LEVEL_NAME;
                } else {
                    itemIdStr = String.valueOf(it.getItemId());
                }
            } else {
                itemIdStr = String.valueOf(it.getItemId());
            }
            x.setItemId(itemIdStr);
            x.setItemQuantity(it.getItemQuantity());
            Map<String, Object> ext = it.getExtJson();
            x.setExtJson(ext);
            return x;
        }).collect(Collectors.toList());
        dto.setItems(itemDTOs);

        // commissions
        List<ProductPackageCommissionDO> comms = commissionMapper.selectByPackageId(pack.getId());
        List<ProductPackageRespDTO.Commission> commDTOs = comms.stream().map(c -> {
            ProductPackageRespDTO.Commission y = new ProductPackageRespDTO.Commission();
            y.setId(c.getId());
            y.setPackageId(c.getPackageId());
            y.setLevel(c.getLevel());
            y.setCommissionType(c.getCommissionType());
            y.setCommissionValue(c.getCommissionValue());
            y.setBaseType(c.getBaseType());
            y.setBaseAmount(c.getBaseAmount());
            return y;
        }).collect(Collectors.toList());
        dto.setCommissions(commDTOs);

        return dto;
    }
}
