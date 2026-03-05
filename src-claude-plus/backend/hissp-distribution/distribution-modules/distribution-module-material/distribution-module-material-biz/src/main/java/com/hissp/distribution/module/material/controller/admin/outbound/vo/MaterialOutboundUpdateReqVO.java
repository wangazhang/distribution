package com.hissp.distribution.module.material.controller.admin.outbound.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 物料出库更新 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class MaterialOutboundUpdateReqVO extends MaterialOutboundCreateReqVO {

    @Schema(description = "出库单ID", required = true, example = "1024")
    private Long id;

}