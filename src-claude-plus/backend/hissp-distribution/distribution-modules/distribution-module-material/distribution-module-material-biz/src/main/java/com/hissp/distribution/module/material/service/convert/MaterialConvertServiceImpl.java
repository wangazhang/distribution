package com.hissp.distribution.module.material.service.convert;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.*;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRuleDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRecordDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialConvertRuleMapper;
import com.hissp.distribution.module.material.dal.mysql.MaterialConvertRecordMapper;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hissp.distribution.module.material.api.enums.MaterialActDirectionEnum;
import com.hissp.distribution.module.material.enums.ErrorCodeConstants;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;

/**
 * 物料转化 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MaterialConvertServiceImpl implements MaterialConvertService {

    @Resource
    private MaterialConvertRuleMapper convertRuleMapper;
    
    @Resource
    private MaterialConvertRecordMapper convertRecordMapper;
    
    @Resource
    private MaterialDefinitionService materialDefinitionService;
    
    @Resource
    private MaterialApi materialApi;

    // ========== 转化规则管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createConvertRule(@Valid MaterialConvertRuleCreateReqDTO createReqVO) {
        // 验证源物料和目标物料存在且支持转化
        MaterialDefinitionDO sourceMaterial = materialDefinitionService.getDefinition(createReqVO.getSourceMaterialId());
        MaterialDefinitionDO targetMaterial = materialDefinitionService.getDefinition(createReqVO.getTargetMaterialId());
        
        if (sourceMaterial == null || targetMaterial == null) {
            throw exception(ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS);
        }
        
        if (!Boolean.TRUE.equals(sourceMaterial.getSupportConvert()) || 
            !Boolean.TRUE.equals(targetMaterial.getSupportConvert())) {
            throw exception(ErrorCodeConstants.MATERIAL_NOT_SUPPORT_CONVERT);
        }
        
        // 检查是否已存在相同的转化规则
        MaterialConvertRuleDO existingRule = convertRuleMapper.selectBySourceAndTarget(
                createReqVO.getSourceMaterialId(), createReqVO.getTargetMaterialId());
        if (existingRule != null) {
            throw exception(ErrorCodeConstants.MATERIAL_CONVERT_RULE_EXISTS);
        }
        
        // 创建转化规则
        MaterialConvertRuleDO rule = buildConvertRuleDO(createReqVO);
        convertRuleMapper.insert(rule);
        
        log.info("[MaterialConvertService][创建物料转化规则成功] ruleId={}, ruleName={}", 
                rule.getId(), createReqVO.getRuleName());
        
        return rule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConvertRule(@Valid MaterialConvertRuleUpdateReqDTO updateReqVO) {
        // 验证转化规则存在
        MaterialConvertRuleDO existingRule = validateConvertRuleExists(updateReqVO.getId());
        
        // 更新转化规则
        MaterialConvertRuleDO updateObj = buildConvertRuleDO(updateReqVO);
        updateObj.setId(updateReqVO.getId());
        convertRuleMapper.updateById(updateObj);
        
        log.info("[MaterialConvertService][更新物料转化规则成功] ruleId={}, ruleName={}", 
                updateReqVO.getId(), updateReqVO.getRuleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConvertRule(Long id) {
        // 验证转化规则存在
        validateConvertRuleExists(id);
        
        // 检查是否有关联的转化记录
        long recordCount = convertRecordMapper.selectCountByRuleId(id);
        if (recordCount > 0) {
            throw exception(ErrorCodeConstants.MATERIAL_CONVERT_RULE_HAS_RECORDS);
        }
        
        // 删除转化规则
        convertRuleMapper.deleteById(id);
        
        log.info("[MaterialConvertService][删除物料转化规则成功] ruleId={}", id);
    }

    @Override
    public MaterialConvertRuleDO getConvertRule(Long id) {
        return convertRuleMapper.selectById(id);
    }

    @Override
    public PageResult<MaterialConvertRuleDO> getConvertRulePage(MaterialConvertRulePageReqDTO pageReqVO) {
        return convertRuleMapper.selectPage(pageReqVO);
    }

    // ========== API 接口方法 ==========

    @Override
    public List<MaterialConvertRuleRespDTO> getAvailableConvertRules(Long sourceMaterialId) {
        List<MaterialConvertRuleDO> rules = convertRuleMapper.selectActiveBySourceMaterialId(sourceMaterialId);
        return rules.stream()
                .map(this::convertToConvertRuleRespDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MaterialConvertPreviewRespDTO previewConvert(@Valid MaterialConvertPreviewReqDTO previewReqDTO) {
        // 获取转化规则
        MaterialConvertRuleDO rule = validateConvertRuleExists(previewReqDTO.getRuleId());
        
        // 计算转化结果
        Integer targetQuantity = calculateTargetQuantity(previewReqDTO.getSourceQuantity(), rule.getConvertRatio());
        BigDecimal totalConvertPrice = rule.getConvertPrice().multiply(BigDecimal.valueOf(previewReqDTO.getSourceQuantity()));
        
        // 检查用户余额
        MaterialBalanceRespDTO balance = materialApi.getBalance(previewReqDTO.getUserId(), rule.getSourceMaterialId());
        boolean hasEnoughBalance = balance.getAvailableBalance() >= previewReqDTO.getSourceQuantity();
        
        // 获取物料信息
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(
                List.of(rule.getSourceMaterialId(), rule.getTargetMaterialId()));
        MaterialDefinitionDO sourceMaterial = materialMap.get(rule.getSourceMaterialId());
        MaterialDefinitionDO targetMaterial = materialMap.get(rule.getTargetMaterialId());
        
        // 构建预览响应
        MaterialConvertPreviewRespDTO respDTO = new MaterialConvertPreviewRespDTO();
        respDTO.setRuleId(rule.getId());
        respDTO.setRuleName(rule.getRuleName());
        respDTO.setSourceMaterialId(rule.getSourceMaterialId());
        respDTO.setSourceMaterialName(sourceMaterial.getName());
        respDTO.setSourceQuantity(previewReqDTO.getSourceQuantity());
        respDTO.setTargetMaterialId(rule.getTargetMaterialId());
        respDTO.setTargetMaterialName(targetMaterial.getName());
        respDTO.setTargetQuantity(targetQuantity);
        respDTO.setConvertRatio(rule.getConvertRatio());
        respDTO.setConvertPrice(rule.getConvertPrice());
        respDTO.setTotalConvertPrice(totalConvertPrice);
        respDTO.setHasEnoughBalance(hasEnoughBalance);
        respDTO.setCurrentSourceBalance(balance.getAvailableBalance());
        
        return respDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MaterialConvertResultRespDTO executeConvert(@Valid MaterialConvertExecuteReqDTO convertReqDTO) {
        // 获取转化规则
        MaterialConvertRuleDO rule = validateConvertRuleExists(convertReqDTO.getRuleId());
        
        // 验证用户余额充足
        MaterialBalanceRespDTO balance = materialApi.getBalance(convertReqDTO.getUserId(), rule.getSourceMaterialId());
        if (balance.getAvailableBalance() < convertReqDTO.getSourceQuantity()) {
            throw exception(ErrorCodeConstants.MATERIAL_BALANCE_NOT_ENOUGH);
        }
        
        // 计算转化结果
        Integer targetQuantity = calculateTargetQuantity(convertReqDTO.getSourceQuantity(), rule.getConvertRatio());
        BigDecimal totalConvertPrice = rule.getConvertPrice().multiply(BigDecimal.valueOf(convertReqDTO.getSourceQuantity()));
        
        try {
            // 扣减源物料
            List<MaterialActDTO> acts = List.of(
                    MaterialActDTO.builder()
                            .userId(convertReqDTO.getUserId())
                            .materialId(rule.getSourceMaterialId())
                            .quantity(convertReqDTO.getSourceQuantity())
                            .direction(MaterialActDirectionEnum.OUT)
                            .bizType("CONVERT")
                            .bizKey(String.valueOf(convertReqDTO.getRuleId()))
                            .reason("物料转化消耗")
                            .build(),
                    MaterialActDTO.builder()
                            .userId(convertReqDTO.getUserId())
                            .materialId(rule.getTargetMaterialId())
                            .quantity(targetQuantity)
                            .direction(MaterialActDirectionEnum.IN)
                            .bizType("CONVERT")
                            .bizKey(String.valueOf(convertReqDTO.getRuleId()))
                            .reason("物料转化获得")
                            .build()
            );
            
            materialApi.applyActs(acts);
            
            // 创建转化记录
            MaterialConvertRecordDO record = buildConvertRecordDO(convertReqDTO, rule, targetQuantity, totalConvertPrice, true, null);
            convertRecordMapper.insert(record);
            
            // 获取物料信息
            Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(
                    List.of(rule.getSourceMaterialId(), rule.getTargetMaterialId()));
            MaterialDefinitionDO sourceMaterial = materialMap.get(rule.getSourceMaterialId());
            MaterialDefinitionDO targetMaterial = materialMap.get(rule.getTargetMaterialId());
            
            // 构建成功响应
            MaterialConvertResultRespDTO respDTO = new MaterialConvertResultRespDTO();
            respDTO.setConvertRecordId(record.getId());
            respDTO.setSuccess(true);
            respDTO.setRuleId(rule.getId());
            respDTO.setRuleName(rule.getRuleName());
            respDTO.setSourceMaterialId(rule.getSourceMaterialId());
            respDTO.setSourceMaterialName(sourceMaterial.getName());
            respDTO.setSourceQuantity(convertReqDTO.getSourceQuantity());
            respDTO.setTargetMaterialId(rule.getTargetMaterialId());
            respDTO.setTargetMaterialName(targetMaterial.getName());
            respDTO.setTargetQuantity(targetQuantity);
            respDTO.setConvertPrice(totalConvertPrice);
            respDTO.setConvertTime(record.getCreateTime());
            
            log.info("[MaterialConvertService][物料转化成功] userId={}, ruleId={}, sourceQuantity={}, targetQuantity={}", 
                    convertReqDTO.getUserId(), convertReqDTO.getRuleId(), convertReqDTO.getSourceQuantity(), targetQuantity);
            
            return respDTO;
            
        } catch (Exception e) {
            // 创建失败记录
            MaterialConvertRecordDO failRecord = buildConvertRecordDO(convertReqDTO, rule, targetQuantity, totalConvertPrice, false, e.getMessage());
            convertRecordMapper.insert(failRecord);
            
            log.error("[MaterialConvertService][物料转化失败] userId={}, ruleId={}, error={}", 
                    convertReqDTO.getUserId(), convertReqDTO.getRuleId(), e.getMessage(), e);
            
            // 构建失败响应
            MaterialConvertResultRespDTO respDTO = new MaterialConvertResultRespDTO();
            respDTO.setConvertRecordId(failRecord.getId());
            respDTO.setSuccess(false);
            respDTO.setFailureReason(e.getMessage());
            respDTO.setRuleId(rule.getId());
            respDTO.setRuleName(rule.getRuleName());
            respDTO.setConvertTime(failRecord.getCreateTime());
            
            return respDTO;
        }
    }

    @Override
    public PageResult<MaterialConvertRecordRespDTO> getConvertRecordPage(@Valid MaterialConvertRecordPageReqDTO pageReqDTO) {
        PageResult<MaterialConvertRecordDO> pageResult = convertRecordMapper.selectPage(pageReqDTO);
        return convertToConvertRecordRespDTOPage(pageResult);
    }

    @Override
    public MaterialConvertRecordDO getConvertRecord(Long id) {
        return convertRecordMapper.selectById(id);
    }

    @Override
    public void cancelConvert(Long id) {
        // TODO: 实现取消转化逻辑
        throw new UnsupportedOperationException("取消转化功能待实现");
    }

    @Override
    public List<MaterialConvertRuleDO> getConvertRuleList() {
        return convertRuleMapper.selectList();
    }

    @Override
    public List<MaterialConvertPriceRespDTO> getConvertPrices(List<Long> ruleIds) {
        if (CollUtil.isEmpty(ruleIds)) {
            return List.of();
        }
        
        List<MaterialConvertRuleDO> rules = convertRuleMapper.selectBatchIds(ruleIds);
        
        // 获取物料信息
        List<Long> materialIds = rules.stream()
                .flatMap(rule -> List.of(rule.getSourceMaterialId(), rule.getTargetMaterialId()).stream())
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(materialIds);
        
        return rules.stream()
                .map(rule -> {
                    MaterialConvertPriceRespDTO dto = new MaterialConvertPriceRespDTO();
                    dto.setRuleId(rule.getId());
                    dto.setConvertPrice(rule.getConvertPrice());
                    dto.setRuleName(rule.getRuleName());
                    
                    MaterialDefinitionDO sourceMaterial = materialMap.get(rule.getSourceMaterialId());
                    MaterialDefinitionDO targetMaterial = materialMap.get(rule.getTargetMaterialId());
                    dto.setSourceMaterialName(sourceMaterial != null ? sourceMaterial.getName() : "");
                    dto.setTargetMaterialName(targetMaterial != null ? targetMaterial.getName() : "");
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // ========== 转化价格管理 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setConvertPrice(Long ruleId, BigDecimal price) {
        MaterialConvertRuleDO rule = validateConvertRuleExists(ruleId);
        
        MaterialConvertRuleDO updateObj = new MaterialConvertRuleDO();
        updateObj.setId(ruleId);
        updateObj.setConvertPrice(price);
        convertRuleMapper.updateById(updateObj);
        
        log.info("[MaterialConvertService][设置转化价格] ruleId={}, price={}", ruleId, price);
    }

    @Override
    public BigDecimal getConvertPrice(Long ruleId) {
        MaterialConvertRuleDO rule = getConvertRule(ruleId);
        return rule != null ? rule.getConvertPrice() : BigDecimal.ZERO;
    }

    // ========== 私有方法 ==========

    private MaterialConvertRuleDO validateConvertRuleExists(Long id) {
        MaterialConvertRuleDO rule = convertRuleMapper.selectById(id);
        if (rule == null) {
            throw exception(ErrorCodeConstants.MATERIAL_CONVERT_RULE_NOT_EXISTS);
        }
        return rule;
    }

    private MaterialConvertRuleDO buildConvertRuleDO(MaterialConvertRuleCreateReqDTO createReqVO) {
        return MaterialConvertRuleDO.builder()
                .ruleName(createReqVO.getRuleName())
                .sourceMaterialId(createReqVO.getSourceMaterialId())
                .targetMaterialId(createReqVO.getTargetMaterialId())
                .convertRatio(createReqVO.getConvertRatio())
                .convertPrice(createReqVO.getConvertPrice())
                .status(1) // 默认启用
                .description(createReqVO.getDescription())
                .attrs(createReqVO.getAttrs())
                .build();
    }

    private MaterialConvertRuleDO buildConvertRuleDO(MaterialConvertRuleUpdateReqDTO updateReqVO) {
        return MaterialConvertRuleDO.builder()
                .ruleName(updateReqVO.getRuleName())
                .sourceMaterialId(updateReqVO.getSourceMaterialId())
                .targetMaterialId(updateReqVO.getTargetMaterialId())
                .convertRatio(updateReqVO.getConvertRatio())
                .convertPrice(updateReqVO.getConvertPrice())
                .status(updateReqVO.getStatus())
                .description(updateReqVO.getDescription())
                .attrs(updateReqVO.getAttrs())
                .build();
    }

    private MaterialConvertRecordDO buildConvertRecordDO(MaterialConvertExecuteReqDTO convertReqDTO, 
            MaterialConvertRuleDO rule, Integer targetQuantity, BigDecimal totalConvertPrice, 
            boolean success, String failureReason) {
        return MaterialConvertRecordDO.builder()
                .userId(convertReqDTO.getUserId())
                .ruleId(convertReqDTO.getRuleId())
                .sourceMaterialId(rule.getSourceMaterialId())
                .targetMaterialId(rule.getTargetMaterialId())
                .sourceQuantity(convertReqDTO.getSourceQuantity())
                .targetQuantity(targetQuantity)
                .convertPrice(totalConvertPrice)
                .status(success ? 1 : 2) // 1成功 2失败
                .orderId(convertReqDTO.getOrderId())
                .reason(convertReqDTO.getReason())
                .failureReason(failureReason)
                .build();
    }

    private Integer calculateTargetQuantity(Integer sourceQuantity, BigDecimal convertRatio) {
        return convertRatio.multiply(BigDecimal.valueOf(sourceQuantity)).intValue();
    }

    private MaterialConvertRuleRespDTO convertToConvertRuleRespDTO(MaterialConvertRuleDO rule) {
        // 获取物料信息
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(
                List.of(rule.getSourceMaterialId(), rule.getTargetMaterialId()));
        MaterialDefinitionDO sourceMaterial = materialMap.get(rule.getSourceMaterialId());
        MaterialDefinitionDO targetMaterial = materialMap.get(rule.getTargetMaterialId());
        
        MaterialConvertRuleRespDTO dto = new MaterialConvertRuleRespDTO();
        dto.setId(rule.getId());
        dto.setRuleName(rule.getRuleName());
        dto.setSourceMaterialId(rule.getSourceMaterialId());
        dto.setSourceMaterialName(sourceMaterial != null ? sourceMaterial.getName() : "");
        dto.setSourceMaterialCode(sourceMaterial != null ? sourceMaterial.getCode() : "");
        dto.setTargetMaterialId(rule.getTargetMaterialId());
        dto.setTargetMaterialName(targetMaterial != null ? targetMaterial.getName() : "");
        dto.setTargetMaterialCode(targetMaterial != null ? targetMaterial.getCode() : "");
        dto.setConvertRatio(rule.getConvertRatio());
        dto.setConvertPrice(rule.getConvertPrice());
        dto.setStatus(rule.getStatus());
        dto.setDescription(rule.getDescription());
        
        return dto;
    }


    private PageResult<MaterialConvertRecordRespDTO> convertToConvertRecordRespDTOPage(PageResult<MaterialConvertRecordDO> pageResult) {
        if (pageResult == null) {
            return new PageResult<>();
        }
        
        List<MaterialConvertRecordRespDTO> dtoList = pageResult.getList().stream()
                .map(this::convertToConvertRecordRespDTO)
                .collect(Collectors.toList());
        
        return new PageResult<>(dtoList, pageResult.getTotal());
    }

    private MaterialConvertRecordRespDTO convertToConvertRecordRespDTO(MaterialConvertRecordDO record) {
        // 获取规则和物料信息
        MaterialConvertRuleDO rule = getConvertRule(record.getRuleId());
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionService.getDefinitionMap(
                List.of(record.getSourceMaterialId(), record.getTargetMaterialId()));
        MaterialDefinitionDO sourceMaterial = materialMap.get(record.getSourceMaterialId());
        MaterialDefinitionDO targetMaterial = materialMap.get(record.getTargetMaterialId());
        
        MaterialConvertRecordRespDTO dto = new MaterialConvertRecordRespDTO();
        dto.setId(record.getId());
        dto.setUserId(record.getUserId());
        dto.setRuleId(record.getRuleId());
        dto.setRuleName(rule != null ? rule.getRuleName() : "");
        dto.setSourceMaterialId(record.getSourceMaterialId());
        dto.setSourceMaterialName(sourceMaterial != null ? sourceMaterial.getName() : "");
        dto.setSourceMaterialCode(sourceMaterial != null ? sourceMaterial.getCode() : "");
        dto.setTargetMaterialId(record.getTargetMaterialId());
        dto.setTargetMaterialName(targetMaterial != null ? targetMaterial.getName() : "");
        dto.setTargetMaterialCode(targetMaterial != null ? targetMaterial.getCode() : "");
        dto.setSourceQuantity(record.getSourceQuantity());
        dto.setTargetQuantity(record.getTargetQuantity());
        dto.setConvertPrice(record.getConvertPrice());
        dto.setStatus(record.getStatus());
        dto.setStatusName(record.getStatus() == 1 ? "成功" : "失败");
        dto.setOrderId(record.getOrderId());
        dto.setReason(record.getReason());
        dto.setFailureReason(record.getFailureReason());
        dto.setCreateTime(record.getCreateTime());
        
        return dto;
    }

}