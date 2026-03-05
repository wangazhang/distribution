package com.hissp.distribution.module.mb.adapter.controller.app.homeconfig.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "医美首页配置 Response VO")
@Data
@Deprecated
public class MbHomeConfigRespVO {

    @Schema(description = "配置版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "v1.0.0")
    private String version;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer status;

    @Schema(description = "配置内容(JSON格式)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configContent;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;
} 