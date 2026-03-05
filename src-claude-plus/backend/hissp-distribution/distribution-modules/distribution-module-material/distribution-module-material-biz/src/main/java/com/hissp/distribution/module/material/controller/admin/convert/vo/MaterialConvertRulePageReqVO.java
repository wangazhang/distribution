package com.hissp.distribution.module.material.controller.admin.convert.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 物料转换规则分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialConvertRulePageReqVO extends PageParam {

    @Schema(description = "规则名称", example = "胶原蛋白转换规则")
    private String ruleName;

    @Schema(description = "源物料ID", example = "1")
    private Long sourceMaterialId;

    @Schema(description = "目标物料ID", example = "2")
    private Long targetMaterialId;

    @Schema(description = "状态", example = "1")
    private Integer status;

}