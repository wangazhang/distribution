package com.hissp.distribution.module.material.controller.app.cwms;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialBalancePageReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialBalanceRespDTO;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.material.controller.app.cwms.vo.AppUserMaterialBalancePageReqVO;
import com.hissp.distribution.module.material.controller.app.cwms.vo.AppUserMaterialBalanceRespVO;
import com.hissp.distribution.module.material.controller.app.cwms.vo.AppUserMaterialStockRespVO;
import com.hissp.distribution.module.material.controller.app.cwms.vo.AppUserMaterialBatchQueryReqVO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

/**
 * App 端物料余额查询入口
 *
 * <p>原 AppUserMaterialBalanceController 重命名并梳理逻辑，避免继续使用旧的 user_material_balance 命名。</p>
 */
@Tag(name = "App - 物料余额/库存查询")
@RestController
@RequestMapping({"/material/user-balance", "/material/balance"})
@Validated
public class AppMaterialBalanceController {

    private static final Long RAW_MATERIAL_ID = 90000L;

    @Resource
    private MaterialApi materialApi;
    @Resource
    private ProductSpuApi productSpuApi;

    @GetMapping("/get")
    @Operation(summary = "获取当前用户的一条物料余额记录")
    public CommonResult<AppUserMaterialBalanceRespVO> getUserMaterialBalance() {
        Long userId = getLoginUserId();
        PageResult<MaterialBalanceRespDTO> page = materialApi.getBalancePage(
                buildBalancePageReq(userId, 1, 1, null));
        if (page.getList() == null || page.getList().isEmpty()) {
            return success(null);
        }
        return success(toBalanceResp(page.getList()).get(0));
    }

    @GetMapping("/page")
    @Operation(summary = "分页查询当前用户物料余额")
    public CommonResult<PageResult<AppUserMaterialBalanceRespVO>> getUserMaterialBalancePage(
            @Valid AppUserMaterialBalancePageReqVO pageReqVO) {
        pageReqVO.setUserId(getLoginUserId());
        PageResult<MaterialBalanceRespDTO> page = materialApi.getBalancePage(
                buildBalancePageReq(pageReqVO.getUserId(),
                        pageReqVO.getPageNo(),
                        pageReqVO.getPageSize(),
                        pageReqVO.getMaterialId()));
        List<AppUserMaterialBalanceRespVO> list = toBalanceResp(page.getList());
        return success(new PageResult<>(list, page.getTotal()));
    }

    @PostMapping("/batch-query")
    @Operation(summary = "批量查询当前用户指定物料的库存（支持通过SPU ID或物料ID查询）")
    public CommonResult<List<AppUserMaterialStockRespVO>> batchQueryUserMaterialStock(
            @RequestBody @Valid AppUserMaterialBatchQueryReqVO reqVO) {
        Long userId = getLoginUserId();
        reqVO.setUserId(userId);
        // 传入的ID可能是SPU ID（商品ID），需要通过物料定义表转换为物料ID
        List<Long> inputIds = Optional.ofNullable(reqVO.getMaterialIds()).orElse(Collections.emptyList());
        
        // 先通过SPU ID查询物料定义，获取物料ID
        Map<Long, MaterialDefinitionRespDTO> definitionBySpuIdMap = loadMaterialDefinitionMapBySpuIds(inputIds);
        java.util.Map<Long, Long> spuIdToMaterialIdMap = new java.util.HashMap<>();
        java.util.Set<Long> actualMaterialIds = new java.util.HashSet<>();
        
        for (Long inputId : inputIds) {
            MaterialDefinitionRespDTO definition = definitionBySpuIdMap.get(inputId);
            if (definition != null) {
                // 找到了物料定义，说明输入的是SPU ID
                Long materialId = definition.getId();
                spuIdToMaterialIdMap.put(inputId, materialId);
                actualMaterialIds.add(materialId);
            } else {
                // 没找到物料定义，说明输入的可能就是物料ID
                actualMaterialIds.add(inputId);
            }
        }
        
        // 查询物料余额
        List<MaterialBalanceRespDTO> balances = materialApi.getBalances(userId, new java.util.ArrayList<>(actualMaterialIds));
        
        // 加载物料定义（通过物料ID）
        Map<Long, MaterialDefinitionRespDTO> definitionMap = loadMaterialDefinitionMap(new java.util.ArrayList<>(actualMaterialIds));
        
        // 收集所有SPU ID
        java.util.Set<Long> spuIds = new java.util.HashSet<>();
        definitionMap.values().stream()
                .filter(Objects::nonNull)
                .forEach(definition -> {
                    if (definition.getSpuId() != null) {
                        spuIds.add(definition.getSpuId());
                    }
                });
        // 添加输入的SPU ID
        spuIds.addAll(inputIds);
        
        Map<Long, ProductSpuRespDTO> spuMap = loadSpuMapBySpuIds(spuIds);
        
        // 构建响应，保持与输入ID的对应关系
        return success(inputIds.stream()
                .map(inputId -> {
                    Long materialId = spuIdToMaterialIdMap.getOrDefault(inputId, inputId);
                    return buildStockResp(materialId, balances, definitionMap, spuMap);
                })
                .collect(Collectors.toList()));
    }

