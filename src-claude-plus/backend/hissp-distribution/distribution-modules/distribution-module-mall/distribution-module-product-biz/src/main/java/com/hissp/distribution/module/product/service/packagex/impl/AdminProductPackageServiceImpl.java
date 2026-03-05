package com.hissp.distribution.module.product.service.packagex.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hissp.distribution.framework.common.exception.ServiceException;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.product.api.packagex.PackageEntitlementType;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackageCommissionVO;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackageItemVO;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackagePageReqVO;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackageRespVO;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.ProductPackageSaveReqVO;
import com.hissp.distribution.module.product.enums.ErrorCodeConstants;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageCommissionBaseTypeEnum;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageCommissionTypeEnum;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageConstants;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageItemTypeEnum;
import com.hissp.distribution.module.product.enums.packagex.ProductPackageStatusEnum;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.invalidParamException;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageCommissionDO;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageDO;
import com.hissp.distribution.module.product.dal.dataobject.packagex.ProductPackageItemDO;
import com.hissp.distribution.module.product.dal.mysql.packagex.ProductPackageCommissionMapper;
import com.hissp.distribution.module.product.dal.mysql.packagex.ProductPackageItemMapper;
import com.hissp.distribution.module.product.dal.mysql.packagex.ProductPackageMapper;
import com.hissp.distribution.module.product.service.packagex.AdminProductPackageService;
import jakarta.annotation.Resource;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
@Service
public class AdminProductPackageServiceImpl implements AdminProductPackageService {

