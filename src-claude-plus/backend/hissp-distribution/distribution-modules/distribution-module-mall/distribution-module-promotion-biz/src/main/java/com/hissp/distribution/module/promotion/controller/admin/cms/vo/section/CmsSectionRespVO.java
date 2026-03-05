package com.hissp.distribution.module.promotion.controller.admin.cms.vo.section;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * CMS板块 Response VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS板块 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsSectionRespVO extends CmsSectionBaseVO {

    @Schema(description = "板块ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
