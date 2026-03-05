package com.hissp.distribution.module.material.controller.app.cwms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialTxnPageReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialTxnRespDTO;
import com.hissp.distribution.module.material.controller.app.cwms.vo.AppMaterialRecordPageReqVO;
import com.hissp.distribution.module.material.controller.app.cwms.vo.AppMaterialRecordRespVO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "App - 物料变更记录")
@RestController
@RequestMapping("/material/record")
@Validated
public class AppMaterialRecordController {

    @Resource
    private MaterialApi materialApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/page")
    @Operation(summary = "获取物料变更记录分页")
    public CommonResult<PageResult<AppMaterialRecordRespVO>> getMaterialRecordPage(@Valid AppMaterialRecordPageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        MaterialTxnPageReqDTO dto = new MaterialTxnPageReqDTO();
        dto.setPageNo(pageReqVO.getPageNo());
        dto.setPageSize(pageReqVO.getPageSize());
        dto.setUserId(pageReqVO.getUserId());
        dto.setMaterialId(pageReqVO.getMaterialId());
        PageResult<MaterialTxnRespDTO> page = materialApi.getTxnPage(dto);
        List<AppMaterialRecordRespVO> list = toRecordResp(page.getList());
        return success(new PageResult<>(list, page.getTotal()));
    }

    private List<AppMaterialRecordRespVO> toRecordResp(List<MaterialTxnRespDTO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> materialIds = list.stream()
                .map(MaterialTxnRespDTO::getMaterialId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MaterialDefinitionRespDTO> definitionMap = loadDefinitionMap(materialIds);
        java.util.Set<Long> spuIds = definitionMap.values().stream()
                .filter(Objects::nonNull)
                .map(MaterialDefinitionRespDTO::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ProductSpuRespDTO> spuMap = loadSpuMapBySpuIds(spuIds);
        return list.stream().map(txn -> {
            AppMaterialRecordRespVO vo = new AppMaterialRecordRespVO();
            vo.setId(txn.getId());
            vo.setUserId(txn.getUserId());
            vo.setMaterialId(txn.getMaterialId());
            int direction = Optional.ofNullable(txn.getDirection()).orElse(1);
            vo.setActionType(direction > 0 ? 0 : 1); // IN->0获, OUT(-1)->1用
            vo.setAmount(txn.getQuantity());
            vo.setAfterAmount(txn.getBalanceAfter());
            Integer qty = Optional.ofNullable(txn.getQuantity()).orElse(0);
            Integer after = Optional.ofNullable(txn.getBalanceAfter()).orElse(0);
            vo.setBeforeAmount(after - qty * direction);
            if (txn.getCreateTime() != null) {
                vo.setCreateTime(txn.getCreateTime());
                vo.setActionDate(txn.getCreateTime().toLocalDate());
            }
            // 优先使用 reason 作为来源描述，兼容缺失时回退到 bizType
            vo.setSourceDesc(txn.getReason() != null ? txn.getReason() : txn.getBizType());
            MaterialDefinitionRespDTO definition = definitionMap.get(txn.getMaterialId());
            if (definition != null) {
                vo.setMaterialName(Optional.ofNullable(definition.getName())
                        .orElse("物料#" + definition.getId()));
                vo.setUnit(Optional.ofNullable(definition.getBaseUnit()).orElse("件"));
                if (definition.getSpuId() != null) {
                    ProductSpuRespDTO spu = spuMap.get(definition.getSpuId());
                    if (spu != null) {
                        if (spu.getName() != null) {
                            vo.setMaterialName(spu.getName());
                        }
                        vo.setMaterialImage(spu.getPicUrl());
                    }
                }
            } else {
                vo.setUnit("件");
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<Long, MaterialDefinitionRespDTO> loadDefinitionMap(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<MaterialDefinitionRespDTO> definitions = materialApi.getDefinitions(materialIds);
        return definitions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MaterialDefinitionRespDTO::getId,
                        def -> def, (a, b) -> a));
    }

    private Map<Long, ProductSpuRespDTO> loadSpuMapBySpuIds(java.util.Collection<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Long> distinctIds = spuIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (distinctIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ProductSpuRespDTO> spus = productSpuApi.getSpuList(distinctIds);
        return spus.stream().collect(Collectors.toMap(ProductSpuRespDTO::getId, vo -> vo, (a, b) -> a));
    }
}
