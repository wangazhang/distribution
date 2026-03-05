package com.hissp.distribution.module.mb.convert.commission;

import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleActionRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleConditionRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleRespVO;
import com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.rule.CommissionRuleSaveReqVO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleActionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleConditionDO;
import com.hissp.distribution.module.mb.dal.dataobject.commission.CommissionRuleDefinitionDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommissionRuleConvert {

    CommissionRuleConvert INSTANCE = Mappers.getMapper(CommissionRuleConvert.class);

    CommissionRuleDefinitionDO convert(CommissionRuleSaveReqVO bean);

    CommissionRuleRespVO convert(CommissionRuleDefinitionDO bean);

    List<CommissionRuleRespVO> convertList(List<CommissionRuleDefinitionDO> list);

    @Mapping(source = "valueJson", target = "value")
    @Mapping(source = "extraJson", target = "extra")
    CommissionRuleConditionRespVO convert(CommissionRuleConditionDO conditionDO);

    List<CommissionRuleConditionRespVO> convertConditions(List<CommissionRuleConditionDO> conditionDOS);

    @Mapping(source = "payloadJson", target = "payload")
    CommissionRuleActionRespVO convert(CommissionRuleActionDO actionDO);

    List<CommissionRuleActionRespVO> convertActions(List<CommissionRuleActionDO> actionDOS);

    void copyTo(CommissionRuleSaveReqVO source, @MappingTarget CommissionRuleDefinitionDO target);
}
