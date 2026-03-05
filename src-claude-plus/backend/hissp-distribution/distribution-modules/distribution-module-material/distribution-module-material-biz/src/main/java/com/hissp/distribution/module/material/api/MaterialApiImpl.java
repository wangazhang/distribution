package com.hissp.distribution.module.material.api;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;

import java.util.List;

import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceRespVO;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.*;
import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.convert.MaterialBalanceConvert;
import com.hissp.distribution.module.material.convert.MaterialTxnConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialBalanceDO;
import com.hissp.distribution.module.material.service.balance.MaterialBalanceService;
import com.hissp.distribution.module.material.service.txn.MaterialTxnService;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import com.hissp.distribution.module.material.service.outbound.MaterialOutboundService;
import com.hissp.distribution.module.material.service.convert.MaterialConvertService;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.*;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Slf4j
@Service("materialApiImpl")
@Validated
public class MaterialApiImpl implements MaterialApi {

    @Resource
    private MaterialBalanceService materialBalanceService;
    @Resource
    private MaterialTxnService materialTxnService;
    @Resource
    private MaterialDefinitionService materialDefinitionService;
    @Resource
    @Lazy
    private MaterialOutboundService materialOutboundService;
    @Resource
    @Lazy
    private MaterialConvertService materialConvertService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyActs(List<MaterialActDTO> acts) {
        for (MaterialActDTO act : acts) {
            // Idempotency check: (bizKey, materialId, direction)
            if (materialTxnService.checkIdempotency(act.getBizKey(), act.getMaterialId(), act.getDirection().getValue())) {
                log.info("[applyActs][bizKey={} materialId={} direction={} already processed, skip]", act.getBizKey(), act.getMaterialId(), act.getDirection());
                continue;
            }

            // Get or create balance
            MaterialBalanceDO balance = materialBalanceService.createOrGetBalance(act.getUserId(), act.getMaterialId());

            // Retry update with optimistic lock
            int retries = 3;
            Integer newBalance = null;
            // allowNegative=true 表示特定账号（例如平台）允许出现负库存，避免业务链路被强行中断。
            boolean allowNegative = act.isAllowNegative();
            while (retries-- > 0) {
                int change = act.getQuantity() * act.getDirection().getValue();
                newBalance = balance.getAvailableBalance() + change;
                if (newBalance < 0 && !allowNegative) {
                    throw exception(com.hissp.distribution.module.trade.enums.ErrorCodeConstants.AFTER_SALE_REFUND_FAIL_RESOURCE_NOT_ENOUGH);
                }
                if (newBalance < 0) {
                    log.warn("[applyActs][userId={} materialId={} bizKey={}] 允许负库存，当前余额 {} -> {}", act.getUserId(),
                            act.getMaterialId(), act.getBizKey(), balance.getAvailableBalance(), newBalance);
                }

                if (materialBalanceService.updateBalanceWithOptimisticLock(balance, newBalance)) {
                    break;
                }
                // reload and retry
                balance = materialBalanceService.getBalanceByUserIdAndMaterialId(act.getUserId(), act.getMaterialId());
            }

            // Create transaction record
            materialTxnService.createTxnRecord(act, newBalance);
        }
    }

