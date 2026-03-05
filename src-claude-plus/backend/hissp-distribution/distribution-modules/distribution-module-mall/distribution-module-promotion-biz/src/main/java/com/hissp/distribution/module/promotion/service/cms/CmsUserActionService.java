package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsUserActionRespVO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsUserActionDO;

/**
 * CMS用户行为 Service 接口
 *
 * @author 芋道源码
 */
public interface CmsUserActionService {

    /**
     * 点赞文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    void likeArticle(Long userId, Long articleId);

    /**
     * 取消点赞文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    void unlikeArticle(Long userId, Long articleId);

    /**
     * 收藏文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    void collectArticle(Long userId, Long articleId);

    /**
     * 取消收藏文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     */
    void uncollectArticle(Long userId, Long articleId);

    /**
     * 检查用户是否点赞了文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否点赞
     */
    boolean hasLiked(Long userId, Long articleId);

    /**
     * 检查用户是否收藏了文章
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @return 是否收藏
     */
    boolean hasCollected(Long userId, Long articleId);

    /**
     * 分页查询用户的点赞记录
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<CmsUserActionDO> getLikeRecordPage(Integer pageNo, Integer pageSize, Long userId);

    /**
     * 分页查询用户的收藏记录
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param userId 用户ID
     * @return 分页结果
     */
    PageResult<CmsUserActionDO> getCollectRecordPage(Integer pageNo, Integer pageSize, Long userId);

    /**
     * 分页查询用户的行为记录
     *
     * @param userId 用户ID
     * @param actionType 行为类型
     * @param pageParam 分页参数
     * @return 分页结果
     */
    PageResult<CmsUserActionDO> getUserActionPage(Long userId, String actionType, PageParam pageParam);

    /**
     * 查询用户对外的行为记录详情
     *
     * @param userId 用户编号
     * @param actionType 行为类型
     * @param pageParam 分页参数
     * @return 行为记录明细
     */
    PageResult<AppCmsUserActionRespVO> getUserActionDetailPage(Long userId, String actionType, PageParam pageParam);

    /**
     * 查询针对当前用户内容的行为记录详情
     *
     * @param userId 用户编号
     * @param actionType 行为类型
     * @param pageParam 分页参数
     * @return 行为记录明细
     */
    PageResult<AppCmsUserActionRespVO> getReceivedActionDetailPage(Long userId, String actionType, PageParam pageParam);

    /**
     * 检查用户是否对文章执行了某种行为
     *
     * @param userId 用户ID
     * @param articleId 文章ID
     * @param actionType 行为类型
     * @return 是否有该行为
     */
    boolean hasUserAction(Long userId, Long articleId, String actionType);

}
