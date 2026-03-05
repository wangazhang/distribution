package com.hissp.distribution.module.mb.dal.dataobject.levelbenefitmapping;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;

/**
 * 会员等级-权益映射 DO
 *
 * @author azhanga
 */
@TableName("mb_level_benefit_mapping")
@KeySequence("mb_level_benefit_mapping_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LevelBenefitMappingDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 会员等级
     */
    private Integer level;
    /**
     * 升级到该等级获得的权益JSON数据
     */
    private String benefitsGainedJson;
    /**
     * 降级到该等级剔除的权益JSON数据
     */
    private String benefitsRemovedJson;
    /**
     * 成为该等级的动作类型： 1线下 2线上
     */
    private Integer actionType;
    /**
     * 版本
     */
    private String version;

}