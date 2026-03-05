package com.hissp.distribution.module.pay.controller.admin.settle;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountPageReqVO;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountRespVO;
import com.hissp.distribution.module.pay.controller.admin.settle.vo.SettleAccountUpdateReqVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountSaveReqVO;
import com.hissp.distribution.module.pay.convert.settle.PaySettleAccountConvert;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import com.hissp.distribution.module.pay.enums.payease.PayeaseSubMerchantStatusEnum;
import com.hissp.distribution.module.pay.service.settle.PaySettleAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 提现资料")
@RestController
@RequestMapping("/pay/settle-account")
@Validated
public class SettleAccountController {

    @Resource
    private PaySettleAccountService settleAccountService;

    @GetMapping("/page")
    @Operation(summary = "分页查询")
    @PreAuthorize("@ss.hasPermission('pay:settle-account:query')")
    public CommonResult<PageResult<SettleAccountRespVO>> getPage(SettleAccountPageReqVO reqVO) {
        PageResult<PaySettleAccountDO> pageResult = settleAccountService.getPage(reqVO);
        return success(PaySettleAccountConvert.INSTANCE.convertPage(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获取详情")
    @PreAuthorize("@ss.hasPermission('pay:settle-account:query')")
    public CommonResult<SettleAccountRespVO> get(@RequestParam("id") Long id) {
        return success(PaySettleAccountConvert.INSTANCE.convert(settleAccountService.get(id)));
    }

    @PutMapping("/update")
    @Operation(summary = "协助修改")
    @PreAuthorize("@ss.hasPermission('pay:settle-account:update')")
    public CommonResult<Boolean> update(@Valid @RequestBody SettleAccountUpdateReqVO reqVO) {
        PaySettleAccountDO account = settleAccountService.get(reqVO.getId());
        if (account == null) {
            return success(false);
        }
        PaySettleAccountDO update = PaySettleAccountDO.builder()
                .id(reqVO.getId())
                .mobile(reqVO.getMobile())
                .email(reqVO.getEmail())
                .bankAccountNo(reqVO.getBankAccountNo())
                .bankBranchName(reqVO.getBankBranchName())
                .bankCardFrontUrl(reqVO.getBankCardFrontUrl())
                .extra(reqVO.getExtra())
                .status(PayeaseSubMerchantStatusEnum.DRAFT.getStatus())
                .build();
        settleAccountService.saveDraft(account.getUserId(), toSaveReq(account, reqVO));
        return success(true);
    }

    @PostMapping("/submit")
    @Operation(summary = "后台提交审核")
    @PreAuthorize("@ss.hasPermission('pay:settle-account:update')")
    public CommonResult<Boolean> submit(@RequestParam("id") Long id) {
        settleAccountService.adminSubmit(id);
        return success(true);
    }

    @PostMapping("/sync")
    @Operation(summary = "同步状态")
    @PreAuthorize("@ss.hasPermission('pay:settle-account:update')")
    public CommonResult<Boolean> sync(@RequestParam("id") Long id) {
        settleAccountService.syncStatus(id);
        return success(true);
    }

    private AppSettleAccountSaveReqVO toSaveReq(PaySettleAccountDO account, SettleAccountUpdateReqVO reqVO) {
        AppSettleAccountSaveReqVO vo = new AppSettleAccountSaveReqVO();
        vo.setSignedName(account.getSignedName());
        vo.setMobile(reqVO.getMobile() == null ? account.getMobile() : reqVO.getMobile());
        vo.setIdCardNo(account.getIdCardNo());
        vo.setIdCardValidStart(account.getIdCardValidStart() == null ? null : account.getIdCardValidStart().toString());
        vo.setIdCardValidEnd(account.getIdCardValidEnd() == null ? null : account.getIdCardValidEnd().toString());
        vo.setIdCardFrontUrl(account.getIdCardFrontUrl());
        vo.setIdCardBackUrl(account.getIdCardBackUrl());
        vo.setBankAccountNo(reqVO.getBankAccountNo() == null ? account.getBankAccountNo() : reqVO.getBankAccountNo());
        vo.setBankAccountName(account.getBankAccountName());
        vo.setBankName(account.getBankName());
        vo.setBankBranchName(reqVO.getBankBranchName() == null ? account.getBankBranchName() : reqVO.getBankBranchName());
        vo.setBankCardFrontUrl(reqVO.getBankCardFrontUrl() == null ? account.getBankCardFrontUrl() : reqVO.getBankCardFrontUrl());
        vo.setProvinceCode(account.getProvinceCode());
        vo.setCityCode(account.getCityCode());
        vo.setAddress(account.getAddress());
        vo.setEmail(reqVO.getEmail() == null ? account.getEmail() : reqVO.getEmail());
        vo.setExtra(reqVO.getExtra() == null ? account.getExtra() : reqVO.getExtra());
        return vo;
    }
}
