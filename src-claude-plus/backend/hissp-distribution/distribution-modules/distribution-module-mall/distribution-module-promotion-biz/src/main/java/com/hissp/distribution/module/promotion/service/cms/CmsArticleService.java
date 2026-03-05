package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleAuditReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticlePageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleSaveReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticlePageReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticlePublishReqVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;

/**
 * CMS文章 Service 接口
 *
 * @author 芋道源码
 */
public interface CmsArticleService {

    /**
     * 创建文章
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createArticle(@Valid CmsArticleSaveReqVO createReqVO);

    /**
     * 用户发布文章(UGC)
     *
     * @param publishReqVO 发布信息
     * @return 编号
     */
    Long publishUserArticle(@Valid AppCmsArticlePublishReqVO publishReqVO);

    /**
     * 更新文章
     *
     * @param updateReqVO 更新信息
     */
    void updateArticle(@Valid CmsArticleSaveReqVO updateReqVO);

    /**
     * 删除文章
     *
     * @param id 编号
     */
    void deleteArticle(Long id);

    /**
     * 获得文章
     *
     * @param id 编号
     * @return 文章
     */
    CmsArticleDO getArticle(Long id);

    PageResult<CmsArticleRespVO> getArticlePageWithTags(CmsArticlePageReqVO pageReqVO);

    /**
     * 获得文章分页(管理后台)
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<CmsArticleDO> getArticlePage(CmsArticlePageReqVO pageReqVO);

    /**
     * 获得已发布文章分页(用户端)
     *
     * @param pageReqVO 分页查询
     * @return 文章分页
     */
    PageResult<CmsArticleDO> getPublishedArticlePage(AppCmsArticlePageReqVO pageReqVO);

    /**
     * 获得文章列表
     *
     * @param ids 编号列表
     * @return 文章列表
     */
    List<CmsArticleDO> getArticleList(Collection<Long> ids);

    /**
     * 审核文章
     *
     * @param auditReqVO 审核信息
     */
    void auditArticle(@Valid CmsArticleAuditReqVO auditReqVO);

    /**
     * 发布文章
     *
     * @param id 文章ID
     */
    void publishArticle(Long id);

    /**
     * 下架文章
     *
     * @param id 文章ID
     */
    void unpublishArticle(Long id);

    /**
     * 增加文章浏览数
     *
     * @param id 文章ID
     */
    void incrementViewCount(Long id);

    /**
     * 增加文章分享数
     *
     * @param id 文章ID
     */
    void incrementShareCount(Long id);

    /**
     * 校验文章是否存在
     *
     * @param id 编号
     */
    void validateArticleExists(Long id);

    /**
     * 获取热门文章列表
     *
     * @param sectionId 板块ID(可选)
     * @param limit     数量限制
     * @return 文章列表
     */
    List<CmsArticleDO> getHotArticleList(Long sectionId, Integer limit);

    /**
     * 获取热门文章列表(不限板块)
     *
     * @param limit 数量限制
     * @return 文章列表
     */
    List<CmsArticleDO> getHotArticleList(Integer limit);

    /**
     * 获取推荐文章列表(官方文章)
     *
     * @param sectionId 板块ID(可选)
     * @param limit     数量限制
     * @return 文章列表
     */
    List<CmsArticleDO> getRecommendArticleList(Long sectionId, Integer limit);

    /**
     * 获取推荐文章列表(不限板块)
     *
     * @param limit 数量限制
     * @return 文章列表
     */
    List<CmsArticleDO> getRecommendArticleList(Integer limit);

    /**
     * 获取用户端文章分页
     *
     * @param pageVO 分页查询参数
     * @return 分页结果
     */
    PageResult<CmsArticleDO> getAppArticlePage(AppCmsArticlePageReqVO pageVO);

    /**
     * 获取用户端文章详情
     *
     * @param id 文章ID
     * @return 文章详情
     */
    CmsArticleDO getAppArticleDetail(Long id);

    /**
     * 批量删除文章
     *
     * @param ids 文章ID列表
     */
    void batchDeleteArticle(Collection<Long> ids);

    /**
     * 批量审核文章
     *
     * @param ids         文章ID列表
     * @param auditStatus 审核状态
     * @param auditRemark 审核备注
     */
    void batchAuditArticle(Collection<Long> ids, String auditStatus, String auditRemark);

    /**
     * 批量发布文章
     *
     * @param ids 文章ID列表
     */
    void batchPublishArticle(Collection<Long> ids);

    /**
     * 批量下架文章
     *
     * @param ids 文章ID列表
     */
    void batchUnpublishArticle(Collection<Long> ids);

    /**
     * 切换文章点赞状态
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 点赞后的状态(true = 已点赞, false = 取消点赞)
     */
    boolean toggleLike(Long articleId, Long userId);

    /**
     * 切换文章收藏状态
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 收藏后的状态(true = 已收藏, false = 取消收藏)
     */
    boolean toggleCollect(Long articleId, Long userId);

    /**
     * 查询用户是否已点赞文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return true=已点赞, false=未点赞
     */
    boolean isLiked(Long articleId, Long userId);

    /**
     * 查询用户是否已收藏文章
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return true=已收藏, false=未收藏
     */
    boolean isCollected(Long articleId, Long userId);

    /**
     * 获得文章详情，包含标签
     *
     * @param id 文章ID
     * @return 文章详情
     */
    CmsArticleRespVO getArticleWithTags(Long id);

}
