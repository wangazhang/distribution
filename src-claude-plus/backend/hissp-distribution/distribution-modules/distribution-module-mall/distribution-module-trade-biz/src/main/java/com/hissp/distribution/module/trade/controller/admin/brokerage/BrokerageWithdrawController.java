package com.hissp.distribution.module.trade.controller.admin.brokerage;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.apilog.core.annotation.ApiAccessLog;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.excel.core.util.ExcelUtils;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.pay.api.notify.dto.PayTransferNotifyReqDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelTransferRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelTransferRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawChannelRetryReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawConfirmPayReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawExportReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceExportExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceImportExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceImportRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawRejectReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawRespVO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageWithdrawConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import com.hissp.distribution.module.trade.framework.excel.BrokerageWithdrawFinanceFillStyleHandler;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageWithdrawService;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawFinanceImportRespBO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hissp.distribution.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.filterList;
import static com.hissp.distribution.framework.common.util.servlet.ServletUtils.getClientIP;

@Tag(name = "管理后台 - 佣金提现")
@RestController
@RequestMapping("/trade/brokerage-withdraw")
@Validated
@Slf4j
public class BrokerageWithdrawController {

    @Resource
    private BrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private MemberUserApi memberUserApi;

    @PutMapping("/approve")
    @Operation(summary = "通过申请")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:audit')")
    public CommonResult<Boolean> approveBrokerageWithdraw(@RequestParam("id") Long id) {
        brokerageWithdrawService.auditBrokerageWithdraw(id,
                BrokerageWithdrawStatusEnum.AUDIT_SUCCESS, "", getClientIP());
        return success(true);
    }