    @GetMapping("/outbound-materials")
    @Operation(summary = "获取当前用户可出库的物料列表")
    public CommonResult<List<AppUserMaterialStockRespVO>> getOutboundMaterials() {
        Long userId = getLoginUserId();
        PageResult<MaterialBalanceRespDTO> page = materialApi.getBalancePage(
                buildBalancePageReq(userId, 1, 1000, null));
        List<MaterialBalanceRespDTO> list = Optional.ofNullable(page.getList()).orElse(Collections.emptyList());
        List<Long> materialIds = list.stream()
                .map(MaterialBalanceRespDTO::getMaterialId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MaterialDefinitionRespDTO> definitionMap = loadMaterialDefinitionMap(materialIds);
        java.util.Set<Long> spuIds = new java.util.HashSet<>();
        definitionMap.values().stream()
                .filter(Objects::nonNull)
                .forEach(definition -> {
                    if (definition.getSpuId() != null) {
                        spuIds.add(definition.getSpuId());
                    }
                });
        Map<Long, ProductSpuRespDTO> spuMap = loadSpuMapBySpuIds(spuIds);
        List<AppUserMaterialStockRespVO> result = list.stream()
                .map(balance -> buildStockResp(balance.getMaterialId(),
                        Collections.singletonList(balance), definitionMap, spuMap))
                .filter(vo -> !RAW_MATERIAL_ID.equals(vo.getMaterialId()))
                .collect(Collectors.toList());
        return success(result);
    }

    private MaterialBalancePageReqDTO buildBalancePageReq(Long userId,
                                                          Integer pageNo,
                                                          Integer pageSize,
                                                          Long materialId) {
        MaterialBalancePageReqDTO dto = new MaterialBalancePageReqDTO();
        dto.setUserId(userId);
        dto.setPageNo(pageNo);
        dto.setPageSize(pageSize);
        dto.setMaterialId(materialId);
        return dto;
    }

    private AppUserMaterialStockRespVO buildStockResp(Long materialId,
                                                      List<MaterialBalanceRespDTO> balances,
                                                      Map<Long, MaterialDefinitionRespDTO> definitionMap,
                                                      Map<Long, ProductSpuRespDTO> spuMap) {
        AppUserMaterialStockRespVO vo = new AppUserMaterialStockRespVO();
        vo.setMaterialId(materialId);
        MaterialBalanceRespDTO balance = balances.stream()
                .filter(item -> Objects.equals(item.getMaterialId(), materialId))
                .findFirst()
                .orElse(null);
        Integer stockValue = balance == null ? 0 : Optional.ofNullable(balance.getAvailableBalance()).orElse(0);
        vo.setStock(stockValue);
        vo.setBalance(stockValue); // 同时设置balance字段，确保与物料余额页面字段名一致
        MaterialDefinitionRespDTO definition = definitionMap.get(materialId);
        ProductSpuRespDTO spu = definition != null && definition.getSpuId() != null ? spuMap.get(definition.getSpuId()) : null;
        if (spu != null) {
            vo.setMaterialName(spu.getName());
            vo.setMaterialImage(spu.getPicUrl());
        } else {
            vo.setMaterialName("未知物料");
            vo.setMaterialImage("");
        }
        vo.setUnit(definition != null && definition.getBaseUnit() != null ? definition.getBaseUnit() : "件");
        return vo;
    }

    private List<AppUserMaterialBalanceRespVO> toBalanceResp(List<MaterialBalanceRespDTO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> materialIds = list.stream()
                .map(MaterialBalanceRespDTO::getMaterialId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MaterialDefinitionRespDTO> definitionMap = loadMaterialDefinitionMap(materialIds);
        java.util.Set<Long> spuIds = new java.util.HashSet<>();
        definitionMap.values().stream()
                .filter(Objects::nonNull)
                .forEach(definition -> {
                    if (definition.getSpuId() != null) {
                        spuIds.add(definition.getSpuId());
                    }
                    if (definition.getConvertedSpuId() != null) {
                        spuIds.add(definition.getConvertedSpuId());
                    }
                });
        Map<Long, ProductSpuRespDTO> spuMap = loadSpuMapBySpuIds(spuIds);

        return list.stream()
                .map(balance -> convertBalance(balance, spuMap, definitionMap))
                .collect(Collectors.toList());
    }

    private AppUserMaterialBalanceRespVO convertBalance(MaterialBalanceRespDTO balance,
                                                        Map<Long, ProductSpuRespDTO> spuMap,
                                                        Map<Long, MaterialDefinitionRespDTO> definitionMap) {
        AppUserMaterialBalanceRespVO vo = new AppUserMaterialBalanceRespVO();
        vo.setId(balance.getId());
        vo.setUserId(balance.getUserId() == null ? null : balance.getUserId().intValue());
        vo.setMaterialId(balance.getMaterialId());
        vo.setBalance(Optional.ofNullable(balance.getAvailableBalance()).orElse(0));
        vo.setUpdateTime(balance.getUpdateTime());

        MaterialDefinitionRespDTO definition = definitionMap.get(balance.getMaterialId());
        Long fallbackSpuId = definition != null ? definition.getSpuId() : null;
        vo.setShareId(fallbackSpuId != null ? fallbackSpuId : balance.getMaterialId());
        vo.setRefillId(fallbackSpuId != null ? fallbackSpuId : balance.getMaterialId());

        if (definition != null) {
            vo.setMaterialType(definition.getType());
            vo.setSupportConvert(Boolean.TRUE.equals(definition.getSupportConvert()));
            vo.setConvertPrice(definition.getConvertPrice());
            ProductSpuRespDTO materialSpu = definition.getSpuId() != null ? spuMap.get(definition.getSpuId()) : null;
            if (materialSpu != null) {
                vo.setMaterialName(materialSpu.getName());
                vo.setMaterialImage(materialSpu.getPicUrl());
            } else {
                vo.setMaterialName("未知物料");
                vo.setMaterialImage("");
            }
            vo.setUnit(definition.getBaseUnit() != null ? definition.getBaseUnit() : "件");
            if (Boolean.TRUE.equals(definition.getSupportConvert()) && definition.getConvertedSpuId() != null) {
                vo.setConvertTargetSpuId(definition.getConvertedSpuId());
                ProductSpuRespDTO targetSpu = spuMap.get(definition.getConvertedSpuId());
                vo.setConvertTargetName(targetSpu != null && targetSpu.getName() != null
                        ? targetSpu.getName()
                        : "SPU#" + definition.getConvertedSpuId());
            }
            vo.setSupportRestock(Boolean.TRUE.equals(definition.getType() != null && definition.getType() == 2));
        } else {
            vo.setMaterialName("未知物料");
            vo.setMaterialImage("");
            vo.setUnit("件");
            vo.setSupportRestock(Boolean.FALSE);
        }
        return vo;
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

    private Map<Long, MaterialDefinitionRespDTO> loadMaterialDefinitionMap(List<Long> materialIds) {
        if (materialIds == null || materialIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<MaterialDefinitionRespDTO> definitions = materialApi.getDefinitions(materialIds);
        if (definitions == null) {
            return Collections.emptyMap();
        }
        return definitions.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(MaterialDefinitionRespDTO::getId, def -> def, (a, b) -> a));
    }

    /**
     * 通过SPU ID批量查询物料定义
     * @param spuIds SPU ID列表
     * @return SPU ID -> 物料定义的映射
     */
    private Map<Long, MaterialDefinitionRespDTO> loadMaterialDefinitionMapBySpuIds(List<Long> spuIds) {
        if (spuIds == null || spuIds.isEmpty()) {
            return Collections.emptyMap();
        }
        java.util.Map<Long, MaterialDefinitionRespDTO> result = new java.util.HashMap<>();
        for (Long spuId : spuIds) {
            try {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(spuId);
                if (definition != null) {
                    result.put(spuId, definition);
                }
            } catch (Exception e) {
                // 如果查询失败，继续处理下一个
                // 可能是该SPU ID没有对应的物料定义
            }
        }
        return result;
    }
}
