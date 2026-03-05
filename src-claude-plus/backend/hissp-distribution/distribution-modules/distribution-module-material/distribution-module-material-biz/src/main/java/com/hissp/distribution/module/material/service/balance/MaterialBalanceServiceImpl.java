package com.hissp.distribution.module.material.service.balance;

import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.module.material.api.dto.MaterialActDTO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceAdjustReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalancePageReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceRespVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceStatRespVO;
import com.hissp.distribution.module.material.convert.MaterialBalanceConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialBalanceDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialBalanceMapper;
import com.hissp.distribution.module.material.enums.ErrorCodeConstants;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import com.hissp.distribution.module.material.service.txn.MaterialTxnService;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物料余额 Service 实现类
 */
@Service
@Validated
public class MaterialBalanceServiceImpl implements MaterialBalanceService {

    private static final Logger log = LoggerFactory.getLogger(MaterialBalanceServiceImpl.class);

    @Resource
    private MaterialBalanceMapper balanceMapper;
    @Resource
    private MaterialDefinitionService definitionService;
    @Resource
    private MaterialTxnService txnService;
    @Resource
    private MemberUserApi memberUserApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @Override
    public PageResult<MaterialBalanceRespVO> getBalancePage(MaterialBalancePageReqVO pageReqVO) {
        PageResult<MaterialBalanceDO> pageResult = balanceMapper.selectPage(pageReqVO);
        if (pageResult == null || CollUtil.isEmpty(pageResult.getList())) {
            return PageResult.empty();
        }

        // Enrich with user and material names
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(CollectionUtils.convertSet(pageResult.getList(), MaterialBalanceDO::getUserId));
        Map<Long, MaterialDefinitionDO> definitionMap = definitionService.getDefinitionMap(CollectionUtils.convertSet(pageResult.getList(), MaterialBalanceDO::getMaterialId));

        // Debug log
        log.info("Balance list size: {}, User map size: {}, Definition map size: {}",
            pageResult.getList().size(), userMap.size(), definitionMap.size());

        // 收集需要通过SPU获取名称的物料ID
        List<Long> spuIds = CollectionUtils.convertList(pageResult.getList(), balance -> {
            MaterialDefinitionDO definition = definitionMap.get(balance.getMaterialId());
            return (definition != null && definition.getSpuId() != null) ? definition.getSpuId() : null;
        });
        spuIds = spuIds.stream().filter(spuId -> spuId != null).distinct().collect(Collectors.toList());

        // 批量获取SPU信息
        Map<Long, ProductSpuRespDTO> spuMap = CollUtil.isEmpty(spuIds) ?
            Map.of() : productSpuApi.getSpusMap(spuIds);

        log.info("SPU IDs: {}, SPU map size: {}", spuIds, spuMap.size());

        List<MaterialBalanceRespVO> respList = CollectionUtils.convertList(pageResult.getList(), balance -> {
            MaterialBalanceRespVO respVO = MaterialBalanceConvert.INSTANCE.convert(balance);
            if (userMap.containsKey(balance.getUserId())) {
                respVO.setNickname(userMap.get(balance.getUserId()).getNickname());
            }

            // 填充物料名称：优先使用物料定义的名称，如果为空则使用SPU的名称
            if (definitionMap.containsKey(balance.getMaterialId())) {
                MaterialDefinitionDO definition = definitionMap.get(balance.getMaterialId());
                String materialName = definition.getName();

                // 如果物料名称为空，尝试从SPU获取名称
                if (StrUtil.isBlank(materialName) && definition.getSpuId() != null) {
                    ProductSpuRespDTO spu = spuMap.get(definition.getSpuId());
                    if (spu != null && StrUtil.isNotBlank(spu.getName())) {
                        materialName = spu.getName();
                        log.info("Using SPU name for material ID {}: {}", balance.getMaterialId(), materialName);
                    }
                }

                respVO.setMaterialName(materialName);
                log.info("Set material name for ID {}: {}", balance.getMaterialId(), materialName);
            } else {
                log.warn("Material definition not found for ID: {}", balance.getMaterialId());
            }
            return respVO;
        });

        return new PageResult<>(respList, pageResult.getTotal());
    }

