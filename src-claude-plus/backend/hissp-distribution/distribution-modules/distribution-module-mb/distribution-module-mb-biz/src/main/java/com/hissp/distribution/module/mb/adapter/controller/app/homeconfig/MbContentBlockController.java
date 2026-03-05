package com.hissp.distribution.module.mb.adapter.controller.app.homeconfig;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.mb.adapter.controller.app.homeconfig.vo.MbHomeConfigRespVO;
import com.hissp.distribution.module.mb.domain.service.contentblock.MbContentBlockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

/**
 * 医美内容区块 Controller
 */
@Deprecated
@Tag(name = "医美内容区块")
@RestController
@RequestMapping("/home/content-blocks")
public class MbContentBlockController {

    @Resource
    private MbContentBlockService mbContentBlockService;

    @GetMapping("/home-config")
    @Operation(summary = "获取首页配置")
    @Parameter(name = "version", description = "配置版本号，不传则获取当前生效配置")
    @PermitAll
    public CommonResult<MbHomeConfigRespVO> getHomeConfig() {
        return success(mbContentBlockService.getHomeConfig(""));
    }
}