package com.hissp.distribution.module.material.controller.admin.definition;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionCreateReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionPageReqVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionRespVO;
import com.hissp.distribution.module.material.controller.admin.definition.vo.MaterialDefinitionUpdateReqVO;
import com.hissp.distribution.module.material.convert.MaterialDefinitionConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.service.definition.MaterialDefinitionService;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料定义")
@Slf4j
@RestController
@RequestMapping("/material/definition")
public class MaterialDefinitionController {

    @Resource
    private MaterialDefinitionService definitionService;

    @Resource
    private ProductSpuApi productSpuApi;

    @PostMapping("/create")
    @Operation(summary = "创建物料定义")
    @PreAuthorize("@ss.hasPermission('material:definition:create')")
    public CommonResult<Long> createDefinition(@Valid @RequestBody MaterialDefinitionCreateReqVO createReqVO) {
        return success(definitionService.createDefinition(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新物料定义")
    @PreAuthorize("@ss.hasPermission('material:definition:update')")
    public CommonResult<Boolean> updateDefinition(@Valid @RequestBody MaterialDefinitionUpdateReqVO updateReqVO) {
        definitionService.updateDefinition(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除物料定义")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('material:definition:delete')")
    public CommonResult<Boolean> deleteDefinition(@RequestParam("id") Long id) {
        definitionService.deleteDefinition(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得物料定义")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('material:definition:query')")
    public CommonResult<MaterialDefinitionRespVO> getDefinition(@RequestParam("id") Long id) {
        MaterialDefinitionDO definition = definitionService.getDefinition(id);
        MaterialDefinitionRespVO respVO = MaterialDefinitionConvert.INSTANCE.convert(definition);
        if (respVO != null) {
            enrichSpuNames(Collections.singletonList(respVO));
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得物料定义分页")
    @PreAuthorize("@ss.hasPermission('material:definition:query')")
    public CommonResult<PageResult<MaterialDefinitionRespVO>> getDefinitionPage(@Valid MaterialDefinitionPageReqVO pageVO) {
        PageResult<MaterialDefinitionDO> pageResult = definitionService.getDefinitionPage(pageVO);
        PageResult<MaterialDefinitionRespVO> voPage = MaterialDefinitionConvert.INSTANCE.convertPage(pageResult);
        if (voPage != null && voPage.getList() != null) {
            enrichSpuNames(voPage.getList());
        }
        return success(voPage);
    }

    @GetMapping("/get-spu-info")
    @Operation(summary = "根据SPU ID获取商品信息（用于自动填充）")
    @Parameter(name = "spuId", description = "SPU ID", required = true)
    @PreAuthorize("@ss.hasPermission('material:definition:query')")
    public CommonResult<MaterialDefinitionRespVO> getSpuInfo(@RequestParam("spuId") Long spuId) {
        // 从ProductSpuApi获取SPU信息
        ProductSpuRespDTO spu = productSpuApi.getSpu(spuId);
        if (spu == null) {
            return success(null);
        }

        // 构造返回对象，用于前端自动填充
        MaterialDefinitionRespVO result = new MaterialDefinitionRespVO();
        result.setName(spu.getName());
        result.setImage(spu.getPicUrl());
        result.setDescription(spu.getDescription()); // 使用真实description
        result.setSpuId(spuId);
        result.setSpuName(spu.getName());
        return success(result);
    }

    @PostMapping("/update-snapshot")
    @Operation(summary = "更新物料的SPU快照（仅快照模式）")
    @Parameter(name = "id", description = "物料ID", required = true)
    @PreAuthorize("@ss.hasPermission('material:definition:update')")
    public CommonResult<Boolean> updateSpuSnapshot(@RequestParam("id") Long id) {
        definitionService.updateSpuSnapshot(id, null);
        return success(true);
    }

    private void enrichSpuNames(List<MaterialDefinitionRespVO> definitionList) {
        if (definitionList == null || definitionList.isEmpty()) {
            return;
        }

        Set<Long> spuIds = definitionList.stream()
                .flatMap(vo -> Stream.of(vo.getSpuId(), vo.getConvertedSpuId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (spuIds.isEmpty()) {
            return;
        }

        try {
            Map<Long, ProductSpuRespDTO> spuMap = productSpuApi.getSpusMap(spuIds);
            if (spuMap == null || spuMap.isEmpty()) {
                return;
            }
            definitionList.forEach(vo -> {
                if (vo.getSpuId() != null) {
                    ProductSpuRespDTO spu = spuMap.get(vo.getSpuId());
                    if (spu != null) {
                        vo.setSpuName(spu.getName());
                    }
                }
                if (vo.getConvertedSpuId() != null) {
                    ProductSpuRespDTO targetSpu = spuMap.get(vo.getConvertedSpuId());
                    if (targetSpu != null) {
                        vo.setConvertedSpuName(targetSpu.getName());
                    }
                }
            });
        } catch (Exception ex) {
            log.warn("[enrichSpuNames][spuIds={}] 查询SPU名称失败", spuIds, ex);
        }
    }

}
