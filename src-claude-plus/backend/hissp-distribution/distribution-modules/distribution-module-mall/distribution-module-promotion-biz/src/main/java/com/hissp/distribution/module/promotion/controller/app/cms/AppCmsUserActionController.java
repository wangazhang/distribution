package com.hissp.distribution.module.promotion.controller.app.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsUserActionReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsUserActionRespVO;
import com.hissp.distribution.module.promotion.service.cms.CmsUserActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - CMS用户行为
 *
 * @author 芋道源码
 */
@Tag(name = "用户端 - CMS用户行为")
@RestController
@RequestMapping("/promotion/cms-user-action")
@Validated
public class AppCmsUserActionController {

    @Resource
    private CmsUserActionService cmsUserActionService;

    @PostMapping("/like")
    @Operation(summary = "点赞文章")
    public CommonResult<Boolean> likeArticle(@Valid @RequestBody AppCmsUserActionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        cmsUserActionService.likeArticle(userId, reqVO.getArticleId());
        return success(true);
    }

    @DeleteMapping("/unlike")
    @Operation(summary = "取消点赞")
    public CommonResult<Boolean> unlikeArticle(@Valid @RequestBody AppCmsUserActionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        cmsUserActionService.unlikeArticle(userId, reqVO.getArticleId());
        return success(true);
    }

    @PostMapping("/collect")
    @Operation(summary = "收藏文章")
    public CommonResult<Boolean> collectArticle(@Valid @RequestBody AppCmsUserActionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        cmsUserActionService.collectArticle(userId, reqVO.getArticleId());
        return success(true);
    }

    @DeleteMapping("/uncollect")
    @Operation(summary = "取消收藏")
    public CommonResult<Boolean> uncollectArticle(@Valid @RequestBody AppCmsUserActionReqVO reqVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        cmsUserActionService.uncollectArticle(userId, reqVO.getArticleId());
        return success(true);
    }

    @GetMapping("/my-page")
    @Operation(summary = "查询我的点赞/收藏列表")
    @Parameter(name = "actionType", description = "行为类型: like-点赞, collect-收藏", required = true)
    public CommonResult<PageResult<AppCmsUserActionRespVO>> getMyActionPage(
            @RequestParam("actionType") String actionType,
            @Valid PageParam pageParam) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<AppCmsUserActionRespVO> pageResult = cmsUserActionService.getUserActionDetailPage(userId, actionType, pageParam);
        return success(pageResult);
    }

    @GetMapping("/received-page")
    @Operation(summary = "查询针对我的点赞/收藏列表")
    @Parameter(name = "actionType", description = "行为类型: like-点赞, collect-收藏", required = true)
    public CommonResult<PageResult<AppCmsUserActionRespVO>> getReceivedActionPage(
            @RequestParam("actionType") String actionType,
            @Valid PageParam pageParam) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        PageResult<AppCmsUserActionRespVO> pageResult = cmsUserActionService.getReceivedActionDetailPage(userId, actionType, pageParam);
        return success(pageResult);
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否已点赞/收藏")
    @Parameter(name = "articleId", description = "文章编号", required = true, example = "1024")
    @Parameter(name = "actionType", description = "行为类型: like-点赞, collect-收藏", required = true)
    public CommonResult<Boolean> checkUserAction(
            @RequestParam("articleId") Long articleId,
            @RequestParam("actionType") String actionType) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        boolean hasAction = cmsUserActionService.hasUserAction(userId, articleId, actionType);
        return success(hasAction);
    }

}
