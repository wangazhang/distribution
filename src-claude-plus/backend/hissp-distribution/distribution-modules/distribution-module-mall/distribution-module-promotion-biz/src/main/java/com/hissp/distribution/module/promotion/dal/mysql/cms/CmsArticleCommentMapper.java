package com.hissp.distribution.module.promotion.dal.mysql.cms;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleCommentDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * CMS文章评论 Mapper
 *
 * @author distribution
 */
@Mapper
public interface CmsArticleCommentMapper extends BaseMapperX<CmsArticleCommentDO> {

    /**
     * 分页查询用户发表的评论列表
     *
     * @param pageParam 分页参数
     * @param userId 用户ID
     * @return 分页结果
     */
    default PageResult<CmsArticleCommentDO> selectPageByUserId(PageParam pageParam, Long userId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleCommentDO>()
                .eq(CmsArticleCommentDO::getUserId, userId)
                .eq(CmsArticleCommentDO::getVisible, true)
                .orderByDesc(CmsArticleCommentDO::getCreateTime));
    }

    /**
     * 分页查询收到的评论和回复
     * 包括：1. 我的文章收到的评论 2. 我的评论收到的回复
     *
     * @param pageParam 分页参数
     * @param userId 当前用户ID
     * @return 分页结果
     */
    default PageResult<CmsArticleCommentDO> selectPageRepliesToMe(PageParam pageParam, Long userId) {
        // 查询条件：文章作者是我 或者 父评论的作者是我
        // 注意：这里简化处理，后续在Service层关联查询
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleCommentDO>()
                .eq(CmsArticleCommentDO::getArticleAuthorId, userId)
                .ne(CmsArticleCommentDO::getUserId, userId) // 排除自己的评论
                .eq(CmsArticleCommentDO::getVisible, true)
                .orderByDesc(CmsArticleCommentDO::getCreateTime));
    }

    /**
     * 查询未读的评论和回复数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    default Long countUnreadByUserId(Long userId) {
        return selectCount(new LambdaQueryWrapperX<CmsArticleCommentDO>()
                .eq(CmsArticleCommentDO::getArticleAuthorId, userId)
                .ne(CmsArticleCommentDO::getUserId, userId)
                .eq(CmsArticleCommentDO::getReadStatus, false)
                .eq(CmsArticleCommentDO::getVisible, true));
    }

    /**
     * 根据文章ID分页查询评论列表
     *
     * @param pageParam 分页参数
     * @param articleId 文章ID
     * @param parentId 父评论ID,null表示查询所有
     * @return 分页结果
     */
    default PageResult<CmsArticleCommentDO> selectPageByArticleId(
            PageParam pageParam, Long articleId, Long parentId) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleCommentDO>()
                .eq(CmsArticleCommentDO::getArticleId, articleId)
                .eqIfPresent(CmsArticleCommentDO::getParentId, parentId)
                .eq(CmsArticleCommentDO::getVisible, true)
                .orderByDesc(CmsArticleCommentDO::getCreateTime));
    }

    /**
     * 批量标记为已读
     *
     * @param ids 评论ID列表
     * @return 更新数量
     */
    default int updateReadStatusByIds(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return update(null, new LambdaUpdateWrapper<CmsArticleCommentDO>()
                .set(CmsArticleCommentDO::getReadStatus, true)
                .in(CmsArticleCommentDO::getId, ids));
    }

    /**
     * 增加评论的回复数
     *
     * @param commentId 评论ID
     * @param increment 增量
     * @return 更新数量
     */
    default int incrementReplyCount(Long commentId, Integer increment) {
        CmsArticleCommentDO comment = selectById(commentId);
        if (comment == null) {
            return 0;
        }
        return update(null, new LambdaUpdateWrapper<CmsArticleCommentDO>()
                .set(CmsArticleCommentDO::getReplyCount,
                        Math.max(0, comment.getReplyCount() + increment))
                .eq(CmsArticleCommentDO::getId, commentId));
    }

    /**
     * 增加评论的回复数(默认+1)
     *
     * @param commentId 评论ID
     * @return 更新数量
     */
    default int incrementReplyCount(Long commentId) {
        return incrementReplyCount(commentId, 1);
    }

    /**
     * 减少评论的回复数(默认-1)
     *
     * @param commentId 评论ID
     * @return 更新数量
     */
    default int decrementReplyCount(Long commentId) {
        return incrementReplyCount(commentId, -1);
    }

    /**
     * 根据文章ID统计评论数
     *
     * @param articleId 文章ID
     * @return 评论数
     */
    default Long countByArticleId(Long articleId) {
        return selectCount(new LambdaQueryWrapperX<CmsArticleCommentDO>()
                .eq(CmsArticleCommentDO::getArticleId, articleId)
                .eq(CmsArticleCommentDO::getVisible, true));
    }

    /**
     * 删除文章的所有评论(软删除)
     *
     * @param articleId 文章ID
     */
    default void deleteByArticleId(Long articleId) {
        delete(CmsArticleCommentDO::getArticleId, articleId);
    }

}
