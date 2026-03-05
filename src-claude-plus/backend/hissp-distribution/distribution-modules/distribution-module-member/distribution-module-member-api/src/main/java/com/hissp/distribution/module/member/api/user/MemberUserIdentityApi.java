package com.hissp.distribution.module.member.api.user;

import com.hissp.distribution.module.member.api.user.dto.MemberUserIdentityRespDTO;
import com.hissp.distribution.module.member.api.user.dto.MemberUserIdentityCreateReqDTO;

import java.util.Collection;
import java.util.List;

/**
 * 用户身份认证信息 API 接口
 *
 * @author 芋道源码
 */
public interface MemberUserIdentityApi {

    /**
     * 保存用户身份信息
     *
     * @param createReqDTO 创建信息
     * @return 编号
     */
    Long saveUserIdentity(MemberUserIdentityCreateReqDTO createReqDTO);

    /**
     * 获取用户身份信息
     *
     * @param userId 用户ID
     * @return 用户身份信息
     */
    MemberUserIdentityRespDTO getUserIdentity(Long userId);

    /**
     * 审核用户身份信息
     *
     * @param userId 用户ID
     * @param verifyStatus 审核状态
     * @param remark 审核备注
     * @return 是否成功
     */
    Boolean verifyUserIdentity(Long userId, Integer verifyStatus, String remark);

    /**
     * 获取用户身份信息列表
     *
     * @param userIds 用户ID列表
     * @return 用户身份信息列表
     */
    List<MemberUserIdentityRespDTO> getUserIdentityList(Collection<Long> userIds);

    /**
     * 删除用户身份信息
     *
     * @param id 编号
     * @return 是否成功
     */
    Boolean deleteUserIdentity(Long id);
}