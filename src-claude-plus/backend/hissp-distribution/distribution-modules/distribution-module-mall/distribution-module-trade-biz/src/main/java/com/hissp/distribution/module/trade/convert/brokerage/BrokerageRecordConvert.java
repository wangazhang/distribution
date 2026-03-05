package com.hissp.distribution.module.trade.convert.brokerage;

import cn.hutool.core.math.Money;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.number.MoneyUtils;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageRecordCreateReqDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageAllTypesReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.record.BrokerageRecordRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordPageAllTypesReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.record.AppBrokerageRecordRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.user.AppBrokerageUserRankByPriceRespVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageRecordDO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizCategoryEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordBizTypeEnum;
import com.hissp.distribution.module.trade.enums.brokerage.BrokerageRecordStatusEnum;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 佣金记录 Convert
 *
 * @author owen
 */
@Mapper
public interface BrokerageRecordConvert {

    BrokerageRecordConvert INSTANCE = Mappers.getMapper(BrokerageRecordConvert.class);

    BrokerageRecordRespVO convert(BrokerageRecordDO bean);

    List<BrokerageRecordRespVO> convertList(List<BrokerageRecordDO> list);

    PageResult<BrokerageRecordRespVO> convertPage(PageResult<BrokerageRecordDO> page);

    default BrokerageRecordDO convert(BrokerageUserDO user, BrokerageRecordBizTypeEnum bizType, String bizId,
        Integer brokerageFrozenDays, int brokeragePrice, LocalDateTime unfreezeTime, String title, Long sourceUserId,
        Integer sourceUserLevel) {
        brokerageFrozenDays = ObjectUtil.defaultIfNull(brokerageFrozenDays, 0);
        // 不冻结时，佣金直接就是结算状态
        Integer status = brokerageFrozenDays > 0 ? BrokerageRecordStatusEnum.WAIT_SETTLEMENT.getStatus()
            : BrokerageRecordStatusEnum.SETTLEMENT.getStatus();
        return new BrokerageRecordDO().setUserId(user.getId()).setBizType(bizType.getType()).setBizId(bizId)
            .setPrice(brokeragePrice).setTotalPrice(user.getBrokeragePrice()).setTitle(title)
            .setDescription(StrUtil.format(bizType.getDescription(), MoneyUtils.fenToYuanStr(Math.abs(brokeragePrice))))
            .setStatus(status).setFrozenDays(brokerageFrozenDays).setUnfreezeTime(unfreezeTime)
            .setSourceUserLevel(sourceUserLevel).setSourceUserId(sourceUserId)
            .setBizCategory(BrokerageRecordBizCategoryEnum.MALL_ORDER.getType());
    }

    default PageResult<BrokerageRecordRespVO> convertPage(PageResult<BrokerageRecordDO> pageResult,
        Map<Long, MemberUserRespDTO> userMap) {
        PageResult<BrokerageRecordRespVO> result = convertPage(pageResult);
        for (BrokerageRecordRespVO respVO : result.getList()) {
            Optional.ofNullable(userMap.get(respVO.getUserId()))
                .ifPresent(user -> respVO.setUserNickname(user.getNickname()).setUserAvatar(user.getAvatar()));
            Optional.ofNullable(userMap.get(respVO.getSourceUserId())).ifPresent(
                user -> respVO.setSourceUserNickname(user.getNickname()).setSourceUserAvatar(user.getAvatar()));
        }
        return result;
    }

    BrokerageRecordPageReqVO convert(AppBrokerageRecordPageReqVO pageReqVO, Long userId);

    BrokerageRecordPageAllTypesReqVO convert(AppBrokerageRecordPageAllTypesReqVO pageReqVO, Long userId);

    default PageResult<AppBrokerageUserRankByPriceRespVO>
        convertPage03(PageResult<AppBrokerageUserRankByPriceRespVO> pageResult, Map<Long, MemberUserRespDTO> userMap) {
        for (AppBrokerageUserRankByPriceRespVO vo : pageResult.getList()) {
            copyTo(userMap.get(vo.getId()), vo);
        }
        return pageResult;
    }

    void copyTo(MemberUserRespDTO from, @MappingTarget AppBrokerageUserRankByPriceRespVO to);

    default BrokerageRecordDO convert(BrokerageRecordCreateReqDTO recordDTO) {
        BrokerageRecordBizTypeEnum bizTypeEnum = BrokerageRecordBizTypeEnum.fromType(recordDTO.getBizType());
        String title = StrUtil.isNotBlank(recordDTO.getTitle())
            ? recordDTO.getTitle()
            : (bizTypeEnum != null ? bizTypeEnum.getTitle() : "");
        String descriptionTemplate = StrUtil.isNotBlank(recordDTO.getDescription())
            ? recordDTO.getDescription()
            : (bizTypeEnum != null ? bizTypeEnum.getDescription() : "");
        String description = descriptionTemplate.contains("{}")
            ? StrUtil.format(descriptionTemplate, MoneyUtils.fenToYuanStr(Math.abs(recordDTO.getPrice())))
            : descriptionTemplate;
        Integer bizCategory = recordDTO.getBizCategory();
        if (bizCategory == null) {
            bizCategory = BrokerageRecordBizCategoryEnum.UNKNOWN.getType();
        }
        return new BrokerageRecordDO().setUserId(recordDTO.getUserId()).setBizType(recordDTO.getBizType())
            .setBizCategory(bizCategory)
            .setBizId(recordDTO.getBizId()).setPrice(recordDTO.getPrice()).setTotalPrice(recordDTO.getPrice())
            .setTitle(title).setDescription(description)
            .setStatus(recordDTO.getStatus()).setFrozenDays(recordDTO.getFrozenDays())
            .setUnfreezeTime(recordDTO.getUnfreezeTime()).setSourceUserLevel(recordDTO.getSourceUserLevel())
            .setSourceUserId(recordDTO.getSourceUserId());
    }
}
