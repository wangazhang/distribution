package com.hissp.distribution.module.promotion.controller.admin.cms.vo.section;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * CMS板块保存(新增/更新) Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS板块保存(新增/更新) Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsSectionSaveReqVO extends CmsSectionBaseVO {

    @Schema(description = "板块ID(更新时必填)", example = "1")
    private Long id;

}
