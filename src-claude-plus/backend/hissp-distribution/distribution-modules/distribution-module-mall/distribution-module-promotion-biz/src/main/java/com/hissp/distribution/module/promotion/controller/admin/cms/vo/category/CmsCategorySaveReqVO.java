package com.hissp.distribution.module.promotion.controller.admin.cms.vo.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * CMS分类保存(新增/更新) Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS分类保存(新增/更新) Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsCategorySaveReqVO extends CmsCategoryBaseVO {

    @Schema(description = "分类ID(更新时必填)", example = "1")
    private Long id;

}
