package com.hissp.distribution.module.trade.api.brokerage;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserCreateOrUpdateDTO;
import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserRespDTO;
import com.hissp.distribution.module.trade.convert.brokerage.BrokerageUserConvert;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.hissp.distribution.module.trade.api.brokerage.dto.BrokerageUserCreateDTO;
import com.hissp.distribution.module.trade.dal.dataobject.brokerage.BrokerageUserDO;
import com.hissp.distribution.module.trade.service.brokerage.BrokerageUserService;

import jakarta.annotation.Resource;

/**
 * 分销用户 API 接口实现类
 */
@Service
@Validated
public class BrokerageUserApiImpl implements BrokerageUserApi {

    @Resource
    private BrokerageUserService brokerageUserService;

    @Override
    public Long createOrEnableBrokerageUser(BrokerageUserCreateDTO createReqDTO) {
        BrokerageUserDO brokerageUserDO = new BrokerageUserDO();
        brokerageUserDO.setId(createReqDTO.getUserId()).setBindUserId(createReqDTO.getBindUserId())
            .setBrokerageEnabled(createReqDTO.getBrokerageEnabled());
        // 调用 Service 创建 .setBrokerageEnabled(createReqDTO.getBrokerageEnabled()) 无效，createOrEnableBrokerageUser 会自动设置
        return brokerageUserService.createOrEnableBrokerageUser(brokerageUserDO);
    }

    @Override
    public BrokerageUserRespDTO getBindBrokerageUser(Long userId) {
        BrokerageUserDO brokerageUserDO = brokerageUserService.getBindBrokerageUser(userId);
        return convertToDTO(brokerageUserDO);
    }

    @Override
    public BrokerageUserRespDTO getBrokerageUser(Long userId) {
        BrokerageUserDO brokerageUserDO = brokerageUserService.getBrokerageUser(userId);
        return convertToDTO(brokerageUserDO);
    }

    @Override
    public BrokerageUserRespDTO getFirstBossBrokerageUser(Long userId) {
        BrokerageUserDO brokerageUserDO = brokerageUserService.getFirstBossBrokerageUser(userId);
        return convertToDTO(brokerageUserDO);
    }

    @Override
    public Long getBrokerageUserCountByBindUserId(Long bindUserId, Integer level) {
        return brokerageUserService.getBrokerageUserCountByBindUserId(bindUserId, level);
    }

    /**
     * 将 BrokerageUserDO 转换为 BrokerageUserRespDTO
     * 
     * @param brokerageUserDO 分销用户DO
     * @return 分销用户DTO
     */
    private BrokerageUserRespDTO convertToDTO(BrokerageUserDO brokerageUserDO) {
        if (brokerageUserDO == null) {
            return null;
        }
        BrokerageUserRespDTO brokerageUserRespDTO = new BrokerageUserRespDTO();
        BeanUtils.copyProperties(brokerageUserDO, brokerageUserRespDTO);
        return brokerageUserRespDTO;
    }

    @Override
    public Integer countAgentUserByBindUserAndLevelId(Long userId, Long levelId) {
        return brokerageUserService.countAgentUserByBindUserAndLevelId(userId, levelId);
    }

    @Override
    public void updateBrokerageUserEnabled(Long userId, Boolean enable) {
        brokerageUserService.updateBrokerageUserEnabled(userId, enable);
    }
    
    @Override
    public void updateUserLevel(Long userId, Long levelId) {
        brokerageUserService.updateUserLevel(userId, levelId);
    }

    @Override
    public void createOrUpdateBrokerageUser(BrokerageUserCreateOrUpdateDTO brokerageUserCreateOrUpdateDTO) {
        brokerageUserService.createOrUpdateBrokerageUser(BrokerageUserConvert.INSTANCE.convert(brokerageUserCreateOrUpdateDTO));
    }

    @Override
    public Integer getInviteOrderIndex(Long userId) {
        // 直接复用 service 层逻辑，保持所有排序规则集中在 trade 模块
        return brokerageUserService.getInviteOrderIndex(userId);
    }
}
