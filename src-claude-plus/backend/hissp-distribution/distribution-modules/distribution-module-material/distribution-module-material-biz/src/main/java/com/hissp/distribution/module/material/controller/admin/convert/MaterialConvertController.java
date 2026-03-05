package com.hissp.distribution.module.material.controller.admin.convert;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRuleCreateReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRulePageReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRuleUpdateReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRecordPageReqDTO;
import com.hissp.distribution.module.material.api.dto.MaterialConvertRecordRespDTO;
import com.hissp.distribution.module.material.controller.admin.convert.vo.MaterialConvertRuleRespVO;
import com.hissp.distribution.module.material.convert.MaterialConvertRuleConvert;
import com.hissp.distribution.module.material.dal.dataobject.MaterialConvertRuleDO;
import com.hissp.distribution.module.material.service.convert.MaterialConvertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料转换")
@RestController
@RequestMapping("/material/convert")
public class MaterialConvertController {

    @Resource
    private MaterialConvertService convertService;

    // ========== 转换规则管理 ==========

    @GetMapping("/rule/page")
    @Operation(summary = "获得转换规则分页")
    @PreAuthorize("@ss.hasPermission('material:convert:query')")
    public CommonResult<PageResult<MaterialConvertRuleRespVO>> getConvertRulePage(@Valid MaterialConvertRulePageReqDTO pageVO) {
        PageResult<MaterialConvertRuleDO> pageResult = convertService.getConvertRulePage(pageVO);
        
        // 转换为VO并设置状态描述
        List<MaterialConvertRuleRespVO> respList = MaterialConvertRuleConvert.INSTANCE.convertList(pageResult.getList());
        respList.forEach(this::setStatusName);
        
        return success(new PageResult<>(respList, pageResult.getTotal()));
    }

    @GetMapping("/rule/{id}")
    @Operation(summary = "获得转换规则详情")
    @Parameter(name = "id", description = "规则ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('material:convert:query')")
    public CommonResult<MaterialConvertRuleRespVO> getConvertRule(@PathVariable("id") Long id) {
        MaterialConvertRuleDO rule = convertService.getConvertRule(id);
        if (rule == null) {
            return success(null);
        }
        
        MaterialConvertRuleRespVO respVO = MaterialConvertRuleConvert.INSTANCE.convert(rule);
        setStatusName(respVO);
        return success(respVO);
    }

    @PostMapping("/rule")
    @Operation(summary = "创建转换规则")
    @PreAuthorize("@ss.hasPermission('material:convert:create')")
    public CommonResult<Long> createConvertRule(@Valid @RequestBody MaterialConvertRuleCreateReqDTO createReqVO) {
        Long ruleId = convertService.createConvertRule(createReqVO);
        return success(ruleId);
    }

    @PutMapping("/rule")
    @Operation(summary = "更新转换规则")
    @PreAuthorize("@ss.hasPermission('material:convert:update')")
    public CommonResult<Boolean> updateConvertRule(@Valid @RequestBody MaterialConvertRuleUpdateReqDTO updateReqVO) {
        convertService.updateConvertRule(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/rule/{id}")
    @Operation(summary = "删除转换规则")
    @Parameter(name = "id", description = "规则ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('material:convert:delete')")
    public CommonResult<Boolean> deleteConvertRule(@PathVariable("id") Long id) {
        convertService.deleteConvertRule(id);
        return success(true);
    }

    @GetMapping("/rule/list")
    @Operation(summary = "获得转换规则列表")
    @PreAuthorize("@ss.hasPermission('material:convert:query')")
    public CommonResult<List<MaterialConvertRuleRespVO>> getConvertRuleList() {
        List<MaterialConvertRuleDO> list = convertService.getConvertRuleList();
        List<MaterialConvertRuleRespVO> respList = MaterialConvertRuleConvert.INSTANCE.convertList(list);
        respList.forEach(this::setStatusName);
        return success(respList);
    }

    // ========== 转换记录管理 ==========

    @GetMapping("/record/page")
    @Operation(summary = "获得转换记录分页")
    @PreAuthorize("@ss.hasPermission('material:convert:query')")
    public CommonResult<PageResult<MaterialConvertRecordRespDTO>> getConvertRecordPage(@Valid MaterialConvertRecordPageReqDTO pageVO) {
        PageResult<MaterialConvertRecordRespDTO> pageResult = convertService.getConvertRecordPage(pageVO);
        return success(pageResult);
    }

    @DeleteMapping("/record/{id}")
    @Operation(summary = "取消转换记录")
    @Parameter(name = "id", description = "记录ID", required = true, example = "1")
    @PreAuthorize("@ss.hasPermission('material:convert:cancel')")
    public CommonResult<Boolean> cancelConvert(@PathVariable("id") Long id) {
        convertService.cancelConvert(id);
        return success(true);
    }

    private void setStatusName(MaterialConvertRuleRespVO respVO) {
        Map<Integer, String> statusMap = new HashMap<>();
        statusMap.put(0, "禁用");
        statusMap.put(1, "启用");
        respVO.setStatusName(statusMap.getOrDefault(respVO.getStatus(), "未知"));
    }

}