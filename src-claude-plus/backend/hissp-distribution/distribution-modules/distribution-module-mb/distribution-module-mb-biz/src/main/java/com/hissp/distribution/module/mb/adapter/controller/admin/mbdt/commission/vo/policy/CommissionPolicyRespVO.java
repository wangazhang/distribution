package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommissionPolicyRespVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "策略编码")
    private String policyCode;

    @Schema(description = "展示名称")
    private String displayName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "业务类型")
    private String bizType;

    @Schema(description = "适用等级")
    private String applicableLevel;

    @Schema(description = "适用商品ID列表")
    private List<Long> applicableProductIds;

    @Schema(description = "适用套包ID列表")
    private List<Long> applicablePackageIds;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
