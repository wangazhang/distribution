package com.hissp.distribution.module.trade.controller.app.brokerage;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawAccountRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawRespVO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageWithdrawConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.List;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.trade.enums.ErrorCodeConstants.BROKERAGE_WITHDRAW_NOT_EXISTS;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 分销提现")
@RestController
@RequestMapping("/trade/brokerage-withdraw")
@Validated
@Slf4j
public class AppBrokerageWithdrawController {

    @Resource
    private BrokerageWithdrawService brokerageWithdrawService;

    @GetMapping("/page")
    @Operation(summary = "获得分销提现分页")
    public CommonResult<PageResult<AppBrokerageWithdrawRespVO>> getBrokerageWithdrawPage(AppBrokerageWithdrawPageReqVO pageReqVO) {
        PageResult<BrokerageWithdrawDO> pageResult = brokerageWithdrawService.getBrokerageWithdrawPage(
                BrokerageWithdrawConvert.INSTANCE.convert(pageReqVO, getLoginUserId()));
        return success(BrokerageWithdrawConvert.INSTANCE.convertPage03(pageResult));
    }

    @GetMapping("/get")
    @Operation(summary = "获得分销提现详情")
    public CommonResult<AppBrokerageWithdrawRespVO> getBrokerageWithdraw(@RequestParam("id") Long id) {
        BrokerageWithdrawDO withdraw = brokerageWithdrawService.getBrokerageWithdraw(id);
        Long loginUserId = getLoginUserId();
        if (withdraw == null || !Objects.equals(withdraw.getUserId(), loginUserId)) {
            throw exception(BROKERAGE_WITHDRAW_NOT_EXISTS);
        }
        return success(BrokerageWithdrawConvert.INSTANCE.convertApp04(withdraw));
    }

    @PostMapping("/create")
    @Operation(summary = "创建分销提现")
    public CommonResult<Long> createBrokerageWithdraw(@RequestBody @Valid AppBrokerageWithdrawCreateReqVO createReqVO) {
        return success(brokerageWithdrawService.createBrokerageWithdraw(getLoginUserId(), createReqVO));
    }

    @GetMapping("/account/last-list")
    @Operation(summary = "获得最近使用的提现账户列表")
    public CommonResult<List<AppBrokerageWithdrawAccountRespVO>> getLastWithdrawAccountList() {
        List<BrokerageWithdrawDO> withdrawList =
                brokerageWithdrawService.getLatestWithdrawAccounts(getLoginUserId());
        return success(BrokerageWithdrawConvert.INSTANCE.convertAccountList(withdrawList));
    }

}
