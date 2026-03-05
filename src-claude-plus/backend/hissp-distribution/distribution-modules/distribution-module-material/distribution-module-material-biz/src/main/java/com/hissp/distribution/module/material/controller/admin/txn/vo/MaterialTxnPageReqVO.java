package com.hissp.distribution.module.material.controller.admin.txn.vo;

import com.hissp.distribution.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Schema(description = "管理后台 - 物料流水分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MaterialTxnPageReqVO extends PageParam {

    @Schema(description = "用户ID", example = "1024")
    private Long userId;

    @Schema(description = "物料ID", example = "2048")
    private Long materialId;

    @Schema(description = "业务唯一标识", example = "10_300")
    private String bizKey;

    @Schema(description = "方向（-1减少，1增加）", example = "1")
    private Integer direction;

}

