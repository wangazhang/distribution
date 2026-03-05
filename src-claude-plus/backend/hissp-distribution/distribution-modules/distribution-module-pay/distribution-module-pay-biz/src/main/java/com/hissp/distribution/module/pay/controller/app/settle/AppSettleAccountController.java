package com.hissp.distribution.module.pay.controller.app.settle;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountChangeCardReqVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountRespVO;
import com.hissp.distribution.module.pay.controller.app.settle.vo.AppSettleAccountSaveReqVO;
import com.hissp.distribution.module.pay.convert.settle.PaySettleAccountConvert;
import com.hissp.distribution.module.pay.dal.dataobject.account.PaySettleAccountDO;
import com.hissp.distribution.module.pay.service.settle.PaySettleAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 App - 提现资料")
@RestController
@RequestMapping("/app/pay/settle-account")
@Validated
public class AppSettleAccountController {

    @Resource
    private PaySettleAccountService settleAccountService;

    @GetMapping("/profile")
    @Operation(summary = "获取提现资料")
    public CommonResult<AppSettleAccountRespVO> getProfile() {
        PaySettleAccountDO account = settleAccountService.getByUser(getLoginUserId());
        return success(account == null ? null : PaySettleAccountConvert.INSTANCE.convertApp(account));
    }

    @PostMapping("/draft")
    @Operation(summary = "保存草稿")
    public CommonResult<Long> saveDraft(@Valid @RequestBody AppSettleAccountSaveReqVO reqVO) {
        return success(settleAccountService.saveDraft(getLoginUserId(), reqVO));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交审核")
    public CommonResult<Boolean> submit() {
        settleAccountService.submit(getLoginUserId());
        return success(true);
    }

    @PostMapping("/change-card/draft")
    @Operation(summary = "保存换卡资料")
    public CommonResult<Long> changeCardDraft(@Valid @RequestBody AppSettleAccountChangeCardReqVO reqVO) {
        return success(settleAccountService.saveChangeCardDraft(getLoginUserId(), reqVO));
    }

    @PostMapping("/change-card/submit")
    @Operation(summary = "提交换卡审核")
    public CommonResult<Boolean> changeCardSubmit() {
        settleAccountService.submitChangeCard(getLoginUserId());
        return success(true);
    }
}
