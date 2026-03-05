package com.hissp.distribution.module.trade.controller.app.brokerage;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.framework.common.pojo.CommonResult.success;
import static com.hissp.distribution.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

import java.util.Objects;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hissp.distribution.framework.common.pojo.CommonResult;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.object.BeanUtils;
import com.hissp.distribution.module.member.api.user.MemberUserApi;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordBizDetailRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageProductPriceRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordDetailRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordPageAllTypesReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordRespVO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageRecordConvert;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.enums.ErrorCodeConstants;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageRecordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "用户 APP - 分销用户")
@RestController
@RequestMapping("/trade/brokerage-record")
@Validated
@Slf4j
public class AppBrokerageRecordController {
    @Resource
    private BrokerageRecordService brokerageRecordService;
    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得分销记录分页")
    public CommonResult<PageResult<AppBrokerageRecordRespVO>>
        getBrokerageRecordPage(@Valid AppBrokerageRecordPageReqVO pageReqVO) {
        PageResult<BrokerageRecordDO> pageResult = brokerageRecordService
            .getBrokerageRecordPage(BrokerageRecordConvert.INSTANCE.convert(pageReqVO, getLoginUserId()));
        return success(BeanUtils.toBean(pageResult, AppBrokerageRecordRespVO.class));
    }

    @GetMapping("/get-product-brokerage-price")
    @Operation(summary = "获得商品的分销金额")
    public CommonResult<AppBrokerageProductPriceRespVO> getProductBrokeragePrice(@RequestParam("spuId") Long spuId) {
        return success(brokerageRecordService.calculateProductBrokeragePrice(getLoginUserId(), spuId));
    }

    @GetMapping("/get")
    @Operation(summary = "获得分销记录详情")
    public CommonResult<AppBrokerageRecordDetailRespVO> getBrokerageRecord(@RequestParam("id") Long id) {
        BrokerageRecordDO record = brokerageRecordService.getBrokerageRecord(id);
        if (record == null || !Objects.equals(record.getUserId(), getLoginUserId())) {
            throw exception(ErrorCodeConstants.BROKERAGE_RECORD_NOT_EXISTS);
        }

        AppBrokerageRecordDetailRespVO respVO = buildDetailResp(record);
        if (record.getSourceUserId() != null) {
            MemberUserRespDTO sourceUser = memberUserApi.getUser(record.getSourceUserId());
            if (sourceUser != null) {
                respVO.setSourceUserNickname(sourceUser.getNickname());
                respVO.setSourceUserAvatar(sourceUser.getAvatar());
            }
        }
        return success(respVO);
    }

    private AppBrokerageRecordDetailRespVO buildDetailResp(BrokerageRecordDO record) {
        AppBrokerageRecordDetailRespVO respVO = new AppBrokerageRecordDetailRespVO();
        if (record.getId() != null) {
            respVO.setId(record.getId().longValue());
        }
        respVO.setBizId(record.getBizId());
        respVO.setBizType(record.getBizType());
        BrokerageRecordBizTypeEnum bizTypeEnum = BrokerageRecordBizTypeEnum.fromType(record.getBizType());
        respVO.setBizTypeName(bizTypeEnum != null ? bizTypeEnum.getTitle() : null);
        respVO.setTitle(record.getTitle());
        respVO.setDescription(record.getDescription());
        respVO.setPrice(record.getPrice());
        respVO.setStatus(record.getStatus());
        BrokerageRecordStatusEnum statusEnum = BrokerageRecordStatusEnum.fromStatus(record.getStatus());
        respVO.setStatusName(statusEnum != null ? statusEnum.getName() : null);
        respVO.setFrozenDays(record.getFrozenDays());
        respVO.setUnfreezeTime(record.getUnfreezeTime());
        respVO.setCreateTime(record.getCreateTime());
        respVO.setUpdateTime(record.getUpdateTime());
        respVO.setSourceUserId(record.getSourceUserId());
        respVO.setSourceUserLevel(record.getSourceUserLevel());
        return respVO;
    }

    @GetMapping("/mobile/biz-detail")
    @Operation(summary = "获得分销记录业务详情（移动端）")
    public CommonResult<BrokerageRecordBizDetailRespVO> getMobileBizDetail(@RequestParam("id") Long id) {
        BrokerageRecordDO record = brokerageRecordService.getBrokerageRecord(id);
        if (record == null || !Objects.equals(record.getUserId(), getLoginUserId())) {
            throw exception(ErrorCodeConstants.BROKERAGE_RECORD_NOT_EXISTS);
        }
        return success(brokerageRecordService.getBrokerageRecordBizDetail(id));
    }

}
