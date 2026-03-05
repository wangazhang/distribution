package com.hissp.distribution.module.promotion.controller.admin.cms;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategoryPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategoryRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.category.CmsCategorySaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsCategoryConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryDO;
import com.hissp.distribution.module.promotion.service.cms.CmsCategoryService;
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
 * 管理后台 - CMS分类
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - CMS分类")
@RestController
@RequestMapping("/promotion/cms-category")
@Validated
public class CmsCategoryController {

    @Resource
    private CmsCategoryService cmsCategoryService;

    @PostMapping("/create")
    @Operation(summary = "创建分类")
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:create')")
    public CommonResult<Long> createCategory(@Valid @RequestBody CmsCategorySaveReqVO createReqVO) {
        return success(cmsCategoryService.createCategory(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新分类")
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:update')")
    public CommonResult<Boolean> updateCategory(@Valid @RequestBody CmsCategorySaveReqVO updateReqVO) {
        cmsCategoryService.updateCategory(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除分类")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:delete')")
    public CommonResult<Boolean> deleteCategory(@RequestParam("id") Long id) {
        cmsCategoryService.deleteCategory(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得分类详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:query')")
    public CommonResult<CmsCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        CmsCategoryDO category = cmsCategoryService.getCategory(id);
        return success(CmsCategoryConvert.INSTANCE.convert(category));
    }

    @GetMapping("/page")
    @Operation(summary = "获得分类分页")
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:query')")
    public CommonResult<PageResult<CmsCategoryRespVO>> getCategoryPage(@Valid CmsCategoryPageReqVO pageVO) {
        PageResult<CmsCategoryDO> pageResult = cmsCategoryService.getCategoryPage(pageVO);
        return success(CmsCategoryConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得分类列表")
    @Parameter(name = "ids", description = "编号列表", example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:query')")
    public CommonResult<List<CmsCategoryRespVO>> getCategoryList(@RequestParam(value = "ids", required = false) List<Long> ids) {
        List<CmsCategoryDO> list;
        if (CollUtil.isNotEmpty(ids)) {
            list = cmsCategoryService.getCategoryList(ids);
        } else {
            list = cmsCategoryService.getCategoryList();
        }
        return success(CmsCategoryConvert.INSTANCE.convertList(list));
    }

    @GetMapping("/list-by-section")
    @Operation(summary = "根据板块ID获得分类列表")
    @Parameter(name = "sectionId", description = "板块编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-category:query')")
    public CommonResult<List<CmsCategoryRespVO>> getCategoryListBySectionId(@RequestParam("sectionId") Long sectionId) {
        List<CmsCategoryDO> list = cmsCategoryService.getCategoryListBySectionId(sectionId);
        return success(CmsCategoryConvert.INSTANCE.convertList(list));
    }

}
