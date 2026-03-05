package com.hissp.distribution.module.promotion.dal.dataobject.cms;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.*;

import java.util.Map;

/**
 * CMS板块 DO
 *
 * @author 芋道源码
 */
@TableName(value = "cms_section", autoResultMap = true)
@KeySequence("cms_section_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库,可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsSectionDO extends BaseDO {

    /**
     * 板块ID
     */
    @TableId
    private Long id;

    /**
     * 板块名称
     */
    private String name;

    /**
     * 板块类型
     *
     * 枚举 {@link SectionTypeEnum}
     */
    private String type;

    /**
     * 排版样式
     */
    private String layoutStyle;

    /**
     * 封面展示类型
     */
    private String coverDisplayType;

    /**
     * 板块配置(JSON)
     * 包含按钮开关等配置
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> config;

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
