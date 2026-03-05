package com.hissp.distribution.module.material.service.definition;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.material.enums.ErrorCodeConstants.MATERIAL_CONVERT_PRICE_INVALID;
import static com.hissp.distribution.module.material.enums.ErrorCodeConstants.MATERIAL_CONVERT_TARGET_NOT_CONFIGURED;
import static com.hissp.distribution.module.material.enums.ErrorCodeConstants.MATERIAL_DEFINITION_NOT_EXISTS;
import static com.hissp.distribution.module.material.enums.ErrorCodeConstants.MATERIAL_DEFINITION_SPU_LINKED;
import static com.hissp.distribution.module.material.enums.ErrorCodeConstants.MATERIAL_DEFINITION_TYPE_NOT_ALLOW;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.util.StringUtils;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionPageReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionUpdateReqVO;
import com.hissp.distribution.module.material.convert.MaterialDefinitionConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialDefinitionMapper;
import com.hissp.distribution.module.material.enums.MaterialTypeEnum;
import com.hissp.distribution.module.material.enums.MaterialLinkModeEnum;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 物料定义 Service 实现类
 */
@Service
@Validated
@Slf4j
public class MaterialDefinitionServiceImpl implements MaterialDefinitionService {

    @Resource
    private MaterialDefinitionMapper definitionMapper;

    @Override
    public Long createDefinition(MaterialDefinitionCreateReqVO createReqVO) {
        // 转换
        MaterialDefinitionDO definition = MaterialDefinitionConvert.INSTANCE.convert(createReqVO);
        normalizeConvertConfig(definition);

        // 自动生成编码
        if (definition.getCode() == null || definition.getCode().trim().isEmpty()) {
            definition.setCode(generateMaterialCode());
        }

        // 插入
        definitionMapper.insert(definition);
        return definition.getId();
    }

    @Override
    public void updateDefinition(MaterialDefinitionUpdateReqVO updateReqVO) {
        validateDefinitionExists(updateReqVO.getId());
        MaterialDefinitionDO updateObj = MaterialDefinitionConvert.INSTANCE.convert(updateReqVO);
        normalizeConvertConfig(updateObj);
        definitionMapper.updateById(updateObj);
    }

    @Override
    public void deleteDefinition(Long id) {
        validateDefinitionExists(id);
        definitionMapper.deleteById(id);
    }

    private void validateDefinitionExists(Long id) {
        if (definitionMapper.selectById(id) == null) {
            throw exception(MATERIAL_DEFINITION_NOT_EXISTS);
        }
    }

    @Override
    public MaterialDefinitionDO getDefinition(Long id) {
        return definitionMapper.selectById(id);
    }

    @Override
    public Map<Long, MaterialDefinitionDO> getDefinitionMap(Collection<Long> ids) {
        if (org.springframework.util.CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<MaterialDefinitionDO> list = definitionMapper.selectBatchIds(ids);
        return CollectionUtils.convertMap(list, MaterialDefinitionDO::getId);
    }

    @Override
    public PageResult<MaterialDefinitionDO> getDefinitionPage(MaterialDefinitionPageReqVO pageReqVO) {
        return definitionMapper.selectPage(pageReqVO);
    }

    // ========== 新增方法实现 ==========

    @Override
    public MaterialDefinitionDO getDefinitionBySpuId(Long spuId) {
        if (spuId == null) {
            return null;
        }
        return definitionMapper.selectBySpuId(spuId);
    }

    @Override
    public MaterialDefinitionDO getDefinitionByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        return definitionMapper.selectByCode(code);
    }

