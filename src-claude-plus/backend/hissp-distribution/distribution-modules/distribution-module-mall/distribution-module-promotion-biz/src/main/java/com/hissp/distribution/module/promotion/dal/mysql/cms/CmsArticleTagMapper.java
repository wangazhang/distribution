package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleTagDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * CMS文章标签关联 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsArticleTagMapper extends BaseMapperX<CmsArticleTagDO> {

    /**
     * 根据文章ID查询关联的标签ID列表
     *
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    default List<CmsArticleTagDO> selectListByArticleId(Long articleId) {
        return selectList(CmsArticleTagDO::getArticleId, articleId);
    }

    /**
     * 根据标签ID查询关联的文章ID列表
     *
     * @param tagId 标签ID
     * @return 文章ID列表
     */
    default List<CmsArticleTagDO> selectListByTagId(Long tagId) {
        return selectList(CmsArticleTagDO::getTagId, tagId);
    }

    /**
     * 根据文章ID删除关联关系
     *
     * @param articleId 文章ID
     */
    default void deleteByArticleId(Long articleId) {
        delete(CmsArticleTagDO::getArticleId, articleId);
    }

    /**
     * 物理删除指定文章的全部标签关联，避免软删除历史记录导致唯一索引冲突
     *
     * @param articleId 文章ID
     */
    @Delete("DELETE FROM cms_article_tag WHERE article_id = #{articleId}")
    void deleteAllByArticleId(@Param("articleId") Long articleId);

    /**
     * 根据标签ID删除关联关系
     *
     * @param tagId 标签ID
     */
    default void deleteByTagId(Long tagId) {
        delete(CmsArticleTagDO::getTagId, tagId);
    }

    /**
     * 根据文章ID和标签ID查询关联关系
     *
     * @param articleId 文章ID
     * @param tagId 标签ID
     * @return 关联关系
     */
    default CmsArticleTagDO selectByArticleIdAndTagId(Long articleId, Long tagId) {
        return selectOne(new LambdaQueryWrapperX<CmsArticleTagDO>()
                .eq(CmsArticleTagDO::getArticleId, articleId)
                .eq(CmsArticleTagDO::getTagId, tagId));
    }

    /**
     * 批量插入文章标签关联
     *
     * @param articleTagList 文章标签关联列表
     */
    default void insertBatch(List<CmsArticleTagDO> articleTagList) {
        insertBatch(articleTagList);
    }

}
