package com.hissp.distribution.module.infra.controller.app.third;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.infra.dal.dataobject.third.ThirdCodeAddressDO;
import com.hissp.distribution.module.infra.service.third.ThirdCodeAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * App - 第三方地址码表查询（首信易）
 * 说明：放在 controller.app 包下，统一由系统自动加上 "/app-api" 前缀
 */
@Tag(name = "用户 APP - 第三方地址码表")
@RestController
@RequestMapping("/infra/third-codes/address")
public class ThirdCodeAddressAppController {

    @Resource
    private ThirdCodeAddressService service;

    @GetMapping("/provinces")
    @Operation(summary = "查询省级列表")
    public CommonResult<List<ThirdCodeAddressDO>> provinces(@RequestParam(defaultValue = "PAYEASE") String provider) {
        return success(service.listProvinces(provider));
    }

    @GetMapping("/cities")
    @Operation(summary = "查询城市列表")
    public CommonResult<List<ThirdCodeAddressDO>> cities(@RequestParam String parentCode,
                                                         @RequestParam(defaultValue = "PAYEASE") String provider) {
        return success(service.listCities(provider, parentCode));
    }

    @GetMapping("/districts")
    @Operation(summary = "查询区县列表")
    public CommonResult<List<ThirdCodeAddressDO>> districts(@RequestParam String parentCode,
                                                            @RequestParam(defaultValue = "PAYEASE") String provider) {
        return success(service.listDistricts(provider, parentCode));
    }
}

