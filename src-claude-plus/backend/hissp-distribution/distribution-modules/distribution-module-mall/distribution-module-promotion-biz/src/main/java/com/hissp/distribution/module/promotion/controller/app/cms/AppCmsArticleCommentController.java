package com.hissp.distribution.module.promotion.controller.app.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.comment.*;
import com.hissp.distribution.module.promotion.service.cms.CmsArticleCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - CMS文章评论
 *
 * @author 芋道源码
 */
@Tag(name = "用户端 - CMS文章评论")
@RestController
@RequestMapping("/app-api/promotion/cms-article-comment")
@Validated
public class AppCmsArticleCommentController {

    @Resource
    private CmsArticleCommentService commentService;

    @PostMapping("/create")
    @Operation(summary = "发表评论")
    public CommonResult<Long> createComment(@Valid @RequestBody AppCmsArticleCommentCreateReqVO createReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "请先登录");
        }
        Long commentId = commentService.createComment(createReqVO, userId);
        return success(commentId);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除评论")
    @Parameter(name = "id", description = "评论ID", required = true, example = "1024")
    public CommonResult<Boolean> deleteComment(@RequestParam("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "请先登录");
        }
        commentService.deleteComment(id, userId);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获取文章评论列表")
    public CommonResult<PageResult<AppCmsArticleCommentRespVO>> getCommentPage(@Valid AppCmsArticleCommentPageReqVO pageReqVO) {
        PageResult<AppCmsArticleCommentRespVO> pageResult = commentService.getCommentPage(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/my-page")
    @Operation(summary = "获取我的评论列表")
    public CommonResult<PageResult<AppCmsArticleCommentRespVO>> getMyComments(@Valid AppCmsArticleCommentPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(PageResult.empty());
        }
        pageReqVO.setUserId(userId);
        PageResult<AppCmsArticleCommentRespVO> pageResult = commentService.getMyComments(pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/replies-to-me")
    @Operation(summary = "获取收到的回复列表")
    public CommonResult<PageResult<AppCmsArticleCommentReplyToMeRespVO>> getRepliesToMe(@Valid AppCmsArticleCommentPageReqVO pageReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(PageResult.empty());
        }
        PageResult<AppCmsArticleCommentReplyToMeRespVO> pageResult = commentService.getRepliesToMe(userId, pageReqVO);
        return success(pageResult);
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读回复数量")
    public CommonResult<Long> getUnreadCount() {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(0L);
        }
        Long count = commentService.getUnreadCount(userId);
        return success(count);
    }

    @PutMapping("/mark-read")
    @Operation(summary = "标记回复为已读")
    public CommonResult<Boolean> markAsRead(@RequestBody AppCmsArticleCommentMarkReadReqVO markReadReqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return CommonResult.error(401, "请先登录");
        }
        commentService.markAsRead(markReadReqVO.getIds(), userId);
        return success(true);
    }

    @PostMapping("/like/{id}")
    @Operation(summary = "点赞评论")
    @Parameter(name = "id", description = "评论ID", required = true, example = "1024")
    public CommonResult<Boolean> likeComment(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(false);
        }
        boolean liked = commentService.toggleLike(id, userId);
        return success(liked);
    }
}
