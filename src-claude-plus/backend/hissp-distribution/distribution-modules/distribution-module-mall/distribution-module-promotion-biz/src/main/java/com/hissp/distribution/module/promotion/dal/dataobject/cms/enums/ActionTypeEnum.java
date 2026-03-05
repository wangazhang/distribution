package com.hissp.distribution.module.promotion.dal.dataobject.cms.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * CMS用户行为类型枚举
 *
 * @author 芋道源码
 */
@Getter
@AllArgsConstructor
public enum ActionTypeEnum {

    LIKE("like", "点赞"),
    COLLECT("collect", "收藏");

    /**
     * 类型
     */
    private final String type;

    /**
     * 描述
     */
    private final String description;

}
