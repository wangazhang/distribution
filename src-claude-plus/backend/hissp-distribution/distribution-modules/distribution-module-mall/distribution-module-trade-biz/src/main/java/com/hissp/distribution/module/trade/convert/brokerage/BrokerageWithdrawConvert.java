package com.hissp.distribution.module.trade.convert.brokerage;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.dict.core.DictFrameworkUtils;
import com.hissp.distribution.module.member.api.user.dto.MemberUserRespDTO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceExportExcelVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawFinanceImportRespVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import com.hissp.distribution.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawAccountRespVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawPageReqVO;
import com.hissp.distribution.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawRespVO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import com.hissp.distribution.module.trade.enums.DictTypeConstants;
import com.hissp.distribution.module.trade.service.brokerage.bo.BrokerageWithdrawFinanceImportRespBO;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 佣金提现 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface BrokerageWithdrawConvert {

    BrokerageWithdrawConvert INSTANCE = Mappers.getMapper(BrokerageWithdrawConvert.class);

    BrokerageWithdrawDO convert(AppBrokerageWithdrawCreateReqVO createReqVO, Long userId, Integer feePrice);

    BrokerageWithdrawRespVO convert(BrokerageWithdrawDO bean);

    List<BrokerageWithdrawRespVO> convertList(List<BrokerageWithdrawDO> list);

    PageResult<BrokerageWithdrawRespVO> convertPage(PageResult<BrokerageWithdrawDO> page);

    List<BrokerageWithdrawExcelVO> convertExcelList(List<BrokerageWithdrawDO> list);

    List<BrokerageWithdrawFinanceExportExcelVO> convertFinanceExcelList(List<BrokerageWithdrawDO> list);

    BrokerageWithdrawFinanceImportRespVO convert(BrokerageWithdrawFinanceImportRespBO bo);

    default PageResult<BrokerageWithdrawRespVO> convertPage(PageResult<BrokerageWithdrawDO> pageResult, Map<Long, MemberUserRespDTO> userMap) {
        PageResult<BrokerageWithdrawRespVO> result = convertPage(pageResult);
        for (BrokerageWithdrawRespVO vo : result.getList()) {
            vo.setUserNickname(Optional.ofNullable(userMap.get(vo.getUserId())).map(MemberUserRespDTO::getNickname).orElse(null));
        }
        return result;
    }

    default List<BrokerageWithdrawExcelVO> convertExcelList(List<BrokerageWithdrawDO> list,
                                                            Map<Long, MemberUserRespDTO> userMap) {
        List<BrokerageWithdrawExcelVO> excelVOS = convertExcelList(list);
        excelVOS.forEach(vo -> vo.setUserNickname(Optional.ofNullable(userMap.get(vo.getUserId()))
                .map(MemberUserRespDTO::getNickname).orElse(null)));
        return excelVOS;
    }

    default List<BrokerageWithdrawFinanceExportExcelVO> convertFinanceExcelList(List<BrokerageWithdrawDO> list,
                                                                               Map<Long, MemberUserRespDTO> userMap) {
        List<BrokerageWithdrawFinanceExportExcelVO> excelVOS = convertFinanceExcelList(list);
        excelVOS.forEach(vo -> vo.setUserNickname(Optional.ofNullable(userMap.get(vo.getUserId()))
                .map(MemberUserRespDTO::getNickname).orElse(null)));
        return excelVOS;
    }
    @IterableMapping(qualifiedByName = "convertApp")
    PageResult<AppBrokerageWithdrawRespVO> convertPage07(PageResult<BrokerageWithdrawDO> pageResult);

    default PageResult<AppBrokerageWithdrawRespVO> convertPage03(PageResult<BrokerageWithdrawDO> pageResult) {
        PageResult<AppBrokerageWithdrawRespVO> result = convertPage07(pageResult);
        for (AppBrokerageWithdrawRespVO vo : result.getList()) {
            fillAppDictData(vo);
        }
        return result;
    }
    @Named("convertApp")
    AppBrokerageWithdrawRespVO convertApp(BrokerageWithdrawDO bean);

    default AppBrokerageWithdrawRespVO convertApp04(BrokerageWithdrawDO bean) {
        AppBrokerageWithdrawRespVO vo = convertApp(bean);
        fillAppDictData(vo);
        return vo;
    }

    List<AppBrokerageWithdrawAccountRespVO> convertAccountList(List<BrokerageWithdrawDO> list);

    BrokerageWithdrawPageReqVO convert(AppBrokerageWithdrawPageReqVO pageReqVO, Long userId);

    default void fillAppDictData(AppBrokerageWithdrawRespVO vo) {
        if (vo == null) {
            return;
        }
        vo.setStatusName(DictFrameworkUtils.getDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS, vo.getStatus()));
        vo.setTypeName(DictFrameworkUtils.getDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_TYPE, vo.getType()));
    }
}
