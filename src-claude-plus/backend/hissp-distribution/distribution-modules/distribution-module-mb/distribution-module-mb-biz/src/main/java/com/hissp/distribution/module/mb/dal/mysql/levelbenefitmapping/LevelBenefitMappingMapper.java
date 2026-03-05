//package com.hissp.distribution.module.mb.dal.mysql.levelbenefitmapping;
//
//import com.hissp.distribution.framework.common.pojo.PageResult;
//import com.hissp.distribution.framework.mybatis.core.query.LambdaQueryWrapperX;
//import com.hissp.distribution.framework.mybatis.core.mapper.BaseMapperX;
//import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.rights.vo.LevelBenefitMappingPageReqVO;
//import com.hissp.distribution.module.mb.dal.dataobject.levelbenefitmapping.LevelBenefitMappingDO;
//import org.apache.ibatis.annotations.Mapper;
//
///**
// * 会员等级-权益映射 Mapper
// *
// * @author azhanga
// */
//@Mapper
//public interface LevelBenefitMappingMapper extends BaseMapperX<LevelBenefitMappingDO> {
//
//    default PageResult<LevelBenefitMappingDO> selectPage(LevelBenefitMappingPageReqVO reqVO) {
//        return selectPage(reqVO, new LambdaQueryWrapperX<LevelBenefitMappingDO>()
//                .eqIfPresent(LevelBenefitMappingDO::getLevel, reqVO.getLevel())
//                .eqIfPresent(LevelBenefitMappingDO::getBenefitsGainedJson, reqVO.getBenefitsGainedJson())
//                .eqIfPresent(LevelBenefitMappingDO::getBenefitsRemovedJson, reqVO.getBenefitsRemovedJson())
//                .eqIfPresent(LevelBenefitMappingDO::getActionType, reqVO.getActionType())
//                .eqIfPresent(LevelBenefitMappingDO::getVersion, reqVO.getVersion())
//                .betweenIfPresent(LevelBenefitMappingDO::getCreateTime, reqVO.getCreateTime())
//                .orderByDesc(LevelBenefitMappingDO::getId));
//    }
//
//}