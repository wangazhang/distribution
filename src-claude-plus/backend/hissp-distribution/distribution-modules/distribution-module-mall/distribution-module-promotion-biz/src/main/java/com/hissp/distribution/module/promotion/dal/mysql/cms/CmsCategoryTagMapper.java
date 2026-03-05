package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryTagDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * CMS分类标签关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsCategoryTagMapper extends BaseMapperX<CmsCategoryTagDO> {

    /**
     * 根据分类ID查询关联的标签ID列表
     *
     * @param categoryId 分类ID
     * @return 标签ID列表
     */
    default List<CmsCategoryTagDO> selectListByCategoryId(Long categoryId) {
        return selectList(CmsCategoryTagDO::getCategoryId, categoryId);
    }

    /**
     * 根据标签ID查询关联的分类ID列表
     *
     * @param tagId 标签ID
     * @return 分类ID列表
     */
    default List<CmsCategoryTagDO> selectListByTagId(Long tagId) {
        return selectList(CmsCategoryTagDO::getTagId, tagId);
    }

    /**
     * 根据分类ID删除关联关系
     *
     * @param categoryId 分类ID
     */
    default void deleteByCategoryId(Long categoryId) {
        delete(CmsCategoryTagDO::getCategoryId, categoryId);
    }

    /**
     * 根据标签ID删除关联关系
     *
     * @param tagId 标签ID
     */
    default void deleteByTagId(Long tagId) {
        delete(CmsCategoryTagDO::getTagId, tagId);
    }

    /**
     * 根据分类ID和标签ID查询关联关系
     *
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @return 关联关系
     */
    default CmsCategoryTagDO selectByCategoryIdAndTagId(Long categoryId, Long tagId) {
        return selectOne(new LambdaQueryWrapperX<CmsCategoryTagDO>()
                .eq(CmsCategoryTagDO::getCategoryId, categoryId)
                .eq(CmsCategoryTagDO::getTagId, tagId));
    }

}
