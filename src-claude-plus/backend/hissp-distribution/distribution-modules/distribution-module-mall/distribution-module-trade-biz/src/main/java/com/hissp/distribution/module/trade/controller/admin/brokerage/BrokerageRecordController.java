package com.hissp.distribution.module.trade.controller.admin.brokerage;

import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertList;
import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertSet;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordBizDetailRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageAllTypesReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageStatisticsRespVO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageRecordConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageStatisticsRespBO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

@Tag(name = "管理后台 - 佣金记录")
@RestController
@RequestMapping("/trade/brokerage-record")
@Validated
public class BrokerageRecordController {

    @Resource
    private BrokerageRecordService brokerageRecordService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/get")
    @Operation(summary = "获得佣金记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-record:query')")
    public CommonResult<BrokerageRecordRespVO> getBrokerageRecord(@RequestParam("id") Long id) {
        BrokerageRecordDO brokerageRecord = brokerageRecordService.getBrokerageRecord(id);
        return success(BrokerageRecordConvert.INSTANCE.convert(brokerageRecord));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金记录分页")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-record:query')")
    public CommonResult<PageResult<BrokerageRecordRespVO>> getBrokerageRecordPage(@Valid BrokerageRecordPageReqVO pageVO) {
        PageResult<BrokerageRecordDO> pageResult = brokerageRecordService.getBrokerageRecordPage(pageVO);

        // 查询用户信息
        Set<Long> userIds = convertSet(pageResult.getList(), BrokerageRecordDO::getUserId);
        userIds.addAll(convertList(pageResult.getList(), BrokerageRecordDO::getSourceUserId));
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(userIds);
        // 拼接数据
        return success(BrokerageRecordConvert.INSTANCE.convertPage(pageResult, userMap));
    }

    @GetMapping("/biz-detail")
    @Operation(summary = "获得佣金记录业务详情")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-record:query')")
    public CommonResult<BrokerageRecordBizDetailRespVO> getBrokerageRecordBizDetail(@RequestParam("id") Long id) {
        return success(brokerageRecordService.getBrokerageRecordBizDetail(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得用户佣金统计")
    @Parameter(name = "userId", description = "用户编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-record:query')")
    public CommonResult<BrokerageStatisticsRespVO> getBrokerageStatistics(@RequestParam("userId") Long userId) {
        BrokerageStatisticsRespBO statisticsBO = brokerageRecordService.getUserBrokerageStatistics(userId);
        BrokerageStatisticsRespVO statisticsVO = new BrokerageStatisticsRespVO();
        statisticsVO.setTotalIncome(statisticsBO.getTotalIncome());
        statisticsVO.setTotalExpense(statisticsBO.getTotalExpense());
        statisticsVO.setTotalCount(statisticsBO.getTotalCount());
        statisticsVO.setPendingCount(statisticsBO.getPendingCount());
        statisticsVO.setPendingAmount(statisticsBO.getPendingAmount());
        statisticsVO.setSettledCount(statisticsBO.getSettledCount());
        statisticsVO.setSettledAmount(statisticsBO.getSettledAmount());
        return success(statisticsVO);
    }

}
