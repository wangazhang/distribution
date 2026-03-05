package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price;

import cn.hutool.core.collection.CollUtil;
import com.hissp.distribution.framework.apilog.core.annotation.ApiAccessLog;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageParam;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.framework.excel.core.util.ExcelUtils;
import com.hissp.distribution.module.material.api.MaterialApi;
import com.hissp.distribution.module.material.api.dto.MaterialDefinitionRespDTO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPricePageReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPriceRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.price.vo.MaterialRestockPriceSaveReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.material.MaterialRestockPriceDO;
import com.hissp.distribution.module.mb.domain.service.mbdt.price.MaterialRestockPriceService;
import com.hissp.distribution.module.member.api.level.MemberLevelApi;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.product.api.spu.ProductSpuApi;
import com.hissp.distribution.module.product.api.spu.dto.ProductSpuRespDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import static com.hissp.distribution.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 补货价格表：记录不同用户等级对应的商品补货价格")
@RestController
@RequestMapping("/mb/material-restock-price")
@Validated
public class MaterialRestockPriceController {

    @Resource
    private MaterialRestockPriceService materialRestockPriceService;
    @Resource
    private MemberLevelApi memberLevelApi;
    @Resource
    private ProductSpuApi productSpuApi;
    @Resource
    private MaterialApi materialApi;

