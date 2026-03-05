package com.hissp.distribution.module.material.controller.admin.outbound;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.*;
import com.hissp.distribution.module.material.service.outbound.MaterialOutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料出库")
@RestController
@RequestMapping("/material/outbound")
public class MaterialOutboundController {

    @Resource
    private MaterialOutboundService outboundService;

    @PostMapping("/create")
    @Operation(summary = "创建物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:create')")
    public CommonResult<Long> createOutbound(@Valid @RequestBody MaterialOutboundCreateReqVO createReqVO) {
        return success(outboundService.createOutbound(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:update')")
    public CommonResult<Boolean> updateOutbound(@Valid @RequestBody MaterialOutboundUpdateReqVO updateReqVO) {
        outboundService.updateOutbound(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除物料出库申请")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('material:outbound:delete')")
    public CommonResult<Boolean> deleteOutbound(@RequestParam("id") Long id) {
        outboundService.deleteOutbound(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得物料出库详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('material:outbound:query')")
    public CommonResult<MaterialOutboundRespVO> getOutbound(@RequestParam("id") Long id) {
        return success(outboundService.getOutbound(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得物料出库分页")
    @PreAuthorize("@ss.hasPermission('material:outbound:query')")
    public CommonResult<PageResult<MaterialOutboundRespVO>> getOutboundPage(@Valid MaterialOutboundPageReqVO pageVO) {
        PageResult<MaterialOutboundRespVO> pageResult = outboundService.getOutboundPage(pageVO);
        return success(pageResult);
    }

    @PutMapping("/approve")
    @Operation(summary = "审核物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:approve')")
    public CommonResult<Boolean> approveOutbound(@RequestParam("id") Long id,
                                                 @RequestParam("approve") Boolean approve,
                                                 @RequestParam(value = "reason", required = false) String reason) {
        // 获取当前登录用户ID作为审核人ID
        Long approveUserId = 1L; // TODO: 获取当前登录用户ID
        outboundService.approveOutbound(id, approveUserId, approve, reason);
        return success(true);
    }

    @PostMapping("/batch-approve")
    @Operation(summary = "批量审核通过物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:approve')")
    public CommonResult<Boolean> batchApproveOutbound(@RequestBody MaterialOutboundBatchApproveReqVO batchApproveReqVO) {
        // 获取当前登录用户ID作为审核人ID
        Long approveUserId = 1L; // TODO: 获取当前登录用户ID
        outboundService.batchApproveOutbound(batchApproveReqVO.getIds(), approveUserId);
        return success(true);
    }

    @PutMapping("/ship")
    @Operation(summary = "发货物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:ship')")
    public CommonResult<Boolean> shipOutbound(@RequestParam("id") Long id,
                                              @RequestParam("logisticsCompany") String logisticsCompany,
                                              @RequestParam("logisticsCode") String logisticsCode) {
        outboundService.shipOutbound(id, logisticsCompany, logisticsCode);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "完成物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:complete')")
    public CommonResult<Boolean> completeOutbound(@RequestParam("id") Long id) {
        outboundService.completeOutbound(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消物料出库申请")
    @PreAuthorize("@ss.hasPermission('material:outbound:cancel')")
    public CommonResult<Boolean> cancelOutbound(@RequestParam("id") Long id,
                                                @RequestParam("reason") String reason) {
        outboundService.cancelOutbound(id, reason);
        return success(true);
    }

    @GetMapping("/export-shipping-template")
    @Operation(summary = "导出待发货物料出库单模板")
    @PreAuthorize("@ss.hasPermission('material:outbound:export')")
    public void exportShippingTemplate(HttpServletResponse response) throws IOException {
        outboundService.exportShippingTemplate(response);
    }

    @PostMapping("/import-shipping")
    @Operation(summary = "批量导入物料出库发货信息")
    @PreAuthorize("@ss.hasPermission('material:outbound:ship')")
    public CommonResult<Map<String, Object>> importShipping(@RequestParam("file") MultipartFile file) throws IOException {
        Map<String, Object> result = outboundService.importShipping(file);
        return success(result);
    }

}