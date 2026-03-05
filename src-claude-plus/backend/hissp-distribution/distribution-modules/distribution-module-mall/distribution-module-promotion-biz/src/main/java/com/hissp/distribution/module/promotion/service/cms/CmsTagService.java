package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagSaveReqVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsTagDO;

import jakarta.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * CMS标签 Service 接口
 *
 * @author 芋道源码
 */
public interface CmsTagService {

    /**
     * 创建标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTag(@Valid CmsTagSaveReqVO createReqVO);

    /**
     * 更新标签
     *
     * @param updateReqVO 更新信息
     */
    void updateTag(@Valid CmsTagSaveReqVO updateReqVO);

    /**
     * 删除标签
     *
     * @param id 编号
     */
    void deleteTag(Long id);

    /**
     * 获得标签
     *
     * @param id 编号
     * @return 标签
     */
    CmsTagDO getTag(Long id);

    /**
     * 获得标签分页
     *
     * @param pageReqVO 分页查询
     * @return 标签分页
     */
    PageResult<CmsTagDO> getTagPage(CmsTagPageReqVO pageReqVO);

    /**
     * 获得标签列表
     *
     * @param ids 编号列表
     * @return 标签列表
     */
    List<CmsTagDO> getTagList(Collection<Long> ids);

    /**
     * 获得标签列表
     *
     * @return 标签列表
     */
    List<CmsTagDO> getTagList();

    /**
     * 根据状态获得标签列表
     *
     * @param status 状态
     * @return 标签列表
     */
    List<CmsTagDO> getTagListByStatus(Integer status);

    /**
     * 校验标签是否存在
     *
     * @param id 编号
     */
    void validateTagExists(Long id);

    /**
     * 根据文章ID获取标签列表
     *
     * @param articleId 文章ID
     * @return 标签列表
     */
    List<CmsTagDO> getTagListByArticleId(Long articleId);

    /**
     * 根据分类ID获取标签列表
     *
     * @param categoryId 分类ID
     * @return 标签列表
     */
    List<CmsTagDO> getTagListByCategoryId(Long categoryId);

}