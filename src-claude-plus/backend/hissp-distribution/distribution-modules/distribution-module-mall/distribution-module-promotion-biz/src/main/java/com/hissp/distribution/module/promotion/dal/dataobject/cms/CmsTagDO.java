package com.hissp.distribution.module.promotion.dal.dataobject.cms;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

/**
 * CMS标签 DO
 *
 * @author 芋道源码
 */
@TableName(value = "cms_tag", autoResultMap = true)
@KeySequence("cms_tag_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库,可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsTagDO extends BaseDO {

    /**
     * 标签ID
     */
    @TableId
    private Long id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态
     *
     * 枚举 {@link com.hissp.distribution.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;

}
