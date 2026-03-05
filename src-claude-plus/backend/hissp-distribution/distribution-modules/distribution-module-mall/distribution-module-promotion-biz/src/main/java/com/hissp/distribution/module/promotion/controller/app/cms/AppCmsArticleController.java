package com.hissp.distribution.module.promotion.controller.app.cms;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticleDetailRespVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticlePageReqVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticleRespVO;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsArticlePublishReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsArticleConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.service.cms.CmsArticleService;
import com.hissp.distribution.module.promotion.service.cms.CmsSectionService;
import com.hissp.distribution.module.system.api.user.AdminUserApi;
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
 * 用户端 - CMS文章
 *
 * @author 芋道源码
 */
@Tag(name = "用户端 - CMS文章")
@RestController
@RequestMapping("/promotion/cms-article")
@Validated
public class AppCmsArticleController {

    @Resource
    private CmsArticleService cmsArticleService;

    @Resource
    private CmsSectionService cmsSectionService;

    @Resource
    private AdminUserApi adminUserApi;

    @Resource
    private MemberUserApi memberUserApi;

    @Resource
    private com.hissp.distribution.module.promotion.service.cms.CmsUserActionService cmsUserActionService;

    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/page")
    @Operation(summary = "获得文章分页")
    public CommonResult<PageResult<AppCmsArticleRespVO>> getArticlePage(@Valid AppCmsArticlePageReqVO pageVO) {
        PageResult<CmsArticleDO> pageResult = cmsArticleService.getAppArticlePage(pageVO);
        // 设置依赖服务
        CmsArticleConvert.INSTANCE.cmsSectionService = cmsSectionService;
        CmsArticleConvert.INSTANCE.adminUserApi = adminUserApi;
        CmsArticleConvert.INSTANCE.memberUserApi = memberUserApi;
        CmsArticleConvert.INSTANCE.cmsUserActionService = cmsUserActionService;
        CmsArticleConvert.INSTANCE.productSpuApi = productSpuApi;
        return success(CmsArticleConvert.INSTANCE.convertAppPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCmsArticleDetailRespVO> getArticle(@RequestParam("id") Long id) {
        CmsArticleDO article = cmsArticleService.getAppArticleDetail(id);
        // 设置依赖服务
        CmsArticleConvert.INSTANCE.cmsSectionService = cmsSectionService;
        CmsArticleConvert.INSTANCE.adminUserApi = adminUserApi;
        CmsArticleConvert.INSTANCE.memberUserApi = memberUserApi;
        CmsArticleConvert.INSTANCE.cmsUserActionService = cmsUserActionService;
        CmsArticleConvert.INSTANCE.productSpuApi = productSpuApi;
        return success(CmsArticleConvert.INSTANCE.convertAppDetail(article));
    }

    @GetMapping("/app/detail")
    @Operation(summary = "获得用户端文章详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCmsArticleDetailRespVO> getAppArticleDetail(@RequestParam("id") Long id) {
        CmsArticleDO article = cmsArticleService.getAppArticleDetail(id);
        // 设置依赖服务
        CmsArticleConvert.INSTANCE.cmsSectionService = cmsSectionService;
        CmsArticleConvert.INSTANCE.adminUserApi = adminUserApi;
        CmsArticleConvert.INSTANCE.memberUserApi = memberUserApi;
        CmsArticleConvert.INSTANCE.cmsUserActionService = cmsUserActionService;
        CmsArticleConvert.INSTANCE.productSpuApi = productSpuApi;
        return success(CmsArticleConvert.INSTANCE.convertAppDetail(article));
    }

    @PostMapping("/view/{id}")
    @Operation(summary = "记录浏览")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> viewArticle(@PathVariable("id") Long id) {
        cmsArticleService.incrementViewCount(id);
        return success(true);
    }

    @PostMapping("/share/{id}")
    @Operation(summary = "记录分享")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> shareArticle(@PathVariable("id") Long id) {
        cmsArticleService.incrementShareCount(id);
        return success(true);
    }

    @PostMapping("/like/{id}")
    @Operation(summary = "点赞/取消点赞")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> likeArticle(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(false);
        }
        boolean liked = cmsArticleService.toggleLike(id, userId);
        return success(liked);
    }

    @PostMapping("/collect/{id}")
    @Operation(summary = "收藏/取消收藏")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> collectArticle(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(false);
        }
        boolean collected = cmsArticleService.toggleCollect(id, userId);
        return success(collected);
    }

