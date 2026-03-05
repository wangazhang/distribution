package com.hissp.distribution.module.material.controller.admin.inbound.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 物料入库分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialInboundPageReqVO extends PageParam {

    @Schema(description = "入库单号", example = "INB202312010001")
    private String inboundNo;

    @Schema(description = "申请人ID", example = "1024")
    private Long applicantId;

    @Schema(description = "物料ID", example = "2048")
    private Long materialId;

    @Schema(description = "状态", example = "1")
    private Integer status;

}