    @Override
    public boolean isRevocable(List<MaterialActDTO> acts) {
        for (MaterialActDTO act : acts) {
            if (act.getDirection() == MaterialActDirectionEnum.OUT) {
                MaterialBalanceRespDTO balance = getBalance(act.getUserId(), act.getMaterialId());
                if (balance.getAvailableBalance() < act.getQuantity()) {
                    log.warn("[isRevocable][balance not enough for userId={} materialId={}]", act.getUserId(), act.getMaterialId());
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public MaterialBalanceRespDTO getBalance(Long userId, Long materialId) {
        MaterialBalanceDO balance = materialBalanceService.getBalanceByUserIdAndMaterialId(userId, materialId);
        if (balance == null) {
            return new MaterialBalanceRespDTO().setUserId(userId).setMaterialId(materialId).setAvailableBalance(0).setFrozenBalance(0);
        }
        return new MaterialBalanceRespDTO().setUserId(userId).setMaterialId(materialId)
                .setAvailableBalance(balance.getAvailableBalance()).setFrozenBalance(balance.getFrozenBalance());
    }

    @Override
    public PageResult<MaterialBalanceRespDTO> getBalancePage(MaterialBalancePageReqDTO pageVO) {
        PageResult<MaterialBalanceRespVO> pageResult =
                materialBalanceService.getBalancePage(MaterialBalanceConvert.INSTANCE.convert(pageVO));
        return MaterialBalanceConvert.INSTANCE.convertPageToDto(pageResult);
    }

    @Override
    public PageResult<MaterialTxnRespDTO> getTxnPage(MaterialTxnPageReqDTO pageVO) {
        PageResult<com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnRespVO> pageResult =
                materialTxnService.getTxnPage(MaterialTxnConvert.INSTANCE.convert(pageVO));
        return MaterialTxnConvert.INSTANCE.convertPageToDto(pageResult);
    }

    @Override
    public java.util.List<MaterialBalanceRespDTO> getBalances(Long userId, java.util.List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.List<MaterialBalanceDO> list = materialBalanceService.getBalancesByUserIdAndMaterialIds(userId, materialIds);
        java.util.Map<Long, MaterialBalanceDO> map = list.stream()
                .collect(java.util.stream.Collectors.toMap(MaterialBalanceDO::getMaterialId, v -> v, (a, b) -> a));
        java.util.List<MaterialBalanceRespDTO> result = new java.util.ArrayList<>(materialIds.size());
        for (Long mid : materialIds) {
            MaterialBalanceDO found = map.get(mid);
            if (found == null) {
                result.add(new MaterialBalanceRespDTO().setUserId(userId).setMaterialId(mid).setAvailableBalance(0).setFrozenBalance(0));
            } else {
                result.add(new MaterialBalanceRespDTO().setUserId(found.getUserId()).setMaterialId(found.getMaterialId())
                        .setAvailableBalance(found.getAvailableBalance()).setFrozenBalance(found.getFrozenBalance()));
            }
        }
        return result;
    }

    // ========== 物料定义管理 ==========

    @Override
    public MaterialDefinitionRespDTO getDefinitionBySpuId(Long spuId) {
        MaterialDefinitionDO definition = materialDefinitionService.getDefinitionBySpuId(spuId);
        return convertToDefinitionRespDTO(definition);
    }

    @Override
    public List<MaterialDefinitionRespDTO> getDefinitions(List<Long> materialIds) {
        List<MaterialDefinitionDO> definitions = materialDefinitionService.getDefinitionList(materialIds);
        return definitions.stream()
                .map(this::convertToDefinitionRespDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public List<MaterialDefinitionRespDTO> getDefinitionListByName(String name) {
        List<MaterialDefinitionDO> definitions = materialDefinitionService.getDefinitionListByName(name);
        return definitions.stream()
                .map(this::convertToDefinitionRespDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean isSupportOutbound(Long materialId) {
        return materialDefinitionService.isSupportOutbound(materialId);
    }

    @Override
    public boolean isSupportConvert(Long materialId) {
        return materialDefinitionService.isSupportConvert(materialId);
    }

    // ========== 物料出库API ==========

    @Override
    public Long createOutbound(@Valid MaterialOutboundCreateReqDTO outboundReqDTO) {
        MaterialOutboundCreateReqVO createReqVO = convertToOutboundCreateReqVO(outboundReqDTO);
        return materialOutboundService.createOutbound(createReqVO);
    }

    @Override
    public void batchApproveOutbound(List<Long> ids, Long approveUserId, Boolean approved) {
        if (Boolean.TRUE.equals(approved)) {
            materialOutboundService.batchApproveOutbound(ids, approveUserId);
        } else {
            for (Long id : ids) {
                materialOutboundService.approveOutbound(id, approveUserId, false, "批量审核拒绝");
            }
        }
    }

    @Override
    public PageResult<MaterialOutboundRespDTO> getUserOutboundPage(Long userId, @Valid MaterialOutboundPageReqDTO pageReqDTO) {
        MaterialOutboundPageReqVO pageReqVO = convertToOutboundPageReqVO(pageReqDTO);
        pageReqVO.setUserId(userId);
        
        PageResult<MaterialOutboundRespVO> pageResult = materialOutboundService.getOutboundPage(pageReqVO);
        return convertToOutboundRespDTOPage(pageResult);
    }

    // ========== 物料转化API ==========

    @Override
    public List<MaterialConvertRuleRespDTO> getAvailableConvertRules(Long sourceMaterialId) {
        return materialConvertService.getAvailableConvertRules(sourceMaterialId);
    }

    @Override
    public MaterialConvertPreviewRespDTO previewConvert(@Valid MaterialConvertPreviewReqDTO previewReqDTO) {
        return materialConvertService.previewConvert(previewReqDTO);
    }

    @Override
    public MaterialConvertResultRespDTO executeConvert(@Valid MaterialConvertExecuteReqDTO convertReqDTO) {
        return materialConvertService.executeConvert(convertReqDTO);
    }

    @Override
    public PageResult<MaterialConvertRecordRespDTO> getUserConvertRecordPage(Long userId, @Valid MaterialConvertRecordPageReqDTO pageReqDTO) {
        pageReqDTO.setUserId(userId);
        return materialConvertService.getConvertRecordPage(pageReqDTO);
    }

    // ========== 物料价格管理 ==========

    @Override
    public BigDecimal getConvertPrice(Long ruleId) {
        return materialConvertService.getConvertPrice(ruleId);
    }

    @Override
    public List<MaterialConvertPriceRespDTO> getConvertPrices(List<Long> ruleIds) {
        return materialConvertService.getConvertPrices(ruleIds);
    }

    // ========== 私有转换方法 ==========

    private MaterialDefinitionRespDTO convertToDefinitionRespDTO(MaterialDefinitionDO definition) {
        if (definition == null) {
            return null;
        }
        
        MaterialDefinitionRespDTO respDTO = new MaterialDefinitionRespDTO();
        respDTO.setId(definition.getId());
        respDTO.setName(definition.getName());
        respDTO.setCode(definition.getCode());
        respDTO.setSpuId(definition.getSpuId());
        respDTO.setImage(definition.getImage());
        respDTO.setDescription(definition.getDescription());
        respDTO.setType(definition.getType());
        respDTO.setBaseUnit(definition.getBaseUnit());
        respDTO.setStatus(definition.getStatus());
        respDTO.setSupportOutbound(definition.getSupportOutbound());
        respDTO.setSupportConvert(definition.getSupportConvert());
        respDTO.setConvertStatus(definition.getConvertStatus());
        respDTO.setConvertedSpuId(definition.getConvertedSpuId());
        respDTO.setConvertPrice(definition.getConvertPrice());
        respDTO.setCreateTime(definition.getCreateTime());
        
        return respDTO;
    }

    private MaterialOutboundCreateReqVO convertToOutboundCreateReqVO(MaterialOutboundCreateReqDTO dto) {
        MaterialOutboundCreateReqVO vo = new MaterialOutboundCreateReqVO();
        vo.setUserId(dto.getUserId());
        vo.setAddressId(dto.getAddressId());
        vo.setReceiverName(dto.getReceiverName());
        vo.setReceiverMobile(dto.getReceiverMobile());
        vo.setReceiverProvince(dto.getReceiverProvince());
        vo.setReceiverCity(dto.getReceiverCity());
        vo.setReceiverDistrict(dto.getReceiverDistrict());
        vo.setReceiverDetailAddress(dto.getReceiverDetailAddress());
        vo.setRemark(dto.getRemark());
        
        List<MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO> items = dto.getItems().stream()
                .map(itemDTO -> {
                    MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO itemVO = 
                            new MaterialOutboundCreateReqVO.MaterialOutboundItemCreateReqVO();
                    itemVO.setMaterialId(itemDTO.getMaterialId());
                    itemVO.setQuantity(itemDTO.getQuantity());
                    return itemVO;
                })
                .collect(java.util.stream.Collectors.toList());
        vo.setItems(items);
        
        return vo;
    }

    private MaterialOutboundPageReqVO convertToOutboundPageReqVO(MaterialOutboundPageReqDTO dto) {
        MaterialOutboundPageReqVO vo = new MaterialOutboundPageReqVO();
        vo.setPageNo(dto.getPageNo());
        vo.setPageSize(dto.getPageSize());
        vo.setUserId(dto.getUserId());
        vo.setOutboundNo(dto.getOutboundNo());
        vo.setReceiverName(dto.getReceiverName());
        vo.setReceiverMobile(dto.getReceiverMobile());
        vo.setStatus(dto.getStatus());
        vo.setCreateTime(dto.getCreateTime());
        
        return vo;
    }

    private PageResult<MaterialOutboundRespDTO> convertToOutboundRespDTOPage(PageResult<MaterialOutboundRespVO> voPageResult) {
        if (voPageResult == null) {
            return new PageResult<>();
        }
        
        List<MaterialOutboundRespDTO> dtoList = voPageResult.getList().stream()
                .map(this::convertToOutboundRespDTO)
                .collect(java.util.stream.Collectors.toList());
        
        return new PageResult<>(dtoList, voPageResult.getTotal());
    }

    private MaterialOutboundRespDTO convertToOutboundRespDTO(MaterialOutboundRespVO vo) {
        MaterialOutboundRespDTO dto = new MaterialOutboundRespDTO();
        dto.setId(vo.getId());
        dto.setOutboundNo(vo.getOutboundNo());
        dto.setUserId(vo.getUserId());
        dto.setUserNickname(vo.getUserNickname());
        dto.setAddressId(vo.getAddressId());
        dto.setReceiverName(vo.getReceiverName());
        dto.setReceiverMobile(vo.getReceiverMobile());
        dto.setReceiverProvince(vo.getReceiverProvince());
        dto.setReceiverCity(vo.getReceiverCity());
        dto.setReceiverDistrict(vo.getReceiverDistrict());
        dto.setReceiverDetailAddress(vo.getReceiverDetailAddress());
        dto.setStatus(vo.getStatus());
        dto.setStatusName(vo.getStatusName());
        dto.setRemark(vo.getRemark());
        dto.setLogisticsCode(vo.getLogisticsCode());
        dto.setLogisticsCompany(vo.getLogisticsCompany());
        dto.setApproveTime(vo.getApproveTime());
        dto.setApproveUserId(vo.getApproveUserId());
        dto.setApproveUserName(vo.getApproveUserName());
        dto.setShipTime(vo.getShipTime());
        dto.setCompleteTime(vo.getCompleteTime());
        dto.setCancelTime(vo.getCancelTime());
        dto.setCancelReason(vo.getCancelReason());
        dto.setCreateTime(vo.getCreateTime());
        dto.setCreateBy(vo.getCreateBy());
        
        if (vo.getItems() != null) {
            List<MaterialOutboundRespDTO.MaterialOutboundItemRespDTO> itemDTOs = vo.getItems().stream()
                    .map(itemVO -> {
                        MaterialOutboundRespDTO.MaterialOutboundItemRespDTO itemDTO = 
                                new MaterialOutboundRespDTO.MaterialOutboundItemRespDTO();
                        itemDTO.setId(itemVO.getId());
                        itemDTO.setMaterialId(itemVO.getMaterialId());
                        itemDTO.setMaterialName(itemVO.getMaterialName());
                        itemDTO.setMaterialCode(itemVO.getMaterialCode());
                        itemDTO.setQuantity(itemVO.getQuantity());
                        itemDTO.setBaseUnit(itemVO.getBaseUnit());
                        return itemDTO;
                    })
                    .collect(java.util.stream.Collectors.toList());
            dto.setItems(itemDTOs);
        }
        
        return dto;
    }

}
