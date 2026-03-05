package com.hissp.distribution.module.pay.service.app;

import com.hissp.distribution.framework.common.enums.CommonStatusEnum;
import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppCreateReqVO;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppPageReqVO;
import com.hissp.distribution.module.pay.controller.admin.app.vo.PayAppUpdateReqVO;
import com.hissp.distribution.module.pay.convert.app.PayAppConvert;
import com.hissp.distribution.module.pay.dal.dataobject.app.PayAppDO;
import com.hissp.distribution.module.pay.dal.mysql.app.PayAppMapper;
import com.hissp.distribution.module.pay.enums.ErrorCodeConstants;
import com.hissp.distribution.module.pay.dal.dataobject.channel.PayChannelDO;
import com.hissp.distribution.module.pay.service.channel.PayChannelService;
import com.hissp.distribution.module.pay.service.order.PayOrderService;
import com.hissp.distribution.module.pay.service.refund.PayRefundService;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.pay.enums.ErrorCodeConstants.*;

/**
 * 支付应用 Service 实现类
 *
 * @author aquan
 */
@Service
@Validated
public class PayAppServiceImpl implements PayAppService {

    @Resource
    private PayAppMapper appMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖报错
    private PayOrderService orderService;
    @Resource
    @Lazy // 延迟加载，避免循环依赖报错
    private PayRefundService refundService;
    @Resource
    private PayChannelService channelService;

    @Override
    public Long createApp(PayAppCreateReqVO createReqVO) {
        // 验证 appKey 是否重复
        validateAppKeyUnique(null, createReqVO.getAppKey());

        // 插入
        PayAppDO app = PayAppConvert.INSTANCE.convert(createReqVO);
        appMapper.insert(app);
        if (createReqVO.getAccountChannelId() != null) {
            Long normalizedChannelId = normalizeAccountChannel(app.getId(), createReqVO.getAccountChannelId());
            appMapper.updateById(new PayAppDO().setId(app.getId()).setAccountChannelId(normalizedChannelId));
        }
        // 返回
        return app.getId();
    }

    @Override
    public void updateApp(PayAppUpdateReqVO updateReqVO) {
        // 校验存在
        validateAppExists(updateReqVO.getId());
        // 验证 appKey 是否重复
        validateAppKeyUnique(updateReqVO.getId(), updateReqVO.getAppKey());

        // 更新
        PayAppDO updateObj = PayAppConvert.INSTANCE.convert(updateReqVO);
        updateObj.setAccountChannelId(normalizeAccountChannel(updateReqVO.getId(), updateReqVO.getAccountChannelId()));
        appMapper.updateById(updateObj);
    }

    void validateAppKeyUnique(Long id, String appKey) {
        PayAppDO app = appMapper.selectByAppKey(appKey);
        if (app == null) {
            return;
        }
        // 如果 id 为空，说明不用比较是否为相同 appKey 的应用
        if (id == null) {
            throw exception(APP_KEY_EXISTS);
        }
        if (!app.getId().equals(id)) {
            throw exception(APP_KEY_EXISTS);
        }
    }

    @Override
    public void updateAppStatus(Long id, Integer status) {
        // 校验商户存在
        validateAppExists(id);
        // 更新状态
        appMapper.updateById(new PayAppDO().setId(id).setStatus(status));
    }

    @Override
    public void deleteApp(Long id) {
        // 校验存在
        validateAppExists(id);
        // 校验关联数据是否存在
        if (orderService.getOrderCountByAppId(id) > 0) {
            throw exception(APP_EXIST_ORDER_CANT_DELETE);
        }
        if (refundService.getRefundCountByAppId(id) > 0) {
            throw exception(APP_EXIST_REFUND_CANT_DELETE);
        }

        // 删除
        appMapper.deleteById(id);
    }

    private void validateAppExists(Long id) {
        if (appMapper.selectById(id) == null) {
            throw exception(APP_NOT_FOUND);
        }
    }

    @Override
    public PayAppDO getApp(Long id) {
        return appMapper.selectById(id);
    }

    @Override
    public List<PayAppDO> getAppList(Collection<Long> ids) {
        return appMapper.selectBatchIds(ids);
    }

    @Override
    public List<PayAppDO> getAppList() {
        return appMapper.selectList();
    }

    @Override
    public PageResult<PayAppDO> getAppPage(PayAppPageReqVO pageReqVO) {
        return appMapper.selectPage(pageReqVO);
    }

    @Override
    public PayAppDO validPayApp(Long appId) {
        PayAppDO app = appMapper.selectById(appId);
        return validatePayApp(app);
    }

    @Override
    public PayAppDO validPayApp(String appKey) {
        PayAppDO app = appMapper.selectByAppKey(appKey);
        return validatePayApp(app);
    }

    /**
     * 校验支付应用实体的有效性：存在 + 开启
     *
     * @param app 待校验的支付应用实体
     * @return 校验通过的支付应用实体
     */
    private PayAppDO validatePayApp(PayAppDO app) {
        // 校验是否存在
        if (app == null) {
            throw exception(ErrorCodeConstants.APP_NOT_FOUND);
        }
        // 校验是否禁用
        if (CommonStatusEnum.isDisable(app.getStatus())) {
            throw exception(ErrorCodeConstants.APP_IS_DISABLE);
        }
        return app;
    }

    private Long normalizeAccountChannel(Long appId, Long accountChannelId) {
        if (accountChannelId == null) {
            return null;
        }
        PayChannelDO channel = channelService.validPayChannel(accountChannelId);
        if (!appId.equals(channel.getAppId())) {
            throw exception(APP_ACCOUNT_CHANNEL_NOT_SAME_APP);
        }
        if (!Integer.valueOf(2).equals(channel.getType())) {
            throw exception(APP_ACCOUNT_CHANNEL_TYPE_ERROR);
        }
        return accountChannelId;
    }

}
