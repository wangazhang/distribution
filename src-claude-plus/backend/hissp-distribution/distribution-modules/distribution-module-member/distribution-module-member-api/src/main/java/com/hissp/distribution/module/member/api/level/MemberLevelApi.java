package com.hissp.distribution.module.member.api.level;

import com.hissp.distribution.module.member.api.level.dto.MemberLevelReqDTO;
import com.hissp.distribution.module.member.api.level.dto.MemberLevelRespDTO;
import com.hissp.distribution.module.member.enums.MemberExperienceBizTypeEnum;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 会员等级 API 接口
 *
 * @author owen
 */
public interface MemberLevelApi {

    /**
     * 获得会员等级
     *
     * @param id 会员等级编号
     * @return 会员等级
     */
    MemberLevelRespDTO getMemberLevel(Long id);

    /**
     * 获得会员等级列表
     *
     * @param ids 会员等级编号列表
     * @return 会员等级列表
     */
    List<MemberLevelRespDTO> getMemberLevelList(Collection<Long> ids);

    /**
     * 获得会员等级 Map
     *
     * @param ids 会员等级编号列表
     * @return 会员等级 Map
     */
    Map<Long, MemberLevelRespDTO> getMemberLevelMap(Collection<Long> ids);

    /**
     * 获得启用中的会员等级列表
     *
     * @return 会员等级列表
     */
    List<MemberLevelRespDTO> getEnableMemberLevelList();

    /**
     * 增加会员经验
     *
     * @param userId     会员ID
     * @param experience 经验
     * @param bizType    业务类型 {@link MemberExperienceBizTypeEnum}
     * @param bizId      业务编号
     */
    void addExperience(Long userId, Integer experience, Integer bizType, String bizId);

    /**
     * 扣减会员经验
     *
     * @param userId     会员ID
     * @param experience 经验
     * @param bizType    业务类型 {@link MemberExperienceBizTypeEnum}
     * @param bizId      业务编号
     */
    void reduceExperience(Long userId, Integer experience, Integer bizType, String bizId);

    /**
     * 更新用户 等级
     * @param userLevelDTO 会员等级对象
     */
    void updateUserLevel(MemberLevelReqDTO userLevelDTO);

}
