package com.hissp.distribution.module.pay.controller.admin.order;

import cn.hutool.core.collection.CollectionUtil;
import com.hissp.distribution.framework.apilog.core.annotation.ApiAccessLog;
import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.framework.excel.core.util.ExcelUtils;
import com.hissp.distribution.framework.pay.core.client.dto.order.PayOrderInnerRespDTO;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import com.hissp.distribution.module.pay.controller.admin.order.vo.*;
import com.hissp.distribution.module.pay.convert.order.PayOrderConvert;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.order.PayOrderDO;

import com.hissp.distribution.module.pay.enums.order.PayOrderStatusEnum;
import com.hissp.distribution.module.pay.framework.pay.core.WalletPayClient;
import com.hissp.distribution.module.pay.service.app.PayAppService;
import com.hissp.distribution.module.pay.service.order.PayOrderService;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.hissp.distribution.framework.apilog.core.enums.OperateTypeEnum.EXPORT;
import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertList;
import static com.hissp.distribution.framework.common.util.servlet.ServletUtils.getClientIP;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserType;

@Tag(name = "管理后台 - 支付订单")
@RestController
@RequestMapping("/pay/order")
@Validated
public class PayOrderController {

    @Resource
    private PayOrderService orderService;
    @Resource
    private PayAppService appService;

    @GetMapping("/get")
    @Operation(summary = "获得支付订单")
    @Parameters({
            @Parameter(name = "id", description = "编号", required = true, example = "1024"),
            @Parameter(name = "sync", description = "是否同步", example = "true")
    })
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderRespVO> getOrder(@RequestParam("id") Long id,
                                                 @RequestParam(value = "sync", required = false) Boolean sync) {
        PayOrderDO order = orderService.getOrder(id);
        // sync 仅在等待支付
        return success(BeanUtils.toBean(order, PayOrderRespVO.class));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得支付订单详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderDetailsRespVO> getOrderDetail(@RequestParam("id") Long id) {
        PayOrderDO order = orderService.getOrder(id);
        if (order == null) {
            return success(null);
        }

        // 拼接返回
        PayAppDO app = appService.getApp(order.getAppId());
        return success(PayOrderConvert.INSTANCE.convert(order, app));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交支付订单")
    public CommonResult<PayOrderSubmitRespVO> submitPayOrder(@RequestBody PayOrderSubmitReqVO reqVO) {
        // 1. 钱包支付事，需要额外传 user_id 和 user_type
        if (Objects.equals(reqVO.getChannelCode(), PayChannelEnum.WALLET.getCode())) {
            Map<String, String> channelExtras = reqVO.getChannelExtras() == null ?
                    Maps.newHashMapWithExpectedSize(2) : reqVO.getChannelExtras();
            channelExtras.put(WalletPayClient.USER_ID_KEY, String.valueOf(getLoginUserId()));
            channelExtras.put(WalletPayClient.USER_TYPE_KEY, String.valueOf(getLoginUserType()));
            reqVO.setChannelExtras(channelExtras);
        }

        // 2. 提交支付
        PayOrderSubmitRespVO respVO = orderService.submitOrder(reqVO, getClientIP());
        return success(respVO);
    }

    @GetMapping("/{id}/channel-order")
    @Operation(summary = "查询渠道侧订单信息")
    @Parameter(name = "id", description = "支付订单编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PayOrderChannelRespVO> queryChannelOrder(@PathVariable("id") Long id) {
        // 统一在 Service 中完成渠道调用，这里只负责转换 VO 并补齐可读字段。
        PayOrderInnerRespDTO respDTO = orderService.queryChannelOrder(id);
        PayOrderChannelRespVO respVO = new PayOrderChannelRespVO();
        respVO.setStatus(respDTO.getStatus());
        respVO.setDeliveryStatus(respDTO.getDeliveryStatus() != null ? respDTO.getDeliveryStatus().getCode() : null);
        respVO.setOutTradeNo(respDTO.getOutTradeNo());
        respVO.setChannelOrderNo(respDTO.getChannelOrderNo());
        respVO.setChannelTransactionNo(respDTO.getChannelTransactionNo());
        respVO.setChannelUserId(respDTO.getChannelUserId());
        respVO.setSuccessTime(respDTO.getSuccessTime());
        respVO.setRawData(respDTO.getRawData());
        respVO.fillStatusName();
        respVO.fillDeliveryStatusName();
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得支付订单分页")
    @PreAuthorize("@ss.hasPermission('pay:order:query')")
    public CommonResult<PageResult<PayOrderPageItemRespVO>> getOrderPage(@Valid PayOrderPageReqVO pageVO) {
        PageResult<PayOrderDO> pageResult = orderService.getOrderPage(pageVO);
        if (CollectionUtil.isEmpty(pageResult.getList())) {
            return success(new PageResult<>(pageResult.getTotal()));
        }

        // 拼接返回
        Map<Long, PayAppDO> appMap = appService.getAppMap(convertList(pageResult.getList(), PayOrderDO::getAppId));
        return success(PayOrderConvert.INSTANCE.convertPage(pageResult, appMap));
    }

    @GetMapping("/export-excel")
    @Operation(summary = "导出支付订单 Excel")
    @PreAuthorize("@ss.hasPermission('pay:order:export')")
    @ApiAccessLog(operateType = EXPORT)
    public void exportOrderExcel(@Valid PayOrderExportReqVO exportReqVO,
            HttpServletResponse response) throws IOException {
        List<PayOrderDO> list = orderService.getOrderList(exportReqVO);
        if (CollectionUtil.isEmpty(list)) {
            ExcelUtils.write(response, "支付订单.xls", "数据",
                    PayOrderExcelVO.class, new ArrayList<>());
            return;
        }

        // 拼接返回
        Map<Long, PayAppDO> appMap = appService.getAppMap(convertList(list, PayOrderDO::getAppId));
        List<PayOrderExcelVO> excelList = PayOrderConvert.INSTANCE.convertList(list, appMap);
        // 导出 Excel
        ExcelUtils.write(response, "支付订单.xls", "数据", PayOrderExcelVO.class, excelList);
    }

}
