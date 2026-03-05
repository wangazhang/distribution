package com.hissp.distribution.module.mb.domain.service.contentblock;

import com.hissp.distribution.module.mb.adapter.controller.app.homeconfig.vo.MbHomeConfigRespVO;

/**
 * 医美内容区块 Service 接口
 */
@Deprecated
public interface MbContentBlockService {

//    /**
//     * 获取所有内容区块
//     *
//     * @return 内容区块列表
//     */
//    List<MbContentBlockRespVO> getAllContentBlocks();

    /**
     * 获取首页配置
     *
     * @param version 配置版本号，为空则获取当前生效配置
     * @return 首页配置
     */
    MbHomeConfigRespVO getHomeConfig(String version);
} 