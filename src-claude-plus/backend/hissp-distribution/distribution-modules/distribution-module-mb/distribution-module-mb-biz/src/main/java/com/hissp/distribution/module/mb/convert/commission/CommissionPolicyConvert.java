package com.hissp.distribution.module.mb.convert.commission;

import com.hissp.distribution.framework.common.pojo.PageResult;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicyRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicySaveReqVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy.CommissionPolicySimpleRespVO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionPolicyDO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommissionPolicyConvert {

    CommissionPolicyConvert INSTANCE = Mappers.getMapper(CommissionPolicyConvert.class);

    CommissionPolicyDO convert(CommissionPolicySaveReqVO bean);

    CommissionPolicyRespVO convert(CommissionPolicyDO bean);

    List<CommissionPolicyRespVO> convertList(List<CommissionPolicyDO> list);

    List<CommissionPolicySimpleRespVO> convertSimpleList(List<CommissionPolicyDO> list);

    default PageResult<CommissionPolicyRespVO> convertPage(PageResult<CommissionPolicyDO> pageResult) {
        return new PageResult<>(convertList(pageResult.getList()), pageResult.getTotal());
    }

    void copyTo(CommissionPolicySaveReqVO source, @MappingTarget CommissionPolicyDO target);
}
