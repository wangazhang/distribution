package com.hissp.distribution.module.mb.dal.dataobject.homeconfig;

import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hissp.distribution.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 医美首页配置 DO
 */
@TableName("mb_home_config")
@KeySequence("mb_home_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MbHomeConfigDO extends BaseDO {

    /**
     * 配置ID
     */
    @TableId
    private Long id;
    /**
     * 配置版本号
     */
    private String version;
    /**
     * 状态：0-未生效，1-已生效
     */
    private Integer status;
    /**
     * 配置内容(JSON格式)
     */
    private String configContent;
} 