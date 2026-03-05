package com.hissp.distribution.module.mb.adapter.controller.app.mbdt.price;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.mb.adapter.controller.app.mbdt.price.vo.AppMbPriceRespVO;
import com.hissp.distribution.module.mb.domain.service.mbdt.price.MaterialRestockPriceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;

@Tag(name = "管理后台 - 补货价格表：记录不同用户等级对应的商品补货价格")
@RestController
@RequestMapping({"/mb/material-restock", "/material/restock-price"}) // 同时支持新旧路径
@Validated
public class AppPriceController {

    @Resource
    private MaterialRestockPriceService materialRestockPriceService;
    @GetMapping("/price/{productId}")
    @Operation(summary = "获取特定产品的补货价格")
    @Parameter(name = "productId", description = "产品ID", required = true, example = "1024")
    public CommonResult<AppMbPriceRespVO> getMaterialRestockPrice(@PathVariable("productId") Long productId) {
        // 获取当前登录用户对特定产品的补货价格
        AppMbPriceRespVO price = materialRestockPriceService.getMaterialRestockPriceByUserIdAndProductId(getLoginUserId(), productId);
        return success(price);
    }
}