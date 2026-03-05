package com.hissp.distribution.module.mb.domain.service.contentblock.impl;

import cn.hutool.core.util.StrUtil;
import com.hissp.distribution.module.mb.adapter.controller.app.homeconfig.vo.MbHomeConfigRespVO;
import com.hissp.distribution.module.mb.convert.homeconfig.MbHomeConfigConvert;
import com.hissp.distribution.module.mb.dal.dataobject.homeconfig.MbHomeConfigDO;
import com.hissp.distribution.module.mb.dal.mysql.homeconfig.MbHomeConfigMapper;
import com.hissp.distribution.module.mb.enums.MbHomeConfigStatusEnum;
import com.hissp.distribution.module.mb.domain.service.contentblock.MbContentBlockService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 医美内容区块 Service 实现类
 */
@Service
@Slf4j
@Deprecated
public class MbContentBlockServiceImpl implements MbContentBlockService {

    @Resource
    private MbHomeConfigMapper mbHomeConfigMapper;


    @Override
    public MbHomeConfigRespVO getHomeConfig(String version) {
        // 查询指定版本或当前生效的配置
        MbHomeConfigDO config;
        if (StrUtil.isNotEmpty(version)) {
            // 如果指定了版本，根据版本号查询
            config = mbHomeConfigMapper.selectByVersion(version);
        } else {
            // 如果未指定版本，查询当前生效的配置
            config = mbHomeConfigMapper.selectOneByStatus(MbHomeConfigStatusEnum.ENABLED.getStatus());
        }
        
        // 转换并返回
        if (config == null) {
            return null;
        }
        return MbHomeConfigConvert.INSTANCE.convert(config);
    }
}