package com.hissp.distribution.module.material.controller.admin.txn;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnPageReqVO;
import com.hissp.distribution.module.material.controller.admin.txn.vo.MaterialTxnRespVO;
import com.hissp.distribution.module.material.service.txn.MaterialTxnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 物料流水")
@RestController
@RequestMapping("/material/txn")
public class MaterialTxnController {

    @Resource
    private MaterialTxnService txnService;

    @GetMapping("/page")
    @Operation(summary = "获得物料流水分页")
    @PreAuthorize("@ss.hasPermission('material:txn:query')")
    public CommonResult<PageResult<MaterialTxnRespVO>> getTxnPage(@Valid MaterialTxnPageReqVO pageVO) {
        PageResult<MaterialTxnRespVO> pageResult = txnService.getTxnPage(pageVO);
        return success(pageResult);
    }

}