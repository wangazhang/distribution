package com.hissp.distribution.module.material.controller.admin.outbound;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.material.controller.admin.outbound.vo.MaterialOutboundItemRespVO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialDefinitionDO;
import com.hissp.distribution.module.material.dal.dataobject.MaterialOutboundItemDO;
import com.hissp.distribution.module.material.dal.mysql.MaterialDefinitionMapper;
import com.hissp.distribution.module.material.dal.mysql.MaterialOutboundItemMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料出库明细")
@RestController
@RequestMapping("/material/outbound-item")
public class MaterialOutboundItemController {

    @Resource
    private MaterialOutboundItemMapper outboundItemMapper;

    @Resource
    private MaterialDefinitionMapper materialDefinitionMapper;

    @GetMapping("/list-by-outbound-id")
    @Operation(summary = "根据出库单ID查询物料明细列表")
    @Parameter(name = "outboundId", description = "出库单ID", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('material:outbound:query')")
    public CommonResult<List<MaterialOutboundItemRespVO>> getItemListByOutboundId(@RequestParam("outboundId") Long outboundId) {
        List<MaterialOutboundItemDO> itemList = outboundItemMapper.selectListByOutboundId(outboundId);

        // 获取所有物料ID
        List<Long> materialIds = itemList.stream()
                .map(MaterialOutboundItemDO::getMaterialId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询物料信息
        Map<Long, MaterialDefinitionDO> materialMap = materialDefinitionMapper.selectBatchIds(materialIds)
                .stream()
                .collect(Collectors.toMap(MaterialDefinitionDO::getId, m -> m));

        // 转换为VO并填充物料信息
        List<MaterialOutboundItemRespVO> respList = itemList.stream().map(item -> {
            MaterialOutboundItemRespVO vo = new MaterialOutboundItemRespVO();
            vo.setId(item.getId());
            vo.setOutboundId(item.getOutboundId());
            vo.setMaterialId(item.getMaterialId());
            vo.setQuantity(item.getQuantity());
            vo.setUnit(item.getUnit());
            vo.setCreateTime(item.getCreateTime());

            // 填充物料信息
            MaterialDefinitionDO material = materialMap.get(item.getMaterialId());
            if (material != null) {
                vo.setMaterialCode(material.getCode());
                vo.setMaterialName(material.getName());
                vo.setMaterialImage(material.getImage());
                vo.setBaseUnit(material.getBaseUnit());
            }

            return vo;
        }).collect(Collectors.toList());

        return success(respList);
    }

}
