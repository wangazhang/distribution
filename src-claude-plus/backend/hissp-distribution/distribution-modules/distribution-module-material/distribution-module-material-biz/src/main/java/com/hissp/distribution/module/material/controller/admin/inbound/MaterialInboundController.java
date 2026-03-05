package com.hissp.distribution.module.material.controller.admin.inbound;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundApproveReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundPageReqVO;
import com.hissp.distribution.module.material.controller.admin.inbound.vo.MaterialInboundRespVO;
import com.hissp.distribution.module.material.service.inbound.MaterialInboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料入库")
@RestController
@RequestMapping("/material/inbound")
public class MaterialInboundController {

    @Resource
    private MaterialInboundService inboundService;

    @GetMapping("/page")
    @Operation(summary = "获得物料入库分页")
    @PreAuthorize("@ss.hasPermission('material:inbound:query')")
    public CommonResult<PageResult<MaterialInboundRespVO>> getInboundPage(@Valid MaterialInboundPageReqVO pageVO) {
        PageResult<MaterialInboundRespVO> pageResult = inboundService.getInboundPage(pageVO);
        return success(pageResult);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获得物料入库详情")
    @Parameter(name = "id", description = "入库单ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('material:inbound:query')")
    public CommonResult<MaterialInboundRespVO> getInbound(@PathVariable("id") Long id) {
        MaterialInboundRespVO inbound = inboundService.getInbound(id);
        return success(inbound);
    }

    @PostMapping("/create")
    @Operation(summary = "创建物料入库申请")
    @PreAuthorize("@ss.hasPermission('material:inbound:create')")
    public CommonResult<Long> createInbound(@Valid @RequestBody MaterialInboundCreateReqVO createReqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        Long inboundId = inboundService.createInbound(createReqVO, operatorId);
        return success(inboundId);
    }

    @PostMapping("/approve")
    @Operation(summary = "审核物料入库申请")
    @PreAuthorize("@ss.hasPermission('material:inbound:approve')")
    public CommonResult<Boolean> approveInbound(@Valid @RequestBody MaterialInboundApproveReqVO approveReqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        inboundService.approveInbound(approveReqVO, operatorId);
        return success(true);
    }

    @PostMapping("/complete/{id}")
    @Operation(summary = "完成物料入库")
    @Parameter(name = "id", description = "入库单ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('material:inbound:complete')")
    public CommonResult<Boolean> completeInbound(@PathVariable("id") Long id) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        inboundService.completeInbound(id, operatorId);
        return success(true);
    }

    @PostMapping("/cancel/{id}")
    @Operation(summary = "取消物料入库")
    @Parameter(name = "id", description = "入库单ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('material:inbound:cancel')")
    public CommonResult<Boolean> cancelInbound(@PathVariable("id") Long id) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        inboundService.cancelInbound(id, operatorId);
        return success(true);
    }

}