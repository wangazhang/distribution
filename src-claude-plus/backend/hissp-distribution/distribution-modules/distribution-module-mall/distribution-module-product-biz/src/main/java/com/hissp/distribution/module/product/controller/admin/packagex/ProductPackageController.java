package com.hissp.distribution.module.product.controller.admin.packagex;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.product.controller.admin.packagex.vo.*;
import com.hissp.distribution.module.product.service.packagex.AdminProductPackageService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product/package")
@Validated
public class ProductPackageController {

    @Resource
    private AdminProductPackageService adminProductPackageService;

    @PostMapping("/create")
    public CommonResult<Long> create(@Valid @RequestBody ProductPackageSaveReqVO reqVO) {
        return CommonResult.success(adminProductPackageService.create(reqVO));
    }

    @PutMapping("/update")
    public CommonResult<Boolean> update(@Valid @RequestBody ProductPackageSaveReqVO reqVO) {
        adminProductPackageService.update(reqVO);
        return CommonResult.success(true);
    }

    @PutMapping("/update-status")
    public CommonResult<Boolean> updateStatus(@RequestParam("id") Long id,
                                              @RequestParam("status") Integer status) {
        adminProductPackageService.updateStatus(id, status);
        return CommonResult.success(true);
    }

    @DeleteMapping("/delete")
    public CommonResult<Boolean> delete(@RequestParam("id") Long id) {
        adminProductPackageService.delete(id);
        return CommonResult.success(true);
    }

    @GetMapping("/get")
    public CommonResult<ProductPackageRespVO> get(@RequestParam("id") Long id) {
        return CommonResult.success(adminProductPackageService.get(id));
    }

    @GetMapping("/page")
    public CommonResult<PageResult<ProductPackageRespVO>> page(@Valid ProductPackagePageReqVO reqVO) {
        return CommonResult.success(adminProductPackageService.page(reqVO));
    }

    @GetMapping("/get-by-spu")
    public CommonResult<ProductPackageRespVO> getBySpu(@RequestParam("spuId") Long spuId) {
        return CommonResult.success(adminProductPackageService.getBySpu(spuId));
    }
}

