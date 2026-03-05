package com.hissp.distribution.module.promotion.controller.admin.cms;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionPageReqVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionRespVO;
import com.hissp.distribution.module.promotion.controller.admin.cms.vo.section.CmsSectionSaveReqVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsSectionConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import com.hissp.distribution.module.promotion.service.cms.CmsSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 管理后台 - CMS板块
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - CMS板块")
@RestController
@RequestMapping("/promotion/cms-section")
@Validated
public class CmsSectionController {

    @Resource
    private CmsSectionService cmsSectionService;

    @PostMapping("/create")
    @Operation(summary = "创建板块")
    @PreAuthorize("@ss.hasPermission('promotion:cms-section:create')")
    public CommonResult<Long> createSection(@Valid @RequestBody CmsSectionSaveReqVO createReqVO) {
        return success(cmsSectionService.createSection(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新板块")
    @PreAuthorize("@ss.hasPermission('promotion:cms-section:update')")
    public CommonResult<Boolean> updateSection(@Valid @RequestBody CmsSectionSaveReqVO updateReqVO) {
        cmsSectionService.updateSection(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除板块")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('promotion:cms-section:delete')")
    public CommonResult<Boolean> deleteSection(@RequestParam("id") Long id) {
        cmsSectionService.deleteSection(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得板块详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('promotion:cms-section:query')")
    public CommonResult<CmsSectionRespVO> getSection(@RequestParam("id") Long id) {
        CmsSectionDO section = cmsSectionService.getSection(id);
        return success(CmsSectionConvert.INSTANCE.convert(section));
    }

    @GetMapping("/page")
    @Operation(summary = "获得板块分页")
    @PreAuthorize("@ss.hasPermission('promotion:cms-section:query')")
    public CommonResult<PageResult<CmsSectionRespVO>> getSectionPage(@Valid CmsSectionPageReqVO pageVO) {
        PageResult<CmsSectionDO> pageResult = cmsSectionService.getSectionPage(pageVO);
        return success(CmsSectionConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/list")
    @Operation(summary = "获得板块列表")
    @Parameter(name = "ids", description = "编号列表", example = "1024,2048")
    @PreAuthorize("@ss.hasPermission('promotion:cms-section:query')")
    public CommonResult<List<CmsSectionRespVO>> getSectionList(@RequestParam(value = "ids", required = false) Collection<Long> ids) {
        List<CmsSectionDO> list;
        if (CollUtil.isNotEmpty(ids)) {
            list = cmsSectionService.getSectionList(ids);
        } else {
            list = cmsSectionService.getSectionList();
        }
        return success(CmsSectionConvert.INSTANCE.convertList(list));
    }

}
