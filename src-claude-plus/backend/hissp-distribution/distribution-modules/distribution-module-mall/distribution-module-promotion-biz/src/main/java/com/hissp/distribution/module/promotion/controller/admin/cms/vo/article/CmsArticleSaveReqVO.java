package com.hissp.distribution.module.promotion.controller.admin.cms.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * CMS文章保存(新增/更新) Request VO
 *
 * @author 芋道源码
 */
@Schema(description = "管理后台 - CMS文章保存(新增/更新) Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CmsArticleSaveReqVO extends CmsArticleBaseVO {

    @Schema(description = "文章ID(更新时必填)", example = "1")
    private Long id;

    @Schema(description = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tagIds;

}
