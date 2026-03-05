package com.hissp.distribution.module.material.service.convert;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.*;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRuleDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRecordDO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

/**
 * 物料转化 Service 接口
 *
 * @author 芋道源码
 */
public interface MaterialConvertService {

    // ========== 转化规则管理 ==========

    /**
     * 创建转化规则
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createConvertRule(MaterialConvertRuleCreateReqDTO createReqVO);

    /**
     * 更新转化规则
     *
     * @param updateReqVO 更新信息
     */
    void updateConvertRule(MaterialConvertRuleUpdateReqDTO updateReqVO);

    /**
     * 删除转化规则
     *
     * @param id 编号
     */
    void deleteConvertRule(Long id);

    /**
     * 获得转化规则
     *
     * @param id 编号
     * @return 转化规则
     */
    MaterialConvertRuleDO getConvertRule(Long id);

    /**
     * 获得转化规则分页
     *
     * @param pageReqVO 分页查询
     * @return 转化规则分页
     */
    PageResult<MaterialConvertRuleDO> getConvertRulePage(MaterialConvertRulePageReqDTO pageReqVO);

    /**
     * 获得转化规则列表
     *
     * @return 转化规则列表
     */
    List<MaterialConvertRuleDO> getConvertRuleList();

    // ========== 转化执行 ==========

    /**
     * 执行物料转化
     *
     * @param convertReqDTO 转化请求
     * @return 转化结果
     */
    MaterialConvertResultRespDTO executeConvert(@Valid MaterialConvertExecuteReqDTO convertReqDTO);

    /**
     * 取消转化
     *
     * @param id 转化记录ID
     */
    void cancelConvert(Long id);

    /**
     * 获得转化记录分页
     *
     * @param pageReqDTO 分页查询
     * @return 转化记录分页
     */
    PageResult<MaterialConvertRecordRespDTO> getConvertRecordPage(MaterialConvertRecordPageReqDTO pageReqDTO);

    /**
     * 获得转化记录
     *
     * @param id 编号
     * @return 转化记录
     */
    MaterialConvertRecordDO getConvertRecord(Long id);

    // ========== API 接口方法 ==========

    /**
     * 获取可用的转化规则
     *
     * @param sourceMaterialId 源物料ID
     * @return 转化规则列表
     */
    List<MaterialConvertRuleRespDTO> getAvailableConvertRules(Long sourceMaterialId);

    /**
     * 预览转化
     *
     * @param previewReqDTO 预览请求
     * @return 预览结果
     */
    MaterialConvertPreviewRespDTO previewConvert(@Valid MaterialConvertPreviewReqDTO previewReqDTO);

    /**
     * 获取转化价格
     *
     * @param ruleIds 规则ID列表
     * @return 转化价格列表
     */
    List<MaterialConvertPriceRespDTO> getConvertPrices(List<Long> ruleIds);

    // ========== 转化价格管理 ==========

    /**
     * 设置转化价格
     *
     * @param ruleId 转化规则ID
     * @param price 转化价格
     */
    void setConvertPrice(Long ruleId, BigDecimal price);

    /**
     * 获取转化价格
     *
     * @param ruleId 转化规则ID
     * @return 转化价格
     */
    BigDecimal getConvertPrice(Long ruleId);
}