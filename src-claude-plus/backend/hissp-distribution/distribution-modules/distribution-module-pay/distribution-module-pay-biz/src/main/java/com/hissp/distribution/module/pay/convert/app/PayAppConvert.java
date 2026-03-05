package com.hissp.distribution.module.pay.convert.app;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.framework.common.util.collection.CollectionUtils;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppPageItemRespVO;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppRespVO;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppUpdateReqVO;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.dataobject.channel.PayChannelDO;
import com.hissp.distribution.framework.pay.core.enums.channel.PayChannelEnum;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * 支付应用信息 Convert
 *
 * @author 芋艿
 */
@Mapper
public interface PayAppConvert {

    PayAppConvert INSTANCE = Mappers.getMapper(PayAppConvert.class);

    PayAppPageItemRespVO pageConvert (PayAppDO bean);

    PayAppDO convert(PayAppCreateReqVO bean);

    PayAppDO convert(PayAppUpdateReqVO bean);

    PayAppRespVO convert(PayAppDO bean);

    List<PayAppRespVO> convertList(List<PayAppDO> list);

    PageResult<PayAppPageItemRespVO> convertPage(PageResult<PayAppDO> page);

    default PageResult<PayAppPageItemRespVO> convertPage(PageResult<PayAppDO> pageResult, List<PayChannelDO> channels) {
        PageResult<PayAppPageItemRespVO> voPageResult = convertPage(pageResult);
        // 处理 channel 关系
        Map<Long, Set<String>> appIdChannelMap = CollectionUtils.convertMultiMap2(channels, PayChannelDO::getAppId, PayChannelDO::getCode);
        Map<Long, PayChannelDO> channelIdMap = CollectionUtils.convertMap(channels, PayChannelDO::getId);
        voPageResult.getList().forEach(app -> {
            app.setChannelCodes(appIdChannelMap.get(app.getId()));
            if (app.getAccountChannelId() != null) {
                PayChannelDO accountChannel = channelIdMap.get(app.getAccountChannelId());
                if (accountChannel != null) {
                    PayChannelEnum channelEnum = PayChannelEnum.getByCode(accountChannel.getCode());
                    app.setAccountChannelName(channelEnum != null ? channelEnum.getName() : accountChannel.getCode());
                }
            }
        });
        return voPageResult;
    }

}