    @Resource
    private ProductPackageMapper packageMapper;
    @Resource
    private ProductPackageItemMapper itemMapper;
    @Resource
    private ProductPackageCommissionMapper commissionMapper;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private MaterialApi materialApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProductPackageSaveReqVO reqVO) {
        validateReq(reqVO, null);
        ProductPackageDO pack = new ProductPackageDO();
        pack.setName(reqVO.getName());
        pack.setSpuId(reqVO.getSpuId());
        pack.setStatus(reqVO.getStatus());
        pack.setRemark(reqVO.getRemark());
        packageMapper.insert(pack);
        batchSaveItemsAndCommissions(pack.getId(), reqVO.getItems(), reqVO.getCommissions());
        return pack.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProductPackageSaveReqVO reqVO) {
        if (reqVO.getId() == null) throw invalidParamException("id 不能为空");
        validateReq(reqVO, reqVO.getId());
        ProductPackageDO exist = packageMapper.selectById(reqVO.getId());
        if (exist == null || Boolean.TRUE.equals(exist.getDeleted())) throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_NOT_EXISTS);
        exist.setName(reqVO.getName());
        exist.setSpuId(reqVO.getSpuId());
        exist.setStatus(reqVO.getStatus());
        exist.setRemark(reqVO.getRemark());
        packageMapper.updateById(exist);
        // 清空并重建子表
        itemMapper.physicalDeleteByPackageId(exist.getId());
        commissionMapper.physicalDeleteByPackageId(exist.getId());
        batchSaveItemsAndCommissions(exist.getId(), reqVO.getItems(), reqVO.getCommissions());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ProductPackageDO exist = packageMapper.selectById(id);
        if (exist == null || Boolean.TRUE.equals(exist.getDeleted())) throw new ServiceException(404, "套包不存在");
        if (Objects.equals(status, 1)) { // 启用唯一性校验
            Long cnt = packageMapper.selectEnabledCountBySpuIdExcludeId(exist.getSpuId(), id);
            if (cnt != null && cnt > 0) throw new ServiceException(400, "同一 SPU 仅允许一个启用的套包");
        }
        exist.setStatus(status);
        packageMapper.updateById(exist);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        ProductPackageDO exist = packageMapper.selectById(id);
        if (exist == null || Boolean.TRUE.equals(exist.getDeleted())) return;
        // 逻辑删除
        packageMapper.deleteById(id);
        itemMapper.delete(new LambdaQueryWrapper<ProductPackageItemDO>().eq(ProductPackageItemDO::getPackageId, id));
        commissionMapper.delete(new LambdaQueryWrapper<ProductPackageCommissionDO>().eq(ProductPackageCommissionDO::getPackageId, id));
    }

    @Override
    public ProductPackageRespVO get(Long id) {
        ProductPackageDO pack = packageMapper.selectById(id);
        if (pack == null || Boolean.TRUE.equals(pack.getDeleted())) return null;
        return assembleResp(pack);
    }

    @Override
    public PageResult<ProductPackageRespVO> page(ProductPackagePageReqVO reqVO) {
        PageResult<ProductPackageDO> page = packageMapper.selectPage(reqVO);
        List<ProductPackageRespVO> list = page.getList().stream().map(this::toSimpleResp).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public ProductPackageRespVO getBySpu(Long spuId) {
        ProductPackageDO pack = packageMapper.selectEnabledBySpuId(spuId);
        if (pack == null) return null;
        return assembleResp(pack);
    }

    // ================== helper ==================

    private void validateReq(ProductPackageSaveReqVO reqVO, Long selfId) {
        if (ProductPackageStatusEnum.ENABLE.getCode().equals(reqVO.getStatus())) { // 启用唯一性
            Long cnt = packageMapper.selectEnabledCountBySpuIdExcludeId(reqVO.getSpuId(), selfId);
            if (cnt != null && cnt > 0) throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_ONLY_ONE_ENABLED_PER_SPU);
        }
        if (CollUtil.isEmpty(reqVO.getItems())) throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_ITEMS_EMPTY);
        // 校验权益参数
        for (ProductPackageItemVO it : reqVO.getItems()) {
            if (ProductPackageItemTypeEnum.PRODUCT.getCode().equals(it.getItemType())) {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(it.getItemId());
                if (definition == null) {
                    throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_MATERIAL_DEFINITION_NOT_EXISTS, it.getItemId());
                }
                if (!Objects.equals(definition.getStatus(), 1)) {
                    throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_MATERIAL_DEFINITION_DISABLED, it.getItemId());
                }
            }
            if (ProductPackageItemTypeEnum.ENTITLEMENT.getCode().equals(it.getItemType())) { // 权益
                if (Objects.equals(it.getItemId(), PackageEntitlementType.SET_LEVEL)) {
                    Map<String, Object> ext = it.getExtJson();
                    if (ext == null || !ext.containsKey(ProductPackageConstants.EXT_KEY_LEVEL_ID)) {
                        throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_SET_LEVEL_NEED_LEVEL_ID);
                    }
                    Long levelId = null;
                    try { levelId = Long.valueOf(String.valueOf(ext.get(ProductPackageConstants.EXT_KEY_LEVEL_ID))); } catch (Exception ignored) {}
                    if (levelId == null) throw invalidParamException("extJson.levelId 非法");
                    MemberLevelRespDTO level = memberLevelApi.getMemberLevel(levelId);
                    if (level == null) throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_LEVEL_NOT_EXISTS);
                }
            }
        }
        // 校验分佣规则
        if (CollUtil.isNotEmpty(reqVO.getCommissions())) {
            long l1 = reqVO.getCommissions().stream().filter(c -> Objects.equals(c.getLevel(), 1)).count();
            long l2 = reqVO.getCommissions().stream().filter(c -> Objects.equals(c.getLevel(), 2)).count();
            if (l1 > 1 || l2 > 1) throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_COMMISSION_LEVEL_DUPLICATE);
            for (ProductPackageCommissionVO c : reqVO.getCommissions()) {
                // 比例分佣
                if (ProductPackageCommissionTypeEnum.RATIO.getCode().equals(c.getCommissionType())) {
                    // 只有当分佣基准是“自定义”时，才需要校验 baseAmount
                    if (ProductPackageCommissionBaseTypeEnum.CUSTOM.getCode().equals(c.getBaseType())) {
                        if (c.getBaseAmount() == null || c.getBaseAmount() <= 0) {
                            throw exception(ErrorCodeConstants.PRODUCT_PACKAGE_COMMISSION_RATIO_BASE_INVALID);
                        }
                    }
                }
            }
        }
    }

    private void batchSaveItemsAndCommissions(Long packageId, List<ProductPackageItemVO> items,
                                              List<ProductPackageCommissionVO> commissions) {
        if (CollUtil.isNotEmpty(items)) {
            List<ProductPackageItemDO> list = new ArrayList<>();
            for (ProductPackageItemVO it : items) {
                ProductPackageItemDO d = new ProductPackageItemDO();
                d.setPackageId(packageId);
                d.setItemType(it.getItemType());
                d.setItemId(it.getItemId());
                d.setItemQuantity(ObjectUtil.defaultIfNull(it.getItemQuantity(), ProductPackageConstants.DEFAULT_ITEM_QUANTITY));
                d.setExtJson(it.getExtJson());
                list.add(d);
            }
            itemMapper.insertBatch(list);
        }
        if (CollUtil.isNotEmpty(commissions)) {
            List<ProductPackageCommissionDO> list = new ArrayList<>();
            for (ProductPackageCommissionVO c : commissions) {
                ProductPackageCommissionDO d = new ProductPackageCommissionDO();
                d.setPackageId(packageId);
                d.setLevel(c.getLevel());
                d.setCommissionType(c.getCommissionType());
                d.setCommissionValue(c.getCommissionValue());
                d.setBaseType(c.getBaseType());
                d.setBaseAmount(c.getBaseAmount());
                list.add(d);
            }
            commissionMapper.insertBatch(list);
        }
    }

    private ProductPackageRespVO assembleResp(ProductPackageDO pack) {
        ProductPackageRespVO vo = toSimpleResp(pack);
        List<ProductPackageItemDO> items = itemMapper.selectByPackageId(pack.getId());
        List<ProductPackageCommissionDO> comms = commissionMapper.selectByPackageId(pack.getId());
        vo.setItems(items.stream().map(d -> {
            ProductPackageRespVO.Item x = new ProductPackageRespVO.Item();
            x.setId(d.getId());
            x.setPackageId(d.getPackageId());
            x.setItemType(d.getItemType());
            x.setItemId(d.getItemId());
            x.setItemQuantity(d.getItemQuantity());
            x.setExtJson(d.getExtJson());
            return x;
        }).collect(Collectors.toList()));
        vo.setCommissions(comms.stream().map(c -> {
            ProductPackageRespVO.Commission y = new ProductPackageRespVO.Commission();
            y.setId(c.getId());
            y.setPackageId(c.getPackageId());
            y.setLevel(c.getLevel());
            y.setCommissionType(c.getCommissionType());
            y.setCommissionValue(c.getCommissionValue());
            y.setBaseType(c.getBaseType());
            y.setBaseAmount(c.getBaseAmount());
            return y;
        }).collect(Collectors.toList()));
        return vo;
    }

    private ProductPackageRespVO toSimpleResp(ProductPackageDO pack) {
        ProductPackageRespVO vo = new ProductPackageRespVO();
        vo.setId(pack.getId());
        vo.setName(pack.getName());
        vo.setSpuId(pack.getSpuId());
        vo.setStatus(pack.getStatus());
        vo.setRemark(pack.getRemark());
        return vo;
    }
}
