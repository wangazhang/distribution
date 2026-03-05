package com.hissp.distribution.module.mb.adapter.controller.admin.trade;

import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderDetailRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderPageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderRefundReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderSaveReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.trade.vo.MbOrderUpdateStatusReqVO;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;

import jakarta.validation.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

import com.hissp.distribution.framework.excel.core.util.ExcelUtils;

import com.hissp.distribution.framework.apilog.core.annotation.ApiAccessLog;
import static com.hissp.distribution.framework.apilog.core.enums.OperateTypeEnum.*;
import com.hissp.distribution.module.mb.domain.service.trade.MbOrderService;

@Tag(name = "管理后台 - mb订单")
@RestController
@RequestMapping("/mb/order")
@Validated
public class MbOrderController {

    @Resource
    private MbOrderService mbOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建mb订单")
    @PreAuthorize("@ss.hasPermission('mb:order:create')")
    public CommonResult<Long> createMbOrder(@Valid @RequestBody MbOrderSaveReqVO createReqVO) {
        return success(mbOrderService.createMbOrder(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新mb订单")
    @PreAuthorize("@ss.hasPermission('mb:order:update')")
    public CommonResult<Boolean> updateMbOrder(@Valid @RequestBody MbOrderSaveReqVO updateReqVO) {
        mbOrderService.updateMbOrder(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除mb订单")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mb:order:delete')")
    public CommonResult<Boolean> deleteMbOrder(@RequestParam("id") Long id) {
        mbOrderService.deleteMbOrder(id);
        return success(true);
    }

    @GetMapping("/refund/config")
    @Operation(summary = "查询退款配置")
    @PreAuthorize("@ss.hasPermission('mb:order:query')")
    public CommonResult<Boolean> getRefundConfig() {
        return success(mbOrderService.isAdminRefundPasswordEnabled());
    }

    @GetMapping("/get")
    @Operation(summary = "获得mb订单")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mb:order:query')")
    public CommonResult<MbOrderDetailRespVO> getMbOrder(@RequestParam("id") Long id) {
        return success(mbOrderService.getAdminOrderDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得mb订单分页")
    @PreAuthorize("@ss.hasPermission('mb:order:query')")
    public CommonResult<PageResult<MbOrderRespVO>> getMbOrderPage(@Valid MbOrderPageReqVO pageReqVO) {
        PageResult<MbOrderRespVO> pageResult = mbOrderService.getAdminOrderPage(pageReqVO);
        return success(pageResult);
    }

    @PutMapping("/status")
    @Operation(summary = "更新订单状态")
    @PreAuthorize("@ss.hasPermission('mb:order:update-status')")
    public CommonResult<Boolean> updateOrderStatus(@Valid @RequestBody MbOrderUpdateStatusReqVO reqVO) {
        mbOrderService.adminUpdateOrderStatus(reqVO.getId(), reqVO.getStatus());
        return success(true);
    }

    @PostMapping("/{id}/virtual-delivery/retry")
    @Operation(summary = "重新触发虚拟发货")
    @Parameter(name = "id", description = "订单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mb:order:retry-virtual-delivery')")
    public CommonResult<Boolean> retryVirtualDelivery(@PathVariable("id") Long id) {
        mbOrderService.retryVirtualDelivery(id);
        return success(true);
    }

    @PostMapping("/refund")
    @Operation(summary = "发起订单退款")
    @PreAuthorize("@ss.hasPermission('mb:order:refund')")
    public CommonResult<Boolean> refundOrder(@Valid @RequestBody MbOrderRefundReqVO reqVO) {
        mbOrderService.adminRefundOrder(reqVO.getId(), reqVO.getReason(), reqVO.getPassword());
        return success(Boolean.TRUE);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出mb订单 Excel")
    @PreAuthorize("@ss.hasPermission('mb:order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMbOrderExcel(@Valid MbOrderPageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MbOrderRespVO> pageResult = mbOrderService.getAdminOrderPage(pageReqVO);
        List<MbOrderRespVO> list = pageResult.getList();
        // 导出 Excel
        ExcelUtils.write(response, "mb订单.xls", "数据", MbOrderRespVO.class,
                        list);
    }

}
