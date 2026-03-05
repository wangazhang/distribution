package com.hissp.distribution.module.infra.controller.admin.third;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.infra.dal.dataobject.third.ThirdCodeAddressDO;
import com.hissp.distribution.module.infra.service.third.ThirdCodeAddressService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/infra/third-codes/address")
public class ThirdCodeAddressController {

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

    @PostMapping("/import")
    @Operation(summary = "导入地址码表（Excel路径）")
    public CommonResult<Integer> importExcel(@RequestParam String filePath,
                                             @RequestParam(defaultValue = "PAYEASE") String provider,
                                             @RequestParam(defaultValue = "20250901") String version) {
        return success(service.importFromExcel(provider, version, filePath));
    }

    @PostMapping(value = "/import/upload", consumes = {"multipart/form-data"})
    @Operation(summary = "导入地址码表（上传Excel）")
    public CommonResult<Integer> importUpload(@RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                              @RequestParam(defaultValue = "PAYEASE") String provider,
                                              @RequestParam(defaultValue = "20250901") String version) throws Exception {
        return success(service.importFromExcel(provider, version, file.getInputStream()));
    }
}
