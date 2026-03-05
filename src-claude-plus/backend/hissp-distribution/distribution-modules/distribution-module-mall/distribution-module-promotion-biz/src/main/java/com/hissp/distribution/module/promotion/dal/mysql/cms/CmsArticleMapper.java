package com.hissp.distribution.module.promotion.dal.mysql.cms;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * CMS文章 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface CmsArticleMapper extends BaseMapperX<CmsArticleDO> {

    /**
     * 根据板块ID查询文章列表
     *
     * @param sectionId 板块ID
     * @return 文章列表
     */
    default List<CmsArticleDO> selectListBySectionId(Long sectionId) {
        return selectList(CmsArticleDO::getSectionId, sectionId);
    }

    /**
     * 根据分类ID查询文章列表
     *
     * @param categoryId 分类ID
     * @return 文章列表
     */
    default List<CmsArticleDO> selectListByCategoryId(Long categoryId) {
        return selectList(CmsArticleDO::getCategoryId, categoryId);
    }

    /**
     * 根据作者查询文章列表
     *
     * @param authorType 作者类型
     * @param authorId 作者ID
     * @return 文章列表
     */
    default List<CmsArticleDO> selectListByAuthor(String authorType, Long authorId) {
        return selectList(new LambdaQueryWrapperX<CmsArticleDO>()
                .eq(CmsArticleDO::getAuthorType, authorType)
                .eq(CmsArticleDO::getAuthorId, authorId)
                .orderByDesc(CmsArticleDO::getCreateTime));
    }

    /**
     * 分页查询文章列表(管理后台)
     *
     * @param pageParam 分页参数
     * @param sectionId 板块ID
     * @param categoryId 分类ID
     * @param title 标题(模糊查询)
     * @param authorType 作者类型
     * @param authorId 作者ID
     * @param auditStatus 审核状态
     * @param publishStatus 发布状态
     * @param isHot 是否热门
     * @param isOfficial 是否官方
     * @param createTime 创建时间范围
     * @return 分页结果
     */
    default PageResult<CmsArticleDO> selectPage(PageParam pageParam,
                                                  Long sectionId, Long categoryId,
                                                  String title, String authorType, Long authorId,
                                                  String auditStatus, Integer publishStatus,
                                                  Integer isHot, Integer isOfficial,
                                                  LocalDateTime[] createTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eqIfPresent(CmsArticleDO::getCategoryId, categoryId)
                .likeIfPresent(CmsArticleDO::getTitle, title)
                .eqIfPresent(CmsArticleDO::getAuthorType, authorType)
                .eqIfPresent(CmsArticleDO::getAuthorId, authorId)
                .eqIfPresent(CmsArticleDO::getAuditStatus, auditStatus)
                .eqIfPresent(CmsArticleDO::getPublishStatus, publishStatus)
                .eqIfPresent(CmsArticleDO::getIsHot, isHot)
                .eqIfPresent(CmsArticleDO::getIsOfficial, isOfficial)
                .betweenIfPresent(CmsArticleDO::getCreateTime, createTime)
                .orderByDesc(CmsArticleDO::getId));
    }

    /**
     * 分页查询文章列表(根据文章ID列表)
     *
     * @param pageParam 分页参数
     * @param articleIds 文章ID列表
     * @param sectionId 板块ID
     * @param categoryId 分类ID
     * @param title 标题(模糊查询)
     * @param authorType 作者类型
     * @param authorId 作者ID
     * @param auditStatus 审核状态
     * @param publishStatus 发布状态
     * @param isHot 是否热门
     * @param isOfficial 是否官方
     * @param createTime 创建时间范围
     * @return 分页结果
     */
    default PageResult<CmsArticleDO> selectPageByIds(PageParam pageParam,
                                                    Collection<Long> articleIds,
                                                    Long sectionId, Long categoryId,
                                                    String title, String authorType, Long authorId,
                                                    String auditStatus, Integer publishStatus,
                                                    Integer isHot, Integer isOfficial,
                                                    LocalDateTime[] createTime) {
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleDO>()
                .in(CmsArticleDO::getId, articleIds)
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eqIfPresent(CmsArticleDO::getCategoryId, categoryId)
                .likeIfPresent(CmsArticleDO::getTitle, title)
                .eqIfPresent(CmsArticleDO::getAuthorType, authorType)
                .eqIfPresent(CmsArticleDO::getAuthorId, authorId)
                .eqIfPresent(CmsArticleDO::getAuditStatus, auditStatus)
                .eqIfPresent(CmsArticleDO::getPublishStatus, publishStatus)
                .eqIfPresent(CmsArticleDO::getIsHot, isHot)
                .eqIfPresent(CmsArticleDO::getIsOfficial, isOfficial)
                .betweenIfPresent(CmsArticleDO::getCreateTime, createTime)
                .orderByDesc(CmsArticleDO::getId));
    }

    /**
     * 分页查询已发布文章列表(用户端)
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param sectionId 板块ID
     * @param categoryId 分类ID
     * @param isHot 是否热门
     * @param isOfficial 是否官方
     * @return 分页结果
     */
    default PageResult<CmsArticleDO> selectPublishedPage(Integer pageNo, Integer pageSize,
                                                           Long sectionId, Long categoryId,
                                                           Integer isHot, Integer isOfficial) {
        PageParam pageParam = new PageParam(pageNo, pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eqIfPresent(CmsArticleDO::getCategoryId, categoryId)
                .eq(CmsArticleDO::getPublishStatus, 1) // 已发布
                .eq(CmsArticleDO::getAuditStatus, "approved") // 已通过审核
                .eqIfPresent(CmsArticleDO::getIsHot, isHot)
                .eqIfPresent(CmsArticleDO::getIsOfficial, isOfficial)
                .orderByDesc(CmsArticleDO::getPublishTime));
    }

    /**
     * 分页查询已发布文章列表(用户端 - 支持标题搜索)
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param sectionId 板块ID
     * @param categoryId 分类ID
     * @param title 标题(模糊查询)
     * @param isHot 是否热门
     * @param isOfficial 是否官方
     * @return 分页结果
     */
    default PageResult<CmsArticleDO> selectPublishedPage(Integer pageNo, Integer pageSize,
                                                           Long sectionId, Long categoryId,
                                                           String title,
                                                           Integer isHot, Integer isOfficial) {
        PageParam pageParam = new PageParam(pageNo, pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eqIfPresent(CmsArticleDO::getCategoryId, categoryId)
                .likeIfPresent(CmsArticleDO::getTitle, title) // 模糊查询标题
                .eq(CmsArticleDO::getPublishStatus, 1) // 已发布
                .eq(CmsArticleDO::getAuditStatus, "approved") // 已通过审核
                .eqIfPresent(CmsArticleDO::getIsHot, isHot)
                .eqIfPresent(CmsArticleDO::getIsOfficial, isOfficial)
                .orderByDesc(CmsArticleDO::getPublishTime));
    }

    /**
     * 查询热门文章列表
     *
     * @param sectionId 板块ID(可选)
     * @param limit 数量限制
     * @return 文章列表
     */
    default List<CmsArticleDO> selectHotList(Long sectionId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eq(CmsArticleDO::getPublishStatus, 1) // 已发布
                .eq(CmsArticleDO::getAuditStatus, "approved") // 已通过审核
                .eq(CmsArticleDO::getIsHot, 1) // 热门
                .orderByDesc(CmsArticleDO::getViewCount)
                .last("LIMIT " + limit));
    }

    default List<CmsArticleDO> selectPendingListBefore(Long sectionId, LocalDateTime deadline) {
        return selectList(new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eq(CmsArticleDO::getAuditStatus, "pending")
                .eq(CmsArticleDO::getPublishStatus, 0)
                .leIfPresent(CmsArticleDO::getCreateTime, deadline));
    }

    default int updateAutoApprove(List<Long> ids, LocalDateTime publishTime, String remark) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        LambdaUpdateWrapper<CmsArticleDO> updateWrapper = new LambdaUpdateWrapper<CmsArticleDO>()
                .in(CmsArticleDO::getId, ids)
                .set(CmsArticleDO::getAuditStatus, "approved")
                .set(CmsArticleDO::getAuditRemark, remark)
                .set(CmsArticleDO::getPublishStatus, 1)
                .set(CmsArticleDO::getPublishTime, publishTime);
        return update(null, updateWrapper);
    }

    /**
     * 查询推荐文章列表(官方文章)
     *
     * @param sectionId 板块ID(可选)
     * @param limit 数量限制
     * @return 文章列表
     */
    default List<CmsArticleDO> selectRecommendList(Long sectionId, Integer limit) {
        return selectList(new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eq(CmsArticleDO::getPublishStatus, 1) // 已发布
                .eq(CmsArticleDO::getAuditStatus, "approved") // 已通过审核
                .eq(CmsArticleDO::getIsOfficial, 1) // 官方
                .orderByDesc(CmsArticleDO::getPublishTime)
                .last("LIMIT " + limit));
    }

    /**
     * 分页查询已发布文章列表(用户端 - 支持作者ID筛选)
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param sectionId 板块ID
     * @param categoryId 分类ID
     * @param title 标题(模糊查询)
     * @param authorId 作者ID
     * @param isHot 是否热门
     * @param isOfficial 是否官方
     * @return 分页结果
     */
    default PageResult<CmsArticleDO> selectPublishedPageByAuthor(Integer pageNo, Integer pageSize,
                                                                   Long sectionId, Long categoryId,
                                                                   String title, Long authorId,
                                                                   Integer isHot, Integer isOfficial) {
        PageParam pageParam = new PageParam(pageNo, pageSize);
        return selectPage(pageParam, new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eqIfPresent(CmsArticleDO::getCategoryId, categoryId)
                .likeIfPresent(CmsArticleDO::getTitle, title)
                .eqIfPresent(CmsArticleDO::getAuthorId, authorId) // 作者ID筛选
                .eq(CmsArticleDO::getPublishStatus, 1) // 已发布
                .eq(CmsArticleDO::getAuditStatus, "approved") // 已通过审核
                .eqIfPresent(CmsArticleDO::getIsHot, isHot)
                .eqIfPresent(CmsArticleDO::getIsOfficial, isOfficial)
                .orderByDesc(CmsArticleDO::getPublishTime));
    }

    /**
     * 分页查询作者文章列表(支持状态筛选)
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param sectionId 板块ID
     * @param categoryId 分类ID
     * @param title 标题(模糊查询)
     * @param authorId 作者ID
     * @param authorType 作者类型
     * @param auditStatus 审核状态
     * @param publishStatus 发布状态
     * @return 分页结果
     */
    default PageResult<CmsArticleDO> selectPageByAuthor(Integer pageNo, Integer pageSize,
                                                         Long sectionId, Long categoryId,
                                                         String title, Long authorId,
                                                         String authorType,
                                                         String auditStatus, Integer publishStatus) {
        PageParam pageParam = new PageParam(pageNo, pageSize);
        LambdaQueryWrapperX<CmsArticleDO> wrapper = new LambdaQueryWrapperX<CmsArticleDO>()
                .eqIfPresent(CmsArticleDO::getSectionId, sectionId)
                .eqIfPresent(CmsArticleDO::getCategoryId, categoryId)
                .likeIfPresent(CmsArticleDO::getTitle, title)
                .eqIfPresent(CmsArticleDO::getAuthorId, authorId) // 作者ID筛选
                .eqIfPresent(CmsArticleDO::getAuthorType, authorType); // 作者类型筛选

        // 根据状态参数进行筛选
        if (auditStatus != null) {
            wrapper.eq(CmsArticleDO::getAuditStatus, auditStatus);
        }
        if (publishStatus != null) {
            wrapper.eq(CmsArticleDO::getPublishStatus, publishStatus);
        }

        return selectPage(pageParam, wrapper
                .orderByDesc(CmsArticleDO::getCreateTime));
    }

    /**
     * 增加文章评论数
     *
     * @param articleId 文章ID
     * @return 更新数量
     */
    default int incrementCommentCount(Long articleId) {
        CmsArticleDO article = selectById(articleId);
        if (article == null) {
            return 0;
        }
        article.setCommentCount(article.getCommentCount() + 1);
        return updateById(article);
    }

    /**
     * 减少文章评论数
     *
     * @param articleId 文章ID
     * @return 更新数量
     */
    default int decrementCommentCount(Long articleId) {
        CmsArticleDO article = selectById(articleId);
        if (article == null) {
            return 0;
        }
        article.setCommentCount(Math.max(0, article.getCommentCount() - 1));
        return updateById(article);
    }

}
