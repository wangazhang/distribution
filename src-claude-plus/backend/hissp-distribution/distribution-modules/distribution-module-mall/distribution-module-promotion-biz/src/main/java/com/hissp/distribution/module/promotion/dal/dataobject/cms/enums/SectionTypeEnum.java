package com.hissp.distribution.module.promotion.dal.dataobject.cms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CMS板块类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum SectionTypeEnum {

    ARTICLE("article", "文章类"),
    DYNAMIC("dynamic", "动态类"),
    COURSE("course", "课程类"),
    CUSTOM("custom", "自定义");

    /**
     * 类型
     */
    private final String type;

    /**
     * 描述
     */
    private final String description;

}
