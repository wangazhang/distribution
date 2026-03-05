package com.hissp.distribution.module.promotion.controller.app.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsTagRespVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsTagConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsTagDO;
import com.hissp.distribution.module.promotion.service.cms.CmsTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - CMS标签
 *
 * @author 芋道源码
 */
@Tag(name = "用户端 - CMS标签")
@RestController
@RequestMapping("/promotion/cms-tag")
@Validated
public class AppCmsTagController {

    @Resource
    private CmsTagService cmsTagService;

    @GetMapping("/list")
    @Operation(summary = "获得标签列表")
    @Parameter(name = "categoryId", description = "分类编号", required = true, example = "1")
    public CommonResult<List<AppCmsTagRespVO>> getTagList(@RequestParam("categoryId") Long categoryId) {
        List<CmsTagDO> list = cmsTagService.getTagListByCategoryId(categoryId);
        return success(CmsTagConvert.INSTANCE.convertAppList(list));
    }

    @GetMapping("/get")
    @Operation(summary = "获得标签详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCmsTagRespVO> getTag(@RequestParam("id") Long id) {
        CmsTagDO tag = cmsTagService.getTag(id);
        return success(CmsTagConvert.INSTANCE.convertApp(tag));
    }

    @GetMapping("/app/list")
    @Operation(summary = "获得用户端标签列表")
    @Parameter(name = "categoryId", description = "分类编号", required = true, example = "1")
    public CommonResult<List<AppCmsTagRespVO>> getAppTagList(@RequestParam("categoryId") Long categoryId) {
        List<CmsTagDO> list = cmsTagService.getTagListByCategoryId(categoryId);
        return success(CmsTagConvert.INSTANCE.convertAppList(list));
    }

}
