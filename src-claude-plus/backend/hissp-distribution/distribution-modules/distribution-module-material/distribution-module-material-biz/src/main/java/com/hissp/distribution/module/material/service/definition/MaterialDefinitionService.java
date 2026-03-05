package com.hissp.distribution.module.material.service.definition;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionPageReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionUpdateReqVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 物料定义 Service 接口
 */
public interface MaterialDefinitionService {

    /**
     * 创建物料定义
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createDefinition(@Valid MaterialDefinitionCreateReqVO createReqVO);

    /**
     * 更新物料定义
     *
     * @param updateReqVO 更新信息
     */
    void updateDefinition(@Valid MaterialDefinitionUpdateReqVO updateReqVO);

    /**
     * 删除物料定义
     *
     * @param id 编号
     */
    void deleteDefinition(Long id);

    /**
     * 获得物料定义
     *
     * @param id 编号
     * @return 物料定义
     */
    MaterialDefinitionDO getDefinition(Long id);

    /**
     * 获得物料定义 Map
     *
     * @param ids 编号列表
     * @return 物料定义 Map
     */
    Map<Long, MaterialDefinitionDO> getDefinitionMap(Collection<Long> ids);

    /**
     * 获得物料定义分页
     *
     * @param pageReqVO 分页查询
     * @return 物料定义分页
     */
    PageResult<MaterialDefinitionDO> getDefinitionPage(MaterialDefinitionPageReqVO pageReqVO);

    // ========== 新增方法 ==========

    /**
     * 根据SPU ID获取物料定义
     *
     * @param spuId SPU ID
     * @return 物料定义
     */
    MaterialDefinitionDO getDefinitionBySpuId(Long spuId);

    /**
     * 根据物料编码获取物料定义
     *
     * @param code 物料编码
     * @return 物料定义
     */
    MaterialDefinitionDO getDefinitionByCode(String code);

    /**
     * 批量获取物料定义
     *
     * @param ids 物料ID列表
     * @return 物料定义列表
     */
    List<MaterialDefinitionDO> getDefinitionList(Collection<Long> ids);

    /**
     * 检查物料是否支持出库
     *
     * @param materialId 物料ID
     * @return 是否支持出库
     */
    boolean isSupportOutbound(Long materialId);

    /**
     * 检查物料是否支持转化
     *
     * @param materialId 物料ID
     * @return 是否支持转化
     */
    boolean isSupportConvert(Long materialId);

    /**
     * 根据类型获取物料定义列表
     *
     * @param type 物料类型
     * @return 物料定义列表
     */
    List<MaterialDefinitionDO> getDefinitionListByType(Integer type);

    /**
     * 按名称模糊搜索物料定义
     *
     * @param name 物料名称关键词
     * @return 物料定义列表
     */
    List<MaterialDefinitionDO> getDefinitionListByName(String name);

    // ========== SPU关联相关方法 ==========

    /**
     * 创建成品物料（关联SPU）
     * - 映射模式：不存储名称、图片、描述，从SPU实时获取
     * - 快照模式：从SPU复制并存储名称、图片、描述
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createFinishedMaterial(@Valid MaterialDefinitionCreateReqVO createReqVO);

    /**
     * 更新SPU快照信息（仅快照模式）
     * 从SPU重新获取信息并更新到物料定义
     *
     * @param materialId 物料ID
     * @param operatorId 操作人ID
     */
    void updateSpuSnapshot(Long materialId, Long operatorId);

    /**
     * 检查SPU是否已被其他物料关联
     *
     * @param spuId SPU ID
     * @return 是否已关联
     */
    boolean isSpuLinked(Long spuId);

    /**
     * 根据物料类型和转化状态获取物料列表
     *
     * @param type 物料类型
     * @param convertStatus 转化状态
     * @return 物料定义列表
     */
    List<MaterialDefinitionDO> getDefinitionListByTypeAndConvertStatus(Integer type, Integer convertStatus);

}