    @GetMapping("/is-liked/{id}")
    @Operation(summary = "查询是否已点赞")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> isLiked(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(false);
        }
        boolean liked = cmsArticleService.isLiked(id, userId);
        return success(liked);
    }

    @GetMapping("/is-collected/{id}")
    @Operation(summary = "查询是否已收藏")
    @Parameter(name = "id", description = "文章编号", required = true, example = "1024")
    public CommonResult<Boolean> isCollected(@PathVariable("id") Long id) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(false);
        }
        boolean collected = cmsArticleService.isCollected(id, userId);
        return success(collected);
    }

    @GetMapping("/hot-list")
    @Operation(summary = "获得热门文章列表")
    @Parameter(name = "limit", description = "数量限制", example = "10")
    @Parameter(name = "sectionId", description = "板块ID", example = "1")
    public CommonResult<List<AppCmsArticleRespVO>> getHotArticleList(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "sectionId", required = false) Long sectionId) {
        List<CmsArticleDO> list = cmsArticleService.getHotArticleList(sectionId, limit);
        // 设置依赖服务
        CmsArticleConvert.INSTANCE.cmsSectionService = cmsSectionService;
        CmsArticleConvert.INSTANCE.adminUserApi = adminUserApi;
        CmsArticleConvert.INSTANCE.memberUserApi = memberUserApi;
        CmsArticleConvert.INSTANCE.cmsUserActionService = cmsUserActionService;
        CmsArticleConvert.INSTANCE.productSpuApi = productSpuApi;
        return success(CmsArticleConvert.INSTANCE.convertAppList(list));
    }

    @GetMapping("/recommend-list")
    @Operation(summary = "获得推荐文章列表")
    @Parameter(name = "limit", description = "数量限制", example = "10")
    @Parameter(name = "sectionId", description = "板块ID", example = "1")
    public CommonResult<List<AppCmsArticleRespVO>> getRecommendArticleList(
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "sectionId", required = false) Long sectionId) {
        List<CmsArticleDO> list = cmsArticleService.getRecommendArticleList(sectionId, limit);
        // 设置依赖服务
        CmsArticleConvert.INSTANCE.cmsSectionService = cmsSectionService;
        CmsArticleConvert.INSTANCE.adminUserApi = adminUserApi;
        CmsArticleConvert.INSTANCE.memberUserApi = memberUserApi;
        CmsArticleConvert.INSTANCE.cmsUserActionService = cmsUserActionService;
        CmsArticleConvert.INSTANCE.productSpuApi = productSpuApi;
        return success(CmsArticleConvert.INSTANCE.convertAppList(list));
    }

    @PostMapping("/publish")
    @Operation(summary = "用户发布文章")
    public CommonResult<Long> publishArticle(@Valid @RequestBody AppCmsArticlePublishReqVO publishReqVO) {
        Long articleId = cmsArticleService.publishUserArticle(publishReqVO);
        return success(articleId);
    }

    @GetMapping("/my-articles/page")
    @Operation(summary = "获取我的发布文章列表")
    public CommonResult<PageResult<AppCmsArticleRespVO>> getMyArticles(@Valid AppCmsArticlePageReqVO pageVO) {
        Long userId = SecurityFrameworkUtils.getLoginUserId();
        if (userId == null) {
            return success(PageResult.empty());
        }

        // 设置只查询当前用户的文章
        pageVO.setAuthorId(userId);
        pageVO.setAuthorType("user"); // 移动端用户类型

        PageResult<CmsArticleDO> pageResult = cmsArticleService.getAppArticlePage(pageVO);
        // 设置依赖服务
        CmsArticleConvert.INSTANCE.cmsSectionService = cmsSectionService;
        CmsArticleConvert.INSTANCE.adminUserApi = adminUserApi;
        CmsArticleConvert.INSTANCE.memberUserApi = memberUserApi;
        CmsArticleConvert.INSTANCE.cmsUserActionService = cmsUserActionService;
        CmsArticleConvert.INSTANCE.productSpuApi = productSpuApi;
        return success(CmsArticleConvert.INSTANCE.convertAppPage(pageResult));
    }

}