    @Override
    public MaterialBalanceDO getBalanceByUserIdAndMaterialId(Long userId, Long materialId) {
        return balanceMapper.selectByUserIdAndMaterialId(userId, materialId);
    }

    @Override
    public List<MaterialBalanceDO> getBalancesByUserIdAndMaterialIds(Long userId, List<Long> materialIds) {
        return balanceMapper.selectListByUserIdAndMaterialIds(userId, materialIds);
    }

    @Override
    public MaterialBalanceDO createOrGetBalance(Long userId, Long materialId) {
        MaterialBalanceDO balance = balanceMapper.selectByUserIdAndMaterialId(userId, materialId);
        if (balance == null) {
            balance = new MaterialBalanceDO().setUserId(userId).setMaterialId(materialId)
                    .setAvailableBalance(0).setFrozenBalance(0).setVersion(0);
            balanceMapper.insert(balance);
        }
        return balance;
    }

    @Override
    public boolean updateBalanceWithOptimisticLock(MaterialBalanceDO balance, Integer newAvailableBalance) {
        MaterialBalanceDO update = new MaterialBalanceDO();
        update.setId(balance.getId());
        update.setAvailableBalance(newAvailableBalance);
        update.setVersion(balance.getVersion());
        
        int updated = balanceMapper.update(update, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<MaterialBalanceDO>()
                .eq(MaterialBalanceDO::getId, balance.getId())
                .eq(MaterialBalanceDO::getVersion, balance.getVersion()));
        return updated > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustBalance(MaterialBalanceAdjustReqVO adjustReqVO, Long operatorId) {
        // 1. 验证物料定义是否存在
        MaterialDefinitionDO definition = definitionService.getDefinition(adjustReqVO.getMaterialId());
        if (definition == null) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 2. 获取或创建余额记录
        MaterialBalanceDO balance = createOrGetBalance(adjustReqVO.getUserId(), adjustReqVO.getMaterialId());
        
        // 3. 计算新余额
        Integer currentBalance = balance.getAvailableBalance();
        Integer newBalance;
        if (adjustReqVO.getAdjustType() == 1) { // 增加
            newBalance = currentBalance + adjustReqVO.getAdjustAmount();
        } else { // 减少
            newBalance = currentBalance - adjustReqVO.getAdjustAmount();
            if (newBalance < 0) {
                throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_BALANCE_NOT_ENOUGH);
            }
        }

        // 4. 使用乐观锁更新余额
        boolean updated = updateBalanceWithOptimisticLock(balance, newBalance);
        if (!updated) {
            throw ServiceExceptionUtil.exception(ErrorCodeConstants.MATERIAL_BALANCE_NOT_EXISTS);
        }

        // 5. 记录流水
        MaterialActDTO act = MaterialActDTO.builder()
                .userId(adjustReqVO.getUserId())
                .materialId(adjustReqVO.getMaterialId())
                .quantity(adjustReqVO.getAdjustAmount())
                .direction(adjustReqVO.getAdjustType() == 1 ? 
                    com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum.IN : 
                    com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum.OUT)
                .bizKey("ADMIN_ADJUST_" + IdUtil.simpleUUID())
                .bizType("ADMIN_ADJUST")
                .reason(adjustReqVO.getReason())
                .build();

        txnService.createTxnRecord(act, newBalance);
    }

    @Override
    public MaterialBalanceStatRespVO getBalanceStatistics() {
        MaterialBalanceStatRespVO stat = new MaterialBalanceStatRespVO();
        
        // 查询统计数据
        Long totalUsers = balanceMapper.selectCount(null);
        Long usersWithBalance = balanceMapper.selectCountWithBalance();
        Long totalMaterials = balanceMapper.selectDistinctMaterialCount();
        Long totalBalance = balanceMapper.selectTotalBalance();
        
        stat.setTotalUsers(totalUsers);
        stat.setUsersWithBalance(usersWithBalance);
        stat.setTotalMaterials(totalMaterials);
        stat.setTotalBalance(totalBalance);
        
        if (usersWithBalance > 0) {
            stat.setAverageBalance(totalBalance.doubleValue() / usersWithBalance);
        } else {
            stat.setAverageBalance(0.0);
        }
        
        return stat;
    }

}

