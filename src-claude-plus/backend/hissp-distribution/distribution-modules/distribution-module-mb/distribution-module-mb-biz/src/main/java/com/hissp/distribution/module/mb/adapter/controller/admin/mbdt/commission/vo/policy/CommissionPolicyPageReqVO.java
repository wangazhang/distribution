package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommissionPolicyPageReqVO extends PageParam {

    @Schema(description = "关键字（名称/编码）")
    private String keyword;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "业务类型")
    private String bizType;

}
