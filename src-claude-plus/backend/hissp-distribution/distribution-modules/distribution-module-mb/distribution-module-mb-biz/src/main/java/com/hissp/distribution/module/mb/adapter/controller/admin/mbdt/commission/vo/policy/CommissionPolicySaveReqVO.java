package com.hissp.distribution.module.mb.adapter.controller.admin.mbdt.commission.vo.policy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CommissionPolicySaveReqVO {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String policyCode;

    @Schema(description = "展示名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String displayName;

    @Schema(description = "业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String bizType;

    @Schema(description = "适用等级，逗号分隔")
    private String applicableLevel;

    @Schema(description = "适用商品 ID 列表")
    private List<Long> applicableProductIds;

    @Schema(description = "适用套包 ID 列表")
    private List<Long> applicablePackageIds;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Integer versionNo;

    @Schema(description = "备注")
    private String remark;
}
