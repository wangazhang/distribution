package com.hissp.distribution.module.promotion.controller.app.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsCategoryRespVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsCategoryConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsCategoryDO;
import com.hissp.distribution.module.promotion.service.cms.CmsCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - CMS分类
 *
 * @author 芋道源码
 */
@Tag(name = "用户端 - CMS分类")
@RestController
@RequestMapping("/promotion/cms-category")
@Validated
public class AppCmsCategoryController {

    @Resource
    private CmsCategoryService cmsCategoryService;

    @GetMapping("/list")
    @Operation(summary = "获得分类列表")
    @Parameter(name = "sectionId", description = "板块编号", required = true, example = "1")
    public CommonResult<List<AppCmsCategoryRespVO>> getCategoryList(@RequestParam("sectionId") Long sectionId) {
        List<CmsCategoryDO> list = cmsCategoryService.getCategoryListBySectionId(sectionId);
        return success(CmsCategoryConvert.INSTANCE.convertAppList(list));
    }

    @GetMapping("/get")
    @Operation(summary = "获得分类详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCmsCategoryRespVO> getCategory(@RequestParam("id") Long id) {
        CmsCategoryDO category = cmsCategoryService.getCategory(id);
        return success(CmsCategoryConvert.INSTANCE.convertApp(category));
    }

    @GetMapping("/app/list")
    @Operation(summary = "获得用户端分类列表")
    @Parameter(name = "sectionId", description = "板块编号", required = true, example = "1")
    public CommonResult<List<AppCmsCategoryRespVO>> getAppCategoryList(@RequestParam("sectionId") Long sectionId) {
        List<CmsCategoryDO> list = cmsCategoryService.getCategoryListBySectionId(sectionId);
        return success(CmsCategoryConvert.INSTANCE.convertAppList(list));
    }

}
