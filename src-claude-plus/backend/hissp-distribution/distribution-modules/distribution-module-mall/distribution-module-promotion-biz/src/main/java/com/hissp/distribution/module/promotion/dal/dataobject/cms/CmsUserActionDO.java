package com.hissp.distribution.module.promotion.dal.dataobject.cms;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * CMS用户行为 DO
 *
 * @author 芋道源码
 */
@TableName(value = "cms_user_action", autoResultMap = true)
@KeySequence("cms_user_action_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库,可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsUserActionDO extends BaseDO {

    /**
     * 行为ID
     */
    @TableId
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     *
     * 关联 {@link CmsArticleDO#getId()}
     */
    private Long articleId;

    /**
     * 行为类型
     * like-点赞, collect-收藏
     */
    private String actionType;

}
