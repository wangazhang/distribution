package com.hissp.distribution.module.material.controller.app.outbound;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.app.outbound.vo.*;
import com.hissp.distribution.module.material.service.outbound.MaterialOutboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "用户APP - 物料出库")
@RestController
@RequestMapping("/material/outbound")
public class AppMaterialOutboundController {

    @Resource
    private MaterialOutboundService outboundService;

    @PostMapping("/create")
    @Operation(summary = "创建物料出库申请")
    public CommonResult<AppMaterialOutboundCreateRespVO> createOutbound(@Valid @RequestBody AppMaterialOutboundCreateReqVO createReqVO) {
        // 设置当前用户ID
        createReqVO.setUserId(getLoginUserId());
        Long outboundId = outboundService.createAppOutbound(createReqVO).getId();
        
        AppMaterialOutboundCreateRespVO respVO = new AppMaterialOutboundCreateRespVO();
        respVO.setId(outboundId);
        return success(respVO);
    }

    @GetMapping("/get")
    @Operation(summary = "获得物料出库详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppMaterialOutboundDetailRespVO> getOutbound(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        return success(outboundService.getAppOutboundDetail(id, userId));
    }

    @GetMapping("/page")
    @Operation(summary = "获得物料出库分页")
    public CommonResult<PageResult<AppMaterialOutboundRespVO>> getOutboundPage(@Valid AppMaterialOutboundPageReqVO pageVO) {
        // 设置当前用户ID
        pageVO.setUserId(getLoginUserId());
        PageResult<AppMaterialOutboundRespVO> pageResult = outboundService.getAppOutboundPage(pageVO);
        return success(pageResult);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消物料出库申请")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> cancelOutbound(@RequestParam("id") Long id,
                                                @RequestParam("reason") String reason) {
        Long userId = getLoginUserId();
        outboundService.appCancelOutbound(id, reason, userId);
        return success(true);
    }

    @PutMapping("/complete")
    @Operation(summary = "确认收货完成")
    @Parameter(name = "id", description = "编号", required = true)
    public CommonResult<Boolean> completeOutbound(@RequestParam("id") Long id) {
        Long userId = getLoginUserId();
        outboundService.appCompleteOutbound(id, userId);
        return success(true);
    }

}