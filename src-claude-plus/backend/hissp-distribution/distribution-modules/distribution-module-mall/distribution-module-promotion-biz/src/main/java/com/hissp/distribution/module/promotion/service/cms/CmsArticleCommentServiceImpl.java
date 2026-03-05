package com.hissp.distribution.module.promotion.service.cms;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.comment.*;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleCommentDO;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleCommentMapper;
import com.hissp.distribution.module.promotion.dal.mysql.cms.CmsArticleMapper;
import com.hissp.distribution.module.system.api.user.AdminUserApi;
import com.hissp.distribution.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hissp.distribution.module.promotion.enums.ErrorCodeConstants.*;

/**
 * CMS文章评论 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class CmsArticleCommentServiceImpl implements CmsArticleCommentService {

    @Resource
    private CmsArticleCommentMapper commentMapper;

    @Resource
    private CmsArticleMapper cmsArticleMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createComment(AppCmsArticleCommentCreateReqVO createReqVO, Long userId) {
        // 校验文章是否存在
        CmsArticleDO article = cmsArticleMapper.selectById(createReqVO.getArticleId());
        if (article == null) {
            throw ServiceExceptionUtil.exception(CMS_ARTICLE_NOT_EXISTS);
        }

        // 如果是回复评论，校验父评论是否存在
        if (createReqVO.getParentId() != null && createReqVO.getParentId() > 0) {
            CmsArticleCommentDO parentComment = commentMapper.selectById(createReqVO.getParentId());
            if (parentComment == null) {
                throw ServiceExceptionUtil.exception(CMS_ARTICLE_COMMENT_NOT_EXISTS);
            }
            // 增加父评论的回复数
            commentMapper.incrementReplyCount(createReqVO.getParentId());
        }

        // 创建评论
        CmsArticleCommentDO comment = new CmsArticleCommentDO();
        comment.setArticleId(createReqVO.getArticleId());
        comment.setUserId(userId);
        comment.setParentId(createReqVO.getParentId() != null ? createReqVO.getParentId() : 0L);
        comment.setContent(createReqVO.getContent());
        comment.setPicUrls(createReqVO.getPicUrls());
        comment.setAnonymous(createReqVO.getAnonymous() != null ? createReqVO.getAnonymous() : false);
        comment.setVisible(true);
        comment.setReplyCount(0);
        comment.setLikeCount(0);
        comment.setReadStatus(false);
        comment.setArticleAuthorId(article.getAuthorId());
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());

        commentMapper.insert(comment);

        // 增加文章评论数
        cmsArticleMapper.incrementCommentCount(createReqVO.getArticleId());

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long id, Long userId) {
        // 校验评论是否存在
        CmsArticleCommentDO comment = commentMapper.selectById(id);
        if (comment == null) {
            throw ServiceExceptionUtil.exception(CMS_ARTICLE_COMMENT_NOT_EXISTS);
        }

        // 校验是否是本人的评论
        if (!comment.getUserId().equals(userId)) {
            throw ServiceExceptionUtil.exception(CMS_ARTICLE_COMMENT_NOT_OWNER);
        }

        // 删除评论
        commentMapper.deleteById(id);

        // 减少文章评论数
        cmsArticleMapper.decrementCommentCount(comment.getArticleId());

        // 如果是回复，减少父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentMapper.decrementReplyCount(comment.getParentId());
        }
    }

    @Override
    public PageResult<AppCmsArticleCommentRespVO> getCommentPage(AppCmsArticleCommentPageReqVO pageReqVO) {
        LambdaQueryWrapper<CmsArticleCommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(pageReqVO.getArticleId() != null, CmsArticleCommentDO::getArticleId, pageReqVO.getArticleId());
        wrapper.eq(pageReqVO.getParentId() != null, CmsArticleCommentDO::getParentId, pageReqVO.getParentId());
        wrapper.eq(CmsArticleCommentDO::getVisible, true);
        wrapper.orderByDesc(CmsArticleCommentDO::getCreateTime);

        IPage<CmsArticleCommentDO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<CmsArticleCommentDO> resultPage = commentMapper.selectPage(page, wrapper);

        return convertToCommentRespVO(resultPage);
    }

    @Override
    public PageResult<AppCmsArticleCommentRespVO> getMyComments(AppCmsArticleCommentPageReqVO pageReqVO) {
        LambdaQueryWrapper<CmsArticleCommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CmsArticleCommentDO::getUserId, pageReqVO.getUserId());
        wrapper.orderByDesc(CmsArticleCommentDO::getCreateTime);

        IPage<CmsArticleCommentDO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<CmsArticleCommentDO> resultPage = commentMapper.selectPage(page, wrapper);

        return convertToCommentRespVO(resultPage);
    }

    @Override
    public PageResult<AppCmsArticleCommentReplyToMeRespVO> getRepliesToMe(Long userId, AppCmsArticleCommentPageReqVO pageReqVO) {
        // 查询回复我的评论(parentId > 0 且 parent.userId = userId)
        LambdaQueryWrapper<CmsArticleCommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(CmsArticleCommentDO::getParentId, 0);
        wrapper.orderByDesc(CmsArticleCommentDO::getCreateTime);

        IPage<CmsArticleCommentDO> page = new Page<>(pageReqVO.getPageNo(), pageReqVO.getPageSize());
        IPage<CmsArticleCommentDO> resultPage = commentMapper.selectPage(page, wrapper);

        // 过滤出回复给当前用户的评论
        List<CmsArticleCommentDO> replies = resultPage.getRecords();
        List<Long> parentIds = replies.stream()
                .map(CmsArticleCommentDO::getParentId)
                .distinct()
                .collect(Collectors.toList());

        if (parentIds.isEmpty()) {
            return PageResult.empty();
        }

        // 查询父评论
        List<CmsArticleCommentDO> parentComments = commentMapper.selectBatchIds(parentIds);
        Map<Long, CmsArticleCommentDO> parentMap = parentComments.stream()
                .collect(Collectors.toMap(CmsArticleCommentDO::getId, c -> c));

        // 过滤出回复给当前用户的
        List<CmsArticleCommentDO> filteredReplies = replies.stream()
                .filter(reply -> {
                    CmsArticleCommentDO parent = parentMap.get(reply.getParentId());
                    return parent != null && parent.getUserId().equals(userId);
                })
                .collect(Collectors.toList());

        return convertToReplyRespVO(filteredReplies, parentMap);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        LambdaQueryWrapper<CmsArticleCommentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.gt(CmsArticleCommentDO::getParentId, 0);
        wrapper.eq(CmsArticleCommentDO::getReadStatus, false);

        List<CmsArticleCommentDO> replies = commentMapper.selectList(wrapper);

        // 查询父评论并过滤
        List<Long> parentIds = replies.stream()
                .map(CmsArticleCommentDO::getParentId)
                .distinct()
                .collect(Collectors.toList());

        if (parentIds.isEmpty()) {
            return 0L;
        }

        List<CmsArticleCommentDO> parentComments = commentMapper.selectBatchIds(parentIds);
        Map<Long, CmsArticleCommentDO> parentMap = parentComments.stream()
                .collect(Collectors.toMap(CmsArticleCommentDO::getId, c -> c));

        long count = replies.stream()
                .filter(reply -> {
                    CmsArticleCommentDO parent = parentMap.get(reply.getParentId());
                    return parent != null && parent.getUserId().equals(userId);
                })
                .count();

        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(List<Long> ids, Long userId) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<CmsArticleCommentDO> comments = commentMapper.selectBatchIds(ids);
        for (CmsArticleCommentDO comment : comments) {
            // 只能标记回复给自己的评论为已读
            if (comment.getParentId() > 0) {
                CmsArticleCommentDO parentComment = commentMapper.selectById(comment.getParentId());
                if (parentComment != null && parentComment.getUserId().equals(userId)) {
                    comment.setReadStatus(true);
                    commentMapper.updateById(comment);
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long id, Long userId) {
        // TODO: 实现点赞功能(需要单独的点赞表)
        return false;
    }

    private PageResult<AppCmsArticleCommentRespVO> convertToCommentRespVO(IPage<CmsArticleCommentDO> page) {
        List<CmsArticleCommentDO> comments = page.getRecords();
        if (comments.isEmpty()) {
            return PageResult.empty();
        }

        // 查询关联的文章信息
        List<Long> articleIds = comments.stream()
                .map(CmsArticleCommentDO::getArticleId)
                .distinct()
                .collect(Collectors.toList());
        List<CmsArticleDO> articles = cmsArticleMapper.selectBatchIds(articleIds);
        Map<Long, CmsArticleDO> articleMap = articles.stream()
                .collect(Collectors.toMap(CmsArticleDO::getId, a -> a));

        // 查询用户信息
        List<Long> userIds = comments.stream()
                .map(CmsArticleCommentDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);

        List<AppCmsArticleCommentRespVO> result = new ArrayList<>();
        for (CmsArticleCommentDO comment : comments) {
            AppCmsArticleCommentRespVO vo = new AppCmsArticleCommentRespVO();
            vo.setId(comment.getId());
            vo.setArticleId(comment.getArticleId());

            CmsArticleDO article = articleMap.get(comment.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getTitle());
                vo.setArticleSubtitle(article.getSubtitle());
                if (article.getCoverImages() != null && !article.getCoverImages().isEmpty()) {
                    vo.setArticleCover(article.getCoverImages().get(0));
                }
            }

            vo.setUserId(comment.getUserId());
            AdminUserRespDTO user = userMap.get(comment.getUserId());
            if (user != null && !comment.getAnonymous()) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatar());
            } else {
                vo.setUserNickname("匿名用户");
            }

            vo.setAnonymous(comment.getAnonymous());
            vo.setParentId(comment.getParentId());
            vo.setContent(comment.getContent());
            vo.setPicUrls(comment.getPicUrls());
            vo.setVisible(comment.getVisible());
            vo.setReplyCount(comment.getReplyCount());
            vo.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);
            vo.setCreateTime(comment.getCreateTime());

            result.add(vo);
        }

        return new PageResult<>(result, page.getTotal());
    }

    private PageResult<AppCmsArticleCommentReplyToMeRespVO> convertToReplyRespVO(
            List<CmsArticleCommentDO> replies, Map<Long, CmsArticleCommentDO> parentMap) {

        if (replies.isEmpty()) {
            return PageResult.empty();
        }

        // 查询关联的文章信息
        List<Long> articleIds = replies.stream()
                .map(CmsArticleCommentDO::getArticleId)
                .distinct()
                .collect(Collectors.toList());
        List<CmsArticleDO> articles = cmsArticleMapper.selectBatchIds(articleIds);
        Map<Long, CmsArticleDO> articleMap = articles.stream()
                .collect(Collectors.toMap(CmsArticleDO::getId, a -> a));

        // 查询回复者信息
        List<Long> replierIds = replies.stream()
                .map(CmsArticleCommentDO::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(replierIds);

        List<AppCmsArticleCommentReplyToMeRespVO> result = new ArrayList<>();
        for (CmsArticleCommentDO reply : replies) {
            AppCmsArticleCommentReplyToMeRespVO vo = new AppCmsArticleCommentReplyToMeRespVO();
            vo.setId(reply.getId());
            vo.setArticleId(reply.getArticleId());

            CmsArticleDO article = articleMap.get(reply.getArticleId());
            if (article != null) {
                vo.setArticleTitle(article.getTitle());
                vo.setArticleSubtitle(article.getSubtitle());
                if (article.getCoverImages() != null && !article.getCoverImages().isEmpty()) {
                    vo.setArticleCover(article.getCoverImages().get(0));
                }
            }

            vo.setReplierId(reply.getUserId());
            AdminUserRespDTO replier = userMap.get(reply.getUserId());
            if (replier != null && !reply.getAnonymous()) {
                vo.setReplierName(replier.getNickname());
                vo.setReplierAvatar(replier.getAvatar());
                // TODO: 判断是否官方用户
                vo.setReplierIsOfficial(false);
            } else {
                vo.setReplierName("匿名用户");
                vo.setReplierIsOfficial(false);
            }

            vo.setContent(reply.getContent());
            vo.setPicUrls(reply.getPicUrls());

            // 设置原评论内容
            CmsArticleCommentDO parentComment = parentMap.get(reply.getParentId());
            if (parentComment != null) {
                vo.setOriginalContent(parentComment.getContent());
            }

            vo.setCreateTime(reply.getCreateTime());
            vo.setReadStatus(reply.getReadStatus());

            result.add(vo);
        }

        return new PageResult<>(result, (long) result.size());
    }
}
