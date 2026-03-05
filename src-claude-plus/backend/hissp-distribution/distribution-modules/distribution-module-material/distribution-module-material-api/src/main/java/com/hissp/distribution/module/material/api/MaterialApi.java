package com.hissp.distribution.module.material.api;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 物料统一服务 API
 * 
 * 提供物料相关的所有核心功能，替代 MB 域的物料操作
 */
public interface MaterialApi {

    // ========== 物料余额管理 ==========

    /**
     * 批量处理物料动作（入账/回退）
     *
     * @param acts 物料动作列表
     */
    void applyActs(@Valid List<MaterialActDTO> acts);

    /**
     * 校验指定物料动作列表是否可回退（资源充足性）
     *
     * @param acts 待回退的物料动作列表
     * @return true 如果所有物料都可回退
     */
    boolean isRevocable(@Valid List<MaterialActDTO> acts);

    /**
     * 查询用户指定物料的余额
     *
     * @param userId 用户ID
     * @param materialId 物料ID
     * @return 物料余额
     */
    MaterialBalanceRespDTO getBalance(Long userId, Long materialId);

    /**
     * 批量查询用户在指定物料集合上的余额
     * @param userId 用户ID
     * @param materialIds 物料ID集合
     * @return 余额列表
     */
    List<MaterialBalanceRespDTO> getBalances(Long userId, List<Long> materialIds);

    /**
     * 获得物料余额分页
     *
     * @param pageVO 分页查询
     * @return 物料余额分页
     */
    PageResult<MaterialBalanceRespDTO> getBalancePage(@Valid MaterialBalancePageReqDTO pageVO);

    /**
     * 获得物料流水分页
     *
     * @param pageVO 分页查询
     * @return 物料流水分页
     */
    PageResult<MaterialTxnRespDTO> getTxnPage(@Valid MaterialTxnPageReqDTO pageVO);

    // ========== 物料定义管理 ==========

    /**
     * 根据SPU ID获取关联的物料定义
     *
     * @param spuId SPU ID
     * @return 物料定义信息
     */
    MaterialDefinitionRespDTO getDefinitionBySpuId(Long spuId);

    /**
     * 批量获取物料定义信息
     *
     * @param materialIds 物料ID列表
     * @return 物料定义信息列表
     */
    List<MaterialDefinitionRespDTO> getDefinitions(List<Long> materialIds);

    /**
     * 根据名称模糊搜索物料定义
     *
     * @param name 物料名称关键字
     * @return 物料定义信息列表
     */
    List<MaterialDefinitionRespDTO> getDefinitionListByName(String name);

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

    // ========== 物料出库API ==========

    /**
     * 创建物料出库申请
     *
     * @param outboundReqDTO 出库申请信息
     * @return 出库单ID
     */
    Long createOutbound(@Valid MaterialOutboundCreateReqDTO outboundReqDTO);

    /**
     * 批量审核物料出库申请
     *
     * @param ids 出库单ID列表
     * @param approveUserId 审核人ID
     * @param approved 是否通过
     */
    void batchApproveOutbound(List<Long> ids, Long approveUserId, Boolean approved);

    /**
     * 获取用户的出库申请列表
     *
     * @param userId 用户ID
     * @param pageReqDTO 分页参数
     * @return 出库申请分页
     */
    PageResult<MaterialOutboundRespDTO> getUserOutboundPage(Long userId, @Valid MaterialOutboundPageReqDTO pageReqDTO);

    // ========== 物料转化API ==========

    /**
     * 获取可转化的物料规则
     *
     * @param sourceMaterialId 源物料ID
     * @return 可转化规则列表
     */
    List<MaterialConvertRuleRespDTO> getAvailableConvertRules(Long sourceMaterialId);

    /**
     * 预览物料转化费用
     *
     * @param previewReqDTO 转化预览请求
     * @return 转化费用预览
     */
    MaterialConvertPreviewRespDTO previewConvert(@Valid MaterialConvertPreviewReqDTO previewReqDTO);

    /**
     * 执行物料转化
     *
     * @param convertReqDTO 转化执行请求
     * @return 转化结果
     */
    MaterialConvertResultRespDTO executeConvert(@Valid MaterialConvertExecuteReqDTO convertReqDTO);

    /**
     * 获取用户的转化记录
     *
     * @param userId 用户ID
     * @param pageReqDTO 分页参数
     * @return 转化记录分页
     */
    PageResult<MaterialConvertRecordRespDTO> getUserConvertRecordPage(Long userId, @Valid MaterialConvertRecordPageReqDTO pageReqDTO);

    // ========== 物料价格管理 ==========

    /**
     * 获取物料转化价格
     *
     * @param ruleId 转化规则ID
     * @return 转化价格
     */
    BigDecimal getConvertPrice(Long ruleId);

    /**
     * 批量获取物料转化价格
     *
     * @param ruleIds 转化规则ID列表
     * @return 价格映射 (ruleId -> price)
     */
    List<MaterialConvertPriceRespDTO> getConvertPrices(List<Long> ruleIds);

}
