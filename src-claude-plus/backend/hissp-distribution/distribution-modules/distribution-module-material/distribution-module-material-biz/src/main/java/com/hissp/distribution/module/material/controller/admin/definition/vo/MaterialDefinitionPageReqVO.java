package com.hissp.distribution.module.material.controller.admin.definition.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 物料定义分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialDefinitionPageReqVO extends PageParam {

    @Schema(description = "物料名称", example = "胶原蛋白")
    private String name;

    @Schema(description = "物料编码", example = "M001")
    private String code;

    @Schema(description = "SPU ID", example = "1")
    private Long spuId;

    @Schema(description = "物料类型", example = "1")
    private Integer type;

    @Schema(description = "状态", example = "1")
    private Integer status;

}

