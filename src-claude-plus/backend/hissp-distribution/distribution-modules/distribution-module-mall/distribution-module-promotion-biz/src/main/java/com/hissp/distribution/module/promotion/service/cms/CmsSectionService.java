package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionSaveReqVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * CMS板块 Service 接口
 *
 * @author 芋道源码
 */
public interface CmsSectionService {

    /**
     * 创建板块
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createSection(@Valid CmsSectionSaveReqVO createReqVO);

    /**
     * 更新板块
     *
     * @param updateReqVO 更新信息
     */
    void updateSection(@Valid CmsSectionSaveReqVO updateReqVO);

    /**
     * 删除板块
     *
     * @param id 编号
     */
    void deleteSection(Long id);

    /**
     * 获得板块
     *
     * @param id 编号
     * @return 板块
     */
    CmsSectionDO getSection(Long id);

    /**
     * 获得板块分页
     *
     * @param pageReqVO 分页查询
     * @return 板块分页
     */
    PageResult<CmsSectionDO> getSectionPage(CmsSectionPageReqVO pageReqVO);

    /**
     * 获得板块列表
     *
     * @param ids 编号列表
     * @return 板块列表
     */
    List<CmsSectionDO> getSectionList(Collection<Long> ids);

    /**
     * 获得板块列表（所有板块）
     *
     * @return 板块列表
     */
    List<CmsSectionDO> getSectionList();

    /**
     * 校验板块是否存在
     *
     * @param id 编号
     */
    void validateSectionExists(Long id);

    /**
     * 获得启用状态的板块列表
     *
     * @return 板块列表
     */
    List<CmsSectionDO> getEnabledSectionList();

    /**
     * 检查板块是否需要审核
     *
     * @param sectionId 板块ID
     * @return 是否需要审核
     */
    boolean isAuditRequired(Long sectionId);

}
