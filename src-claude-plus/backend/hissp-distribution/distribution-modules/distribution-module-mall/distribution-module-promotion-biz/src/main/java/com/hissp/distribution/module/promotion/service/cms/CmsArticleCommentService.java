package com.hissp.distribution.module.promotion.service.cms;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.comment.*;

import java.util.List;

/**
 * CMS文章评论 Service 接口
 *
 * @author 芋道源码
 */
public interface CmsArticleCommentService {

    /**
     * 创建评论
     *
     * @param createReqVO 创建信息
     * @param userId 用户ID
     * @return 评论ID
     */
    Long createComment(AppCmsArticleCommentCreateReqVO createReqVO, Long userId);

    /**
     * 删除评论
     *
     * @param id 评论ID
     * @param userId 用户ID
     */
    void deleteComment(Long id, Long userId);

    /**
     * 获取文章评论列表
     *
     * @param pageReqVO 分页查询
     * @return 评论分页结果
     */
    PageResult<AppCmsArticleCommentRespVO> getCommentPage(AppCmsArticleCommentPageReqVO pageReqVO);

    /**
     * 获取我的评论列表
     *
     * @param pageReqVO 分页查询
     * @return 评论分页结果
     */
    PageResult<AppCmsArticleCommentRespVO> getMyComments(AppCmsArticleCommentPageReqVO pageReqVO);

    /**
     * 获取收到的回复列表
     *
     * @param userId 用户ID
     * @param pageReqVO 分页查询
     * @return 回复分页结果
     */
    PageResult<AppCmsArticleCommentReplyToMeRespVO> getRepliesToMe(Long userId, AppCmsArticleCommentPageReqVO pageReqVO);

    /**
     * 获取未读回复数量
     *
     * @param userId 用户ID
     * @return 未读数量
     */
    Long getUnreadCount(Long userId);

    /**
     * 标记回复为已读
     *
     * @param ids 评论ID列表
     * @param userId 用户ID
     */
    void markAsRead(List<Long> ids, Long userId);

    /**
     * 点赞/取消点赞评论
     *
     * @param id 评论ID
     * @param userId 用户ID
     * @return true=已点赞, false=已取消点赞
     */
    boolean toggleLike(Long id, Long userId);
}
