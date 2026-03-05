package com.hissp.distribution.module.promotion.service.cms;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagSaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsTagConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsTagDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsTagMapper;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsCategoryTagMapper;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryTagDO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.CMS_TAG_NOT_EXISTS;

/**
 * CMS标签 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class CmsTagServiceImpl implements CmsTagService {

    @Resource
    private CmsTagMapper cmsTagMapper;

    @Resource
    private CmsCategoryTagMapper cmsCategoryTagMapper;

    @Override
    public Long createTag(CmsTagSaveReqVO createReqVO) {
        // 校验名称唯一
        validateTagNameUnique(createReqVO.getName(), null);
        // 插入
        CmsTagDO tag = CmsTagConvert.INSTANCE.convert(createReqVO);
        cmsTagMapper.insert(tag);
        // 返回
        return tag.getId();
    }

    @Override
    public void updateTag(CmsTagSaveReqVO updateReqVO) {
        // 校验存在
        validateTagExists(updateReqVO.getId());
        // 校验名称唯一
        validateTagNameUnique(updateReqVO.getName(), updateReqVO.getId());
        // 更新
        CmsTagDO updateObj = CmsTagConvert.INSTANCE.convert(updateReqVO);
        cmsTagMapper.updateById(updateObj);
    }

    private void validateTagNameUnique(String name, Long id) {
        CmsTagDO tag = cmsTagMapper.selectOne(CmsTagDO::getName, name);
        if (tag == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 id 的标签
        if (id == null) {
            throw exception(CMS_TAG_NOT_EXISTS);
        }
        if (!tag.getId().equals(id)) {
            throw exception(CMS_TAG_NOT_EXISTS);
        }
    }

    @Override
    public void deleteTag(Long id) {
        // 校验存在
        validateTagExists(id);
        // 删除
        cmsTagMapper.deleteById(id);
    }

    @Override
    public CmsTagDO getTag(Long id) {
        return cmsTagMapper.selectById(id);
    }

    @Override
    public PageResult<CmsTagDO> getTagPage(CmsTagPageReqVO pageReqVO) {
        return cmsTagMapper.selectPage(pageReqVO, new LambdaQueryWrapperX<CmsTagDO>()
                .likeIfPresent(CmsTagDO::getName, pageReqVO.getName())
                .eqIfPresent(CmsTagDO::getStatus, pageReqVO.getStatus())
                .betweenIfPresent(CmsTagDO::getCreateTime, pageReqVO.getCreateTime())
                .orderByDesc(CmsTagDO::getSort)
                .orderByDesc(CmsTagDO::getId));
    }

    @Override
    public List<CmsTagDO> getTagList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return cmsTagMapper.selectBatchIds(ids);
    }

    @Override
    public List<CmsTagDO> getTagListByStatus(Integer status) {
        return cmsTagMapper.selectListByStatus(status);
    }

    @Override
    public List<CmsTagDO> getTagList() {
        return cmsTagMapper.selectList(new LambdaQueryWrapperX<CmsTagDO>()
                .orderByAsc(CmsTagDO::getSort));
    }

    @Override
    public void validateTagExists(Long id) {
        if (cmsTagMapper.selectById(id) == null) {
            throw exception(CMS_TAG_NOT_EXISTS);
        }
    }

    @Override
    public List<CmsTagDO> getTagListByArticleId(Long articleId) {
        // TODO 芋道源码：需要实现文章标签关联查询
        return new ArrayList<>();
    }

    @Override
    public List<CmsTagDO> getTagListByCategoryId(Long categoryId) {
        // 1. 通过分类标签关联表查询关联关系
        List<CmsCategoryTagDO> categoryTags = cmsCategoryTagMapper.selectListByCategoryId(categoryId);
        if (CollUtil.isEmpty(categoryTags)) {
            return ListUtil.empty();
        }

        // 2. 提取标签ID列表
        List<Long> tagIds = CollectionUtils.convertList(categoryTags, CmsCategoryTagDO::getTagId);

        // 3. 批量查询标签
        return cmsTagMapper.selectBatchIds(tagIds);
    }

}
