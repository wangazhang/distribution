package com.hissp.distribution.module.material.controller.admin.balance;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceAdjustReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalancePageReqVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceRespVO;
import com.hissp.distribution.module.material.controller.admin.balance.vo.MaterialBalanceStatRespVO;
import com.hissp.distribution.module.material.service.balance.MaterialBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料余额")
@RestController
@RequestMapping("/material/balance")
public class MaterialBalanceController {

    @Resource
    private MaterialBalanceService balanceService;

    @GetMapping("/page")
    @Operation(summary = "获得物料余额分页")
    @PreAuthorize("@ss.hasPermission('material:balance:query')")
    public CommonResult<PageResult<MaterialBalanceRespVO>> getBalancePage(@Valid MaterialBalancePageReqVO pageVO) {
        PageResult<MaterialBalanceRespVO> pageResult = balanceService.getBalancePage(pageVO);
        return success(pageResult);
    }

    @PostMapping("/adjust")
    @Operation(summary = "调整物料余额")
    @PreAuthorize("@ss.hasPermission('material:balance:adjust')")
    public CommonResult<Boolean> adjustBalance(@Valid @RequestBody MaterialBalanceAdjustReqVO adjustReqVO) {
        Long operatorId = SecurityFrameworkUtils.getLoginUserId();
        balanceService.adjustBalance(adjustReqVO, operatorId);
        return success(true);
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取物料余额统计信息")
    @PreAuthorize("@ss.hasPermission('material:balance:query')")
    public CommonResult<MaterialBalanceStatRespVO> getBalanceStatistics() {
        MaterialBalanceStatRespVO statistics = balanceService.getBalanceStatistics();
        return success(statistics);
    }

    @GetMapping("/user-materials")
    @Operation(summary = "获取指定用户的所有物料余额")
    @Parameter(name = "userId", description = "用户ID", required = true)
    @PreAuthorize("@ss.hasPermission('material:balance:query')")
    public CommonResult<PageResult<MaterialBalanceRespVO>> getUserMaterials(@RequestParam("userId") Long userId) {
        MaterialBalancePageReqVO pageReqVO = new MaterialBalancePageReqVO();
        pageReqVO.setUserId(userId);
        pageReqVO.setPageNo(1);
        pageReqVO.setPageSize(1000); // 获取所有物料
        PageResult<MaterialBalanceRespVO> pageResult = balanceService.getBalancePage(pageReqVO);
        return success(pageResult);
    }

}

