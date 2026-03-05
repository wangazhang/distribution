package com.hissp.distribution.module.promotion.controller.app.cms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.promotion.controller.app.cms.vo.AppCmsSectionRespVO;
import com.hissp.distribution.module.promotion.convert.cms.CmsSectionConvert;
import com.hissp.distribution.module.promotion.dal.dataobject.cms.CmsSectionDO;
import com.hissp.distribution.module.promotion.service.cms.CmsSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 用户端 - CMS板块
 *
 * @author 芋道源码
 */
@Tag(name = "用户端 - CMS板块")
@RestController
@RequestMapping("/promotion/cms-section")
@Validated
public class AppCmsSectionController {

    @Resource
    private CmsSectionService cmsSectionService;

    @GetMapping("/get")
    @Operation(summary = "获得板块详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppCmsSectionRespVO> getSection(@RequestParam("id") Long id) {
        CmsSectionDO section = cmsSectionService.getSection(id);
        return success(CmsSectionConvert.INSTANCE.convertApp(section));
    }

    @GetMapping("/list")
    @Operation(summary = "获得板块列表")
    public CommonResult<List<AppCmsSectionRespVO>> getSectionList() {
        List<CmsSectionDO> list = cmsSectionService.getSectionList();
        return success(CmsSectionConvert.INSTANCE.convertAppList(list));
    }

    @GetMapping("/app/list")
    @Operation(summary = "获得用户端板块列表")
    public CommonResult<List<AppCmsSectionRespVO>> getAppSectionList() {
        List<CmsSectionDO> list = cmsSectionService.getSectionList();
        return success(CmsSectionConvert.INSTANCE.convertAppList(list));
    }

}
