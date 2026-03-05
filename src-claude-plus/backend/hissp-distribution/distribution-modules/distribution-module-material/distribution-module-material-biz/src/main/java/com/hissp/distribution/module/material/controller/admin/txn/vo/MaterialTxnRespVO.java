package com.hissp.distribution.module.material.controller.admin.txn.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 物料流水 Response VO")
@Data
public class MaterialTxnRespVO {

    @Schema(description = "流水ID", required = true, example = "1")
    private Long id;

    @Schema(description = "用户ID", required = true, example = "1024")
    private Long userId;

    @Schema(description = "用户昵称", required = true, example = "张三")
    private String nickname;

    @Schema(description = "物料ID", required = true, example = "2048")
    private Long materialId;

    @Schema(description = "物料名称", required = true, example = "胶原蛋白")
    private String materialName;

    @Schema(description = "方向（1:增加 -1:减少）", required = true, example = "1")
    private Integer direction;

    @Schema(description = "数量", required = true, example = "10")
    private Integer quantity;

    @Schema(description = "流水后余额", required = true, example = "110")
    private Integer balanceAfter;

    @Schema(description = "业务唯一标识", required = true, example = "10_300")
    private String bizKey;

    @Schema(description = "业务类型", required = true, example = "PACKAGE_GRANT")
    private String bizType;

    @Schema(description = "变更原因/描述", example = "下级购买减少，订单号:XXX")
    private String reason;

    @Schema(description = "创建时间", required = true)
    private LocalDateTime createTime;

}

