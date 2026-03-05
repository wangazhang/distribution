package com.hissp.distribution.module.promotion.dal.dataobject.cms;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * CMS文章标签关联 DO
 *
 * @author 芋道源码
 */
@TableName(value = "cms_article_tag", autoResultMap = true)
@KeySequence("cms_article_tag_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库,可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsArticleTagDO extends BaseDO {

    /**
     * 关联ID
     */
    @TableId
    private Long id;

    /**
     * 文章ID
     *
     * 关联 {@link CmsArticleDO#getId()}
     */
    private Long articleId;

    /**
     * 标签ID
     *
     * 关联 {@link CmsTagDO#getId()}
     */
    private Long tagId;

}
