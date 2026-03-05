package com.hissp.distribution.module.promotion.controller.admin.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleAuditReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleBatchAuditReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticlePageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.article.CmsArticleSaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsArticleConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsArticleDO;
import com.hissp.distribution.module.promotion.service.cms.CmsArticleService;
import com.hissp.distribution.module.promotion.service.cms.CmsArticleServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - CMS文章
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - CMS文章")
@RestController
@RequestMapping("/promotion/cms-article")
@Validated
public class CmsArticleController {

    @Resource
    private CmsArticleService cmsArticleService;

    @PostMapping("/create")
    @Operation(summary = "创建文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:create')")
    public CommonResult<Long> createArticle(@Valid @RequestBody CmsArticleSaveReqVO createReqVO) {
        return success(cmsArticleService.createArticle(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:update')")
    public CommonResult<Boolean> updateArticle(@Valid @RequestBody CmsArticleSaveReqVO updateReqVO) {
        cmsArticleService.updateArticle(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文章")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:delete')")
    public CommonResult<Boolean> deleteArticle(@RequestParam("id") Long id) {
        cmsArticleService.deleteArticle(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得文章详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:query')")
    public CommonResult<CmsArticleRespVO> getArticle(@RequestParam("id") Long id) {
        CmsArticleRespVO article =  cmsArticleService.getArticleWithTags(id);
        return success(article);
    }

    @GetMapping("/page")
    @Operation(summary = "获得文章分页")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:query')")
    public CommonResult<PageResult<CmsArticleRespVO>> getArticlePage(@Valid CmsArticlePageReqVO pageVO) {
        PageResult<CmsArticleRespVO> pageResult =cmsArticleService.getArticlePageWithTags(pageVO);
        return success(pageResult);
    }

    @PutMapping("/audit")
    @Operation(summary = "审核文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:audit')")
    public CommonResult<Boolean> auditArticle(@Valid @RequestBody CmsArticleAuditReqVO auditReqVO) {
        cmsArticleService.auditArticle(auditReqVO);
        return success(true);
    }

    @PutMapping("/publish/{id}")
    @Operation(summary = "发布文章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:publish')")
    public CommonResult<Boolean> publishArticle(@PathVariable("id") Long id) {
        cmsArticleService.publishArticle(id);
        return success(true);
    }

    @PutMapping("/unpublish/{id}")
    @Operation(summary = "下架文章")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:publish')")
    public CommonResult<Boolean> unpublishArticle(@PathVariable("id") Long id) {
        cmsArticleService.unpublishArticle(id);
        return success(true);
    }

    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:delete')")
    public CommonResult<Boolean> batchDeleteArticle(@RequestBody @Valid Collection<Long> ids) {
        cmsArticleService.batchDeleteArticle(ids);
        return success(true);
    }

    @PostMapping("/batch-audit")
    @Operation(summary = "批量审核文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:audit')")
    public CommonResult<Boolean> batchAuditArticle(@RequestBody @Valid CmsArticleBatchAuditReqVO reqVO) {
        cmsArticleService.batchAuditArticle(reqVO.getIds(), reqVO.getAuditStatus(), reqVO.getAuditRemark());
        return success(true);
    }

    @PostMapping("/batch-publish")
    @Operation(summary = "批量发布文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:publish')")
    public CommonResult<Boolean> batchPublishArticle(@RequestBody @Valid Collection<Long> ids) {
        cmsArticleService.batchPublishArticle(ids);
        return success(true);
    }

    @PostMapping("/batch-unpublish")
    @Operation(summary = "批量下架文章")
    @PreAuthorize("@ss.hasPermission('promotion:cms-article:publish')")
    public CommonResult<Boolean> batchUnpublishArticle(@RequestBody @Valid Collection<Long> ids) {
        cmsArticleService.batchUnpublishArticle(ids);
        return success(true);
    }

}
