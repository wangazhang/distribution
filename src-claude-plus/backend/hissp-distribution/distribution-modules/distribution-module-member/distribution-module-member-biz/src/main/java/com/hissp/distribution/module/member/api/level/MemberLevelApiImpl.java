package com.hissp.distribution.module.member.api.level;

import com.hissp.distribution.module.member.api.level.dto.MemberLevelReqDTO;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.controller.admin.user.vo.MemberUserUpdateLevelReqVO;
import com.hissp.distribution.module.member.convert.level.MemberLevelConvert;
import com.hissp.distribution.module.member.enums.MemberExperienceBizTypeEnum;
import com.hissp.distribution.module.member.service.level.MemberLevelService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.hissp.distribution.framework.common.util.collection.CollectionUtils.convertMap;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static com.hissp.distribution.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.hissp.distribution.module.member.enums.ErrorCodeConstants.EXPERIENCE_BIZ_NOT_SUPPORT;

/**
 * 会员等级 API 实现类
 *
 * @author owen
 */
@Slf4j
@Service
@Validated
public class MemberLevelApiImpl implements MemberLevelApi {

    @Resource
    private MemberLevelService memberLevelService;

    @Override
    public MemberLevelRespDTO getMemberLevel(Long id) {
        return MemberLevelConvert.INSTANCE.convert02(memberLevelService.getLevel(id));
    }

    @Override
    public List<MemberLevelRespDTO> getMemberLevelList(Collection<Long> ids) {
        return MemberLevelConvert.INSTANCE.convertList03(memberLevelService.getLevelList(ids));
    }

    @Override
    public Map<Long, MemberLevelRespDTO> getMemberLevelMap(Collection<Long> ids) {
        List<MemberLevelRespDTO> list = getMemberLevelList(ids);
        return convertMap(list, MemberLevelRespDTO::getId);
    }

    @Override
    public List<MemberLevelRespDTO> getEnableMemberLevelList() {
        return MemberLevelConvert.INSTANCE.convertList03(memberLevelService.getEnableLevelList());
    }

    @Override
    public void addExperience(Long userId, Integer experience, Integer bizType, String bizId) {
        MemberExperienceBizTypeEnum bizTypeEnum = MemberExperienceBizTypeEnum.getByType(bizType);
        if (bizTypeEnum == null) {
            throw exception(EXPERIENCE_BIZ_NOT_SUPPORT);
        }
        memberLevelService.addExperience(userId, experience, bizTypeEnum, bizId);
    }

    @Override
    public void reduceExperience(Long userId, Integer experience, Integer bizType, String bizId) {
        addExperience(userId, -experience, bizType, bizId);
    }

    @Override
    public void updateUserLevel(MemberLevelReqDTO userLevelDTO) {
        MemberUserUpdateLevelReqVO memberUserUpdateLevelReqVO = new MemberUserUpdateLevelReqVO()
            .setEnableBrokerage(userLevelDTO.getEnableBrokerage()).setLevelId(userLevelDTO.getLevelId())
            .setId(userLevelDTO.getUserId()).setAdminOp(userLevelDTO.getAdminOp()).setReason(userLevelDTO.getReason())
            .setBizId(userLevelDTO.getBizId());
        memberLevelService.updateUserLevel(memberUserUpdateLevelReqVO);
    }

}
