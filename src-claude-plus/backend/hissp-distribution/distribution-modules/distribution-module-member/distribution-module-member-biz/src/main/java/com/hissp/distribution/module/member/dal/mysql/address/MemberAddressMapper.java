package com.hissp.distribution.module.member.dal.mysql.address;

import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.hissp.distribution.module.member.dal.dataobject.address.MemberAddressDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberAddressMapper extends BaseMapperX<MemberAddressDO> {

    default MemberAddressDO selectByIdAndUserId(Long id, Long userId) {
        return selectOne(MemberAddressDO::getId, id, MemberAddressDO::getUserId, userId);
    }

    default List<MemberAddressDO> selectListByUserIdAndDefaulted(Long userId, Boolean defaulted) {
        return selectList(new LambdaQueryWrapperX<MemberAddressDO>().eq(MemberAddressDO::getUserId, userId)
                .eqIfPresent(MemberAddressDO::getDefaultStatus, defaulted));
    }

    /**
     * 查询是否存在相同的收货地址（姓名、手机号、区域ID、详细地址全部相同）
     * 
     * @param userId 用户ID
     * @param name 收件人姓名
     * @param mobile 手机号
     * @param areaId 区域ID
     * @param detailAddress 详细地址
     * @return 收货地址对象，如果不存在则返回null
     */
    default MemberAddressDO selectByUserIdAndFullInfo(Long userId, String name, String mobile, String detailAddress) {
        return selectOne(new LambdaQueryWrapperX<MemberAddressDO>()
                .eq(MemberAddressDO::getUserId, userId)
                .eq(MemberAddressDO::getName, name)
                .eq(MemberAddressDO::getMobile, mobile)
                .eq(MemberAddressDO::getDetailAddress, detailAddress));
    }
    
    /**
     * 查询是否存在相同的收货地址（姓名、手机号、区域ID、详细地址全部相同）
     * 
     * @param userId 用户ID
     * @param name 收件人姓名
     * @param mobile 手机号
     * @param areaId 区域ID
     * @param detailAddress 详细地址
     * @return 收货地址对象，如果不存在则返回null
     */
    default MemberAddressDO selectByUserIdAndFullInfo(Long userId, String name, String mobile, Long areaId, String detailAddress) {
        return selectOne(new LambdaQueryWrapperX<MemberAddressDO>()
                .eq(MemberAddressDO::getUserId, userId)
                .eq(MemberAddressDO::getName, name)
                .eq(MemberAddressDO::getMobile, mobile)
                .eq(MemberAddressDO::getAreaId, areaId)
                .eq(MemberAddressDO::getDetailAddress, detailAddress));
    }

}