    @PostMapping("/create")
    @Operation(summary = "创建补货价格表：记录不同用户等级对应的商品补货价格")
    @PreAuthorize("@ss.hasPermission('mb:material-restock-price:create')")
    public CommonResult<Long> createMaterialRestockPrice(@Valid @RequestBody MaterialRestockPriceSaveReqVO createReqVO) {
        return success(materialRestockPriceService.createMaterialRestockPrice(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新补货价格表：记录不同用户等级对应的商品补货价格")
    @PreAuthorize("@ss.hasPermission('mb:material-restock-price:update')")
    public CommonResult<Boolean> updateMaterialRestockPrice(@Valid @RequestBody MaterialRestockPriceSaveReqVO updateReqVO) {
        materialRestockPriceService.updateMaterialRestockPrice(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除补货价格表：记录不同用户等级对应的商品补货价格")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('mb:material-restock-price:delete')")
    public CommonResult<Boolean> deleteMaterialRestockPrice(@RequestParam("id") Long id) {
        materialRestockPriceService.deleteMaterialRestockPrice(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得补货价格表：记录不同用户等级对应的商品补货价格")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('mb:material-restock-price:query')")
    public CommonResult<MaterialRestockPriceRespVO> getMaterialRestockPrice(@RequestParam("id") Long id) {
        MaterialRestockPriceDO materialRestockPrice = materialRestockPriceService.getMaterialRestockPrice(id);
        if (materialRestockPrice == null) {
            return success(null);
        }
        MaterialRestockPriceRespVO respVO = BeanUtils.toBean(materialRestockPrice, MaterialRestockPriceRespVO.class);
        MemberLevelRespDTO level = memberLevelApi.getMemberLevel(materialRestockPrice.getLevelId());
        if (level != null) {
            respVO.setLevelName(level.getName());
        }
        ProductSpuRespDTO product = productSpuApi.getSpu(materialRestockPrice.getProductId());
        if (product != null) {
            respVO.setProductName(product.getName());
        } else {
            MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(materialRestockPrice.getProductId());
            if (definition != null) {
                respVO.setProductName(definition.getName());
            }
        }
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得补货价格表：记录不同用户等级对应的商品补货价格分页")
    @PreAuthorize("@ss.hasPermission('mb:material-restock-price:query')")
    public CommonResult<PageResult<MaterialRestockPriceRespVO>> getMaterialRestockPricePage(@Valid MaterialRestockPricePageReqVO pageReqVO) {
        PageResult<MaterialRestockPriceDO> pageResult = materialRestockPriceService.getMaterialRestockPricePage(pageReqVO);
        List<MaterialRestockPriceDO> list = pageResult.getList();
        if (CollUtil.isEmpty(list)) {
            return success(new PageResult<>(pageResult.getTotal()));
        }
        Set<Long> levelIds = CollectionUtils.convertSet(list, MaterialRestockPriceDO::getLevelId);
        Set<Long> productIds = CollectionUtils.convertSet(list, MaterialRestockPriceDO::getProductId);
        Map<Long, MemberLevelRespDTO> levelMap = CollUtil.isEmpty(levelIds)
                ? Collections.emptyMap()
                : memberLevelApi.getMemberLevelMap(levelIds);
        Map<Long, ProductSpuRespDTO> productMap = CollUtil.isEmpty(productIds)
                ? new HashMap<>()
                : new HashMap<>(CollectionUtils.convertMap(productSpuApi.getSpuList(productIds), ProductSpuRespDTO::getId));
        List<MaterialRestockPriceRespVO> voList = CollectionUtils.convertList(list, item -> {
            MaterialRestockPriceRespVO vo = BeanUtils.toBean(item, MaterialRestockPriceRespVO.class);
            Optional.ofNullable(levelMap.get(item.getLevelId())).ifPresent(level -> vo.setLevelName(level.getName()));
            ProductSpuRespDTO product = productMap.get(item.getProductId());
            if (product == null) {
                product = productSpuApi.getSpu(item.getProductId());
                if (product != null) {
                    productMap.put(item.getProductId(), product);
                }
            }
            if (product != null) {
                vo.setProductName(product.getName());
            } else {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(item.getProductId());
                if (definition != null) {
                    vo.setProductName(definition.getName());
                }
            }
            return vo;
        });
        return success(new PageResult<>(voList, pageResult.getTotal()));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出补货价格表：记录不同用户等级对应的商品补货价格 Excel")
    @PreAuthorize("@ss.hasPermission('mb:material-restock-price:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportMaterialRestockPriceExcel(@Valid MaterialRestockPricePageReqVO pageReqVO,
              HttpServletResponse response) throws IOException {
        pageReqVO.setPageSize(PageParam.PAGE_SIZE_NONE);
        PageResult<MaterialRestockPriceDO> pageResult = materialRestockPriceService.getMaterialRestockPricePage(pageReqVO);
        List<MaterialRestockPriceDO> list = pageResult.getList();
        Set<Long> levelIds = CollectionUtils.convertSet(list, MaterialRestockPriceDO::getLevelId);
        Set<Long> productIds = CollectionUtils.convertSet(list, MaterialRestockPriceDO::getProductId);
        Map<Long, MemberLevelRespDTO> levelMap = CollUtil.isEmpty(levelIds)
                ? Collections.emptyMap()
                : memberLevelApi.getMemberLevelMap(levelIds);
        Map<Long, ProductSpuRespDTO> productMap = CollUtil.isEmpty(productIds)
                ? new HashMap<>()
                : new HashMap<>(CollectionUtils.convertMap(productSpuApi.getSpuList(productIds), ProductSpuRespDTO::getId));
        // 导出 Excel
        List<MaterialRestockPriceRespVO> data = CollectionUtils.convertList(list, item -> {
            MaterialRestockPriceRespVO vo = BeanUtils.toBean(item, MaterialRestockPriceRespVO.class);
            Optional.ofNullable(levelMap.get(item.getLevelId())).ifPresent(level -> vo.setLevelName(level.getName()));
            ProductSpuRespDTO product = productMap.get(item.getProductId());
            if (product == null) {
                product = productSpuApi.getSpu(item.getProductId());
                if (product != null) {
                    productMap.put(item.getProductId(), product);
                }
            }
            if (product != null) {
                vo.setProductName(product.getName());
            } else {
                MaterialDefinitionRespDTO definition = materialApi.getDefinitionBySpuId(item.getProductId());
                if (definition != null) {
                    vo.setProductName(definition.getName());
                }
            }
            return vo;
        });
        ExcelUtils.write(response, "补货价格表：记录不同用户等级对应的商品补货价格.xls", "数据", MaterialRestockPriceRespVO.class, data);
    }

}