    @Override
    public List<MaterialDefinitionDO> getDefinitionList(Collection<Long> ids) {
        if (org.springframework.util.CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return definitionMapper.selectBatchIds(ids);
    }

    @Override
    public boolean isSupportOutbound(Long materialId) {
        MaterialDefinitionDO definition = getDefinition(materialId);
        return definition != null && Boolean.TRUE.equals(definition.getSupportOutbound());
    }

    @Override
    public boolean isSupportConvert(Long materialId) {
        MaterialDefinitionDO definition = getDefinition(materialId);
        return definition != null && Boolean.TRUE.equals(definition.getSupportConvert());
    }

    @Override
    public List<MaterialDefinitionDO> getDefinitionListByType(Integer type) {
        if (type == null) {
            return Collections.emptyList();
        }
        return definitionMapper.selectList(MaterialDefinitionDO::getType, type);
    }

    @Override
    public List<MaterialDefinitionDO> getDefinitionListByName(String name) {
        if (!StringUtils.hasText(name)) {
            return Collections.emptyList();
        }
        return definitionMapper.selectListByNameLike(name);
    }

    // ========== SPU关联相关方法实现 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    // TODO: 添加操作日志注解
    // @LogRecord(type = MATERIAL_DEFINITION_TYPE, subType = CREATE_FINISHED_MAPPING,
    //            bizNo = "{{#materialId}}", action = Actions.CREATE_FINISHED_MAPPING)
    public Long createFinishedMaterial(@Valid MaterialDefinitionCreateReqVO createReqVO) {
        // 校验SPU是否存在且未被关联
        if (createReqVO.getSpuId() != null && isSpuLinked(createReqVO.getSpuId())) {
            throw exception(MATERIAL_DEFINITION_SPU_LINKED);
        }

        // 创建成品物料
        MaterialDefinitionDO definition = MaterialDefinitionConvert.INSTANCE.convert(createReqVO);
        definition.setType(MaterialTypeEnum.FINISHED.getType());

        // 自动生成编码
        if (definition.getCode() == null || definition.getCode().trim().isEmpty()) {
            definition.setCode(generateMaterialCode());
        }

        // 根据关联模式处理名称、图片、描述
        if (createReqVO.getLinkMode() != null) {
            if (MaterialLinkModeEnum.MAPPING.getMode().equals(createReqVO.getLinkMode())) {
                // 映射模式：前端传来的值已通过@NotBlank验证,但落库时强制设为null
                // 查询时从ProductSpu实时获取
                definition.setName(null);
                definition.setImage(null);
                definition.setDescription(null);
            } else if (MaterialLinkModeEnum.SNAPSHOT.getMode().equals(createReqVO.getLinkMode())) {
                // 快照模式：前端传来的值直接落库存储
                // 已经通过convert方法设置了name/image/description
                // TODO: 可以从SPU服务再次获取最新信息并存储快照
                // ProductSpuDO spu = productSpuService.getSpu(createReqVO.getSpuId());
                // if (spu != null) {
                //     definition.setSpuSnapshot(JSON.toJSONString(spu));
                // }
            }
        }

        normalizeConvertConfig(definition);

        definitionMapper.insert(definition);

        log.info("[MaterialDefinitionService][创建成品物料] materialId={}, spuId={}, linkMode={}",
                definition.getId(), createReqVO.getSpuId(), createReqVO.getLinkMode());

        return definition.getId();
    }

    private void normalizeConvertConfig(MaterialDefinitionDO definition) {
        if (definition == null) {
            return;
        }
        if (!Boolean.TRUE.equals(definition.getSupportConvert())) {
            definition.setConvertedSpuId(null);
            definition.setConvertPrice(null);
            return;
        }

        // 仅允许半成品配置转化
        if (!Objects.equals(definition.getType(), MaterialTypeEnum.SEMI_FINISHED.getType())) {
            throw exception(MATERIAL_DEFINITION_TYPE_NOT_ALLOW);
        }
        if (definition.getConvertedSpuId() == null) {
            throw exception(MATERIAL_CONVERT_TARGET_NOT_CONFIGURED);
        }
        Integer convertPrice = definition.getConvertPrice();
        if (convertPrice == null || convertPrice < 0) {
            throw exception(MATERIAL_CONVERT_PRICE_INVALID);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    // TODO: 添加操作日志注解
    // @LogRecord(type = MATERIAL_DEFINITION_TYPE, subType = UPDATE_SPU_SNAPSHOT,
    //            bizNo = "{{#materialId}}", action = Actions.UPDATE_SPU_SNAPSHOT)
    public void updateSpuSnapshot(Long materialId, Long operatorId) {
        MaterialDefinitionDO material = getDefinition(materialId);
        if (material == null) {
            throw exception(MATERIAL_DEFINITION_NOT_EXISTS);
        }

        // 检查是否为快照模式
        if (!MaterialLinkModeEnum.SNAPSHOT.getMode().equals(material.getLinkMode())) {
            throw exception(MATERIAL_DEFINITION_TYPE_NOT_ALLOW, "只有快照模式才能更新快照");
        }

        // TODO: 更新快照信息
        // if (material.getSpuId() != null) {
        //     ProductSpuDO spu = productSpuService.getSpu(material.getSpuId());
        //     if (spu != null) {
        //         material.setName(spu.getName());
        //         material.setImage(spu.getPicUrl());
        //         material.setDescription(spu.getDescription());
        //         String snapshot = JSON.toJSONString(spu);
        //         material.setSpuSnapshot(snapshot);
        //         definitionMapper.updateById(material);
        //     }
        // }

        log.info("[MaterialDefinitionService][更新SPU快照] materialId={}, operatorId={}",
                materialId, operatorId);
    }

    @Override
    public boolean isSpuLinked(Long spuId) {
        if (spuId == null) {
            return false;
        }
        MaterialDefinitionDO existing = definitionMapper.selectBySpuId(spuId);
        return existing != null;
    }

    @Override
    public List<MaterialDefinitionDO> getDefinitionListByTypeAndConvertStatus(Integer type, Integer convertStatus) {
        if (type == null) {
            return Collections.emptyList();
        }
        return definitionMapper.selectByTypeAndConvertStatus(type, convertStatus);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 生成物料编码
     * 格式: MER + 7位递增数字 (例如: MER0000001)
     *
     * @return 生成的物料编码
     */
    private String generateMaterialCode() {
        // 查询当前最大编码
        String maxCode = definitionMapper.selectMaxCode();

        if (maxCode == null || !maxCode.startsWith("MER")) {
            return "MER0000001";
        }

        // 提取数字部分并递增
        String numberPart = maxCode.substring(3);
        long nextNumber = Long.parseLong(numberPart) + 1;

        // 格式化为7位数字
        return String.format("MER%07d", nextNumber);
    }

}
