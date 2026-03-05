package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategoryPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategorySaveReqVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * CMS分类 Service 接口
 *
 * @author 芋道源码
 */
public interface CmsCategoryService {

    /**
     * 创建分类
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCategory(@Valid CmsCategorySaveReqVO createReqVO);

    /**
     * 更新分类
     *
     * @param updateReqVO 更新信息
     */
    void updateCategory(@Valid CmsCategorySaveReqVO updateReqVO);

    /**
     * 删除分类
     *
     * @param id 编号
     */
    void deleteCategory(Long id);

    /**
     * 获得分类
     *
     * @param id 编号
     * @return 分类
     */
    CmsCategoryDO getCategory(Long id);

    /**
     * 获得分类分页
     *
     * @param pageReqVO 分页查询
     * @return 分类分页
     */
    PageResult<CmsCategoryDO> getCategoryPage(CmsCategoryPageReqVO pageReqVO);

    /**
     * 获得分类列表
     *
     * @param ids 编号列表
     * @return 分类列表
     */
    List<CmsCategoryDO> getCategoryList(Collection<Long> ids);

    /**
     * 获得分类列表
     *
     * @return 分类列表
     */
    List<CmsCategoryDO> getCategoryList();

    /**
     * 根据板块ID获得分类列表
     *
     * @param sectionId 板块ID
     * @return 分类列表
     */
    List<CmsCategoryDO> getCategoryListBySectionId(Long sectionId);

    /**
     * 根据板块ID和状态获得分类列表
     *
     * @param sectionId 板块ID
     * @param status 状态
     * @return 分类列表
     */
    List<CmsCategoryDO> getCategoryListBySectionIdAndStatus(Long sectionId, Integer status);

    /**
     * 校验分类是否存在
     *
     * @param id 编号
     */
    void validateCategoryExists(Long id);

}
