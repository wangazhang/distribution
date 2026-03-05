package com.hissp.distribution.module.promotion.service.cms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategoryPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategorySaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsCategoryConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleMapper;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsCategoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_CATEGORY_HAS_ARTICLES;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_CATEGORY_NOT_EXISTS;

/**
 * CMS分类 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CmsCategoryServiceImpl implements CmsCategoryService {

    @Resource
    private CmsCategoryMapper cmsCategoryMapper;

    @Resource
    private CmsArticleMapper cmsArticleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(CmsCategorySaveReqVO createReqVO) {
        // 插入
        CmsCategoryDO category = CmsCategoryConvert.INSTANCE.convert(createReqVO);
        cmsCategoryMapper.insert(category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CmsCategorySaveReqVO updateReqVO) {
        // 校验存在
        validateCategoryExists(updateReqVO.getId());
        // 更新
        CmsCategoryDO updateObj = CmsCategoryConvert.INSTANCE.convert(updateReqVO);
        cmsCategoryMapper.updateById(updateObj);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 校验存在
        validateCategoryExists(id);
        // 校验是否有文章
        if (CollUtil.isNotEmpty(cmsArticleMapper.selectListByCategoryId(id))) {
            throw exception(CMS_CATEGORY_HAS_ARTICLES);
        }
        // 删除
        cmsCategoryMapper.deleteById(id);
    }

    @Override
    public CmsCategoryDO getCategory(Long id) {
        return cmsCategoryMapper.selectById(id);
    }

    @Override
    public PageResult<CmsCategoryDO> getCategoryPage(CmsCategoryPageReqVO pageReqVO) {
        return cmsCategoryMapper.selectPage(pageReqVO.getPageNo(), pageReqVO.getPageSize(),
                pageReqVO.getSectionId(), pageReqVO.getName(), pageReqVO.getStatus());
    }

    @Override
    public List<CmsCategoryDO> getCategoryList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return cmsCategoryMapper.selectBatchIds(ids);
    }

    @Override
    public List<CmsCategoryDO> getCategoryList() {
        return cmsCategoryMapper.selectList();
    }

    @Override
    public List<CmsCategoryDO> getCategoryListBySectionId(Long sectionId) {
        return cmsCategoryMapper.selectListBySectionId(sectionId);
    }

    @Override
    public List<CmsCategoryDO> getCategoryListBySectionIdAndStatus(Long sectionId, Integer status) {
        return cmsCategoryMapper.selectListBySectionIdAndStatus(sectionId, status);
    }

    @Override
    public void validateCategoryExists(Long id) {
        if (cmsCategoryMapper.selectById(id) == null) {
            throw exception(CMS_CATEGORY_NOT_EXISTS);
        }
    }

}