    @PutMapping("/reject")
    @Operation(summary = "驳回申请")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:audit')")
    public CommonResult<Boolean> rejectBrokerageWithdraw(@Valid @RequestBody BrokerageWithdrawRejectReqVO reqVO) {
        brokerageWithdrawService.auditBrokerageWithdraw(reqVO.getId(),
                BrokerageWithdrawStatusEnum.AUDIT_FAIL, reqVO.getAuditReason(), getClientIP());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得佣金提现")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:query')")
    public CommonResult<BrokerageWithdrawRespVO> getBrokerageWithdraw(@RequestParam("id") Long id) {
        BrokerageWithdrawDO brokerageWithdraw = brokerageWithdrawService.getBrokerageWithdraw(id);
        return success(BrokerageWithdrawConvert.INSTANCE.convert(brokerageWithdraw));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金提现分页")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:query')")
    public CommonResult<PageResult<BrokerageWithdrawRespVO>> getBrokerageWithdrawPage(@Valid BrokerageWithdrawPageReqVO pageVO) {
        // 分页查询
        PageResult<BrokerageWithdrawDO> pageResult = brokerageWithdrawService.getBrokerageWithdrawPage(pageVO);

        // 拼接信息
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                convertSet(pageResult.getList(), BrokerageWithdrawDO::getUserId));
        PageResult<BrokerageWithdrawRespVO> respPage = BrokerageWithdrawConvert.INSTANCE.convertPage(pageResult, userMap);
        Map<Long, Boolean> channelPayMap = new HashMap<>(pageResult.getList().size());
        pageResult.getList().forEach(item ->
                channelPayMap.put(item.getId(),
                        brokerageWithdrawService.isChannelPayEnabled(item.getUserId(), item.getType())));
        respPage.getList().forEach(vo ->
                vo.setChannelPayEnabled(Boolean.TRUE.equals(channelPayMap.get(vo.getId()))));
        respPage.getList().forEach(vo -> vo.setChannelPayLocked(
                brokerageWithdrawService.hasChannelTransferRecord(vo.getId())));
        return success(respPage);
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出佣金提现 Excel")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportBrokerageWithdrawExcel(@Valid BrokerageWithdrawExportReqVO exportReqVO,
                                             HttpServletResponse response) throws IOException {
        List<BrokerageWithdrawDO> list = brokerageWithdrawService.getBrokerageWithdrawList(exportReqVO);
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                convertSet(list, BrokerageWithdrawDO::getUserId));
        List<BrokerageWithdrawExcelVO> dataList = BrokerageWithdrawConvert.INSTANCE.convertExcelList(list, userMap);
        ExcelUtils.write(response, "brokerage_withdraw.xlsx", "数据", BrokerageWithdrawExcelVO.class, dataList);
    }

    @GetMapping("/export-finance-excel")
    @Operation(summary = "导出待打款提现单 Excel")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportFinanceBrokerageWithdrawExcel(@Valid BrokerageWithdrawExportReqVO exportReqVO,
                                                    HttpServletResponse response) throws IOException {
        exportReqVO.setStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus());
        List<BrokerageWithdrawDO> list = brokerageWithdrawService.getBrokerageWithdrawList(exportReqVO);
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                convertSet(list, BrokerageWithdrawDO::getUserId));
        List<BrokerageWithdrawFinanceExportExcelVO> dataList =
                BrokerageWithdrawConvert.INSTANCE.convertFinanceExcelList(list, userMap);
        dataList.forEach(item -> item.setFinanceStatus(BrokerageWithdrawStatusEnum.WITHDRAW_FINANCE_PAYING.getStatus()));
        ExcelUtils.write(response, "brokerage_withdraw_finance.xlsx", "数据",
                BrokerageWithdrawFinanceExportExcelVO.class, dataList,
                List.of(new BrokerageWithdrawFinanceFillStyleHandler()));
    }

    // TODO @luchi：update-transferred，url 改成这个。和 update-paid 、update-refunded 保持一致
    @PostMapping("/update-transfer")
    @Operation(summary = "更新转账订单为转账成功") // 由 pay-module 支付服务，进行回调，可见 PayNotifyJob
    @PermitAll // 无需登录，安全由 PayDemoOrderService 内部校验实现
    public CommonResult<Boolean> updateBrokerageWithdrawTransferred(@RequestBody PayTransferNotifyReqDTO notifyReqDTO) {
        log.info("[updateAfterRefund][notifyReqDTO({})]", notifyReqDTO);
        brokerageWithdrawService.updateBrokerageWithdrawTransferred(
                Long.parseLong(notifyReqDTO.getMerchantTransferId()), notifyReqDTO.getPayTransferId());
        return success(true);
    }

    @PutMapping("/confirm-pay")
    @Operation(summary = "财务确认打款")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:confirm')")
    public CommonResult<Boolean> confirmBrokerageWithdrawPay(@Valid @RequestBody BrokerageWithdrawConfirmPayReqVO reqVO) {
        brokerageWithdrawService.confirmFinancePay(reqVO.getId(), reqVO.getRemark());
        return success(true);
    }

    @PutMapping("/channel-pay")
    @Operation(summary = "渠道打款")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:channel-pay')")
    public CommonResult<Boolean> channelPay(@RequestParam("id") Long id) {
        brokerageWithdrawService.channelPay(id);
        return success(true);
    }

    @PostMapping("/channel-pay/retry")
    @Operation(summary = "渠道打款重新发起")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:channel-pay')")
    public CommonResult<BrokerageWithdrawChannelTransferRespVO> retryChannelPay(
            @Valid @RequestBody BrokerageWithdrawChannelRetryReqVO reqVO) {
        return success(brokerageWithdrawService.retryChannelPay(reqVO));
    }

    @GetMapping("/channel-transfer")
    @Operation(summary = "渠道打款详情")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:query')")
    public CommonResult<BrokerageWithdrawChannelTransferRespVO> getChannelTransfer(@RequestParam("id") Long id) {
        return success(brokerageWithdrawService.getChannelTransferDetail(id));
    }

    @PostMapping("/channel-transfer/sync")
    @Operation(summary = "渠道打款手动同步")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:channel-pay')")
    public CommonResult<BrokerageWithdrawChannelTransferRespVO> syncChannelTransfer(@RequestParam("id") Long id) {
        return success(brokerageWithdrawService.syncChannelTransfer(id));
    }

    @PostMapping("/import-finance-result")
    @Operation(summary = "导入财务打款结果")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-withdraw:import')")
    public CommonResult<BrokerageWithdrawFinanceImportRespVO> importFinanceResult(@RequestPart("file") MultipartFile file)
            throws Exception {
        log.info("[importFinanceResult][name={}, size={}]", file.getOriginalFilename(), file.getSize());
        List<BrokerageWithdrawFinanceImportExcelVO> dataList =
                ExcelUtils.read(file, BrokerageWithdrawFinanceImportExcelVO.class);
        log.info("[importFinanceResult][rows={}]", dataList.size());
        dataList = filterList(dataList, item -> item != null && (item.getId() != null
                || item.getFinanceStatus() != null || StrUtil.isNotBlank(item.getRemark())));
        log.info("[importFinanceResult][filteredRows={}]", dataList.size());
        BrokerageWithdrawFinanceImportRespBO respBO = brokerageWithdrawService.importFinanceResult(dataList);
        return success(BrokerageWithdrawConvert.INSTANCE.convert(respBO));
    }

}
