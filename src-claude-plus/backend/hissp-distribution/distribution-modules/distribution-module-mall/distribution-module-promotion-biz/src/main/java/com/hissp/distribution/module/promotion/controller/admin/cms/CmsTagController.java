package com.hissp.distribution.module.promotion.controller.admin.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.tag.CmsTagSaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsTagConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsTagDO;
import com.hissp.distribution.module.promotion.service.cms.CmsTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - CMS标签
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - CMS标签")
@RestController
@RequestMapping("/promotion/cms-tag")
@Validated
public class CmsTagController {

    @Resource
    private CmsTagService cmsTagService;

    @PostMapping("/create")
    @Operation(summary = "创建标签")
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:create')")
    public CommonResult<Long> createTag(@Valid @RequestBody CmsTagSaveReqVO createReqVO) {
        return success(cmsTagService.createTag(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新标签")
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:update')")
    public CommonResult<Boolean> updateTag(@Valid @RequestBody CmsTagSaveReqVO updateReqVO) {
        cmsTagService.updateTag(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除标签")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:delete')")
    public CommonResult<Boolean> deleteTag(@RequestParam("id") Long id) {
        cmsTagService.deleteTag(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得标签详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:query')")
    public CommonResult<CmsTagRespVO> getTag(@RequestParam("id") Long id) {
        CmsTagDO tag = cmsTagService.getTag(id);
        return success(CmsTagConvert.INSTANCE.convert(tag));
    }

    @GetMapping("/page")
    @Operation(summary = "获得标签分页")
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:query')")
    public CommonResult<PageResult<CmsTagRespVO>> getTagPage(@Valid CmsTagPageReqVO pageVO) {
        PageResult<CmsTagDO> pageResult = cmsTagService.getTagPage(pageVO);
        return success(CmsTagConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得标签列表")
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:query')")
    public CommonResult<List<CmsTagRespVO>> getTagList() {
        List<CmsTagDO> list = cmsTagService.getTagList();
        return success(CmsTagConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-by-category")
    @Operation(summary = "根据分类ID获得标签列表")
    @Parameter(name = "categoryId", description = "分类编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-tag:query')")
    public CommonResult<List<CmsTagRespVO>> getTagListByCategoryId(@RequestParam("categoryId") Long categoryId) {
        List<CmsTagDO> list = cmsTagService.getTagListByCategoryId(categoryId);
        return success(CmsTagConvert.INSTANCE.convertList(list));
    }

}